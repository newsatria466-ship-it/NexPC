package com.nexpc.app.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketManager(private val scope: CoroutineScope) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _telemetryFlow = MutableSharedFlow<SystemStatus>(extraBufferCapacity = 5)
    val telemetryFlow = _telemetryFlow.asSharedFlow()

    fun connect(host: String, port: Int, token: String) {
        disconnect()
        val url = "ws://$host:$port/ws?token=$token"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                scope.launch(Dispatchers.Main) {
                    _isConnected.value = true
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = gson.fromJson(text, JsonObject::class.java)
                    if (json.has("type") && json.get("type").asString == "telemetry") {
                        val dataJson = json.get("data")
                        val status = gson.fromJson(dataJson, SystemStatus::class.java)
                        scope.launch {
                            _telemetryFlow.emit(status)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                scope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                scope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                scope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                }
            }
        })
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (e: Exception) {
            // ignore
        } finally {
            webSocket = null
            _isConnected.value = false
        }
    }
}
