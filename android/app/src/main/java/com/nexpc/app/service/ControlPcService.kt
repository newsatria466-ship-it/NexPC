package com.nexpc.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nexpc.app.MainActivity
import com.nexpc.app.R
import com.nexpc.app.data.AppNotificationManager
import com.nexpc.app.data.BatteryInfo
import com.nexpc.app.data.CpuInfo
import com.nexpc.app.data.PreferencesManager
import com.nexpc.app.data.SystemStatus
import kotlinx.coroutines.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class ControlPcService : Service() {

    companion object {
        const val ACTION_START = "com.nexpc.app.action.START_SERVICE"
        const val ACTION_STOP = "com.nexpc.app.action.STOP_SERVICE"

        private const val CHANNEL_SERVICE = "controlpc_channel_service"
        private const val NOTIFICATION_ID = 2001

        fun startService(context: Context) {
            val intent = Intent(context, ControlPcService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ControlPcService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: AppNotificationManager
    private lateinit var prefs: PreferencesManager
    private val gson = Gson()

    private var okHttpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = AppNotificationManager(this)
        prefs = PreferencesManager(this)
        createServiceNotificationChannel()

        okHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            stopMonitoring()
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!isRunning) {
            isRunning = true
            startForeground(NOTIFICATION_ID, buildServiceNotification("Menghubungkan ke PC..."))
            startMonitoring()
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive && isRunning) {
                val host = prefs.savedHost
                val port = prefs.savedPort

                if (host.isNotEmpty()) {
                    connectWebSocket(host, port)
                }

                // Wait 5 seconds before attempting reconnect if disconnected
                delay(5000)
            }
        }
    }

    private fun connectWebSocket(host: String, port: Int) {
        val token = prefs.savedToken.ifEmpty { "0909" }
        val wsUrl = "ws://$host:$port/ws?token=$token"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = okHttpClient?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                updateServiceNotification("Terhubung ke PC • Memantau di latar belakang")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingTelemetry(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                updateServiceNotification("Menghubungkan ulang ke PC...")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                updateServiceNotification("Terputus • Menghubungkan ulang...")
            }
        })
    }

    private fun handleIncomingTelemetry(jsonText: String) {
        try {
            val root = gson.fromJson(jsonText, JsonObject::class.java)
            if (root.get("type")?.asString == "telemetry") {
                val dataObj = root.getAsJsonObject("data")
                val status = gson.fromJson(dataObj, SystemStatus::class.java)

                // 1. Check Battery status
                status.battery?.let { bat ->
                    notificationManager.checkAndNotifyBattery(bat.percent, bat.powerPlugged)
                }

                // 2. Check CPU Overheat
                if (status.cpu.temperature > 0f) {
                    notificationManager.checkAndNotifyCpuTemp(status.cpu.temperature)
                }

                // Update small persistent notification status
                val batStr = status.battery?.let { "${it.percent.toInt()}%" } ?: "N/A"
                val plugStr = if (status.battery?.powerPlugged == true) "⚡" else ""
                val tempStr = if (status.cpu.temperature > 0f) "${status.cpu.temperature.toInt()}°C" else ""
                
                updateServiceNotification("Baterai: $batStr $plugStr  •  Suhu CPU: $tempStr")
            }
        } catch (e: Exception) {
            // Ignore parse errors in background
        }
    }

    private fun stopMonitoring() {
        isRunning = false
        try {
            webSocket?.close(1000, "Service stopped")
            webSocket = null
        } catch (e: Exception) {
            // ignore
        }
        serviceScope.cancel()
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_SERVICE,
                "Background Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Status layanan pemantau status PC di latar belakang"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildServiceNotification(contentText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_nexora_logo)
            .setContentTitle("Nexora Background Active")
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateServiceNotification(contentText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildServiceNotification(contentText))
    }
}
