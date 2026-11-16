package com.nexpc.app.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class DiscoveryManager(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val serviceType = "_nexpc._tcp."
    private val tag = "Nexora_Discovery"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched = _hasSearched.asStateFlow()

    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var udpJob: Job? = null
    private var timeoutJob: Job? = null

    fun startDiscovery() {
        stopDiscovery()

        _isSearching.value = true
        _hasSearched.value = false
        _discoveredDevices.value = emptyList()

        // 1. mDNS Discovery via NsdManager
        startNsdDiscovery()

        // 2. Fast UDP Broadcast Probe on LAN (Port 8766)
        startUdpDiscovery()

        // 3. Timeout after 10 seconds if no device found
        timeoutJob = scope.launch {
            delay(10000)
            if (_discoveredDevices.value.isEmpty()) {
                _isSearching.value = false
                _hasSearched.value = true
            }
        }
    }

    private fun startNsdDiscovery() {
        if (nsdManager == null) return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(tag, "Service discovery started: $regType")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(tag, "Service found: ${service.serviceName}")
                if (service.serviceType.contains("nexpc")) {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e(tag, "Resolve failed: $errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val host = serviceInfo.host?.hostAddress ?: return
                            val port = serviceInfo.port
                            val devName = serviceInfo.serviceName
                                .replace("NexPC-", "")
                                .replace("Nexora-", "")
                                .replace("._nexpc._tcp.local.", "")

                            val devOs = serviceInfo.attributes?.get("os")?.let { String(it, Charsets.UTF_8) } ?: "Arch Linux"
                            val osFam = serviceInfo.attributes?.get("os_family")?.let { String(it, Charsets.UTF_8) } ?: (if (devOs.contains("windows", ignoreCase = true)) "windows" else "linux")

                            val device = DiscoveredDevice(
                                name = devName,
                                host = host,
                                port = port,
                                os = devOs,
                                osFamily = osFam,
                                requiresAuth = true
                            )

                            addDiscoveredDevice(device)
                        }
                    })
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(tag, "Service lost: ${service.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(tag, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Start discovery failed: $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Stop discovery failed: $errorCode")
            }
        }

        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(tag, "Error discovering services: ${e.message}")
        }
    }

    private fun startUdpDiscovery() {
        udpJob = scope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket().apply {
                    broadcast = true
                    soTimeout = 1500
                }

                val pingMsg = "NEXORA_DISCOVERY_PING".toByteArray()
                val targetAddr = InetAddress.getByName("255.255.255.255")
                val sendPacket = DatagramPacket(pingMsg, pingMsg.size, targetAddr, 8766)

                val receiveBuffer = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)

                var attempts = 0
                while (isActive && attempts < 8) {
                    attempts++
                    try {
                        socket.send(sendPacket)
                    } catch (e: Exception) {
                        Log.e(tag, "UDP send failed: ${e.message}")
                    }

                    val loopEnd = System.currentTimeMillis() + 1200
                    while (System.currentTimeMillis() < loopEnd && isActive) {
                        try {
                            socket.receive(receivePacket)
                            val resp = String(receivePacket.data, 0, receivePacket.length).trim()
                            val fromIp = receivePacket.address.hostAddress ?: continue

                            val json = gson.fromJson(resp, JsonObject::class.java)
                            if (json != null && json.has("type") && json.get("type").asString == "discovery_pong") {
                                val devName = json.get("name")?.asString ?: "Arch PC"
                                val devPort = json.get("port")?.asInt ?: 8765
                                val devOs = json.get("os")?.asString ?: "Arch Linux"
                                val osFam = json.get("os_family")?.asString ?: (if (devOs.contains("windows", ignoreCase = true)) "windows" else "linux")
                                val requiresAuth = (json.get("auth")?.asInt ?: 1) == 1

                                val dev = DiscoveredDevice(
                                    name = devName,
                                    host = fromIp,
                                    port = devPort,
                                    os = devOs,
                                    osFamily = osFam,
                                    requiresAuth = requiresAuth
                                )
                                addDiscoveredDevice(dev)
                            }
                        } catch (e: java.net.SocketTimeoutException) {
                            // normal timeout, probe again
                        } catch (e: Exception) {
                            // ignore parse error
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "UDP discovery error: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    private fun addDiscoveredDevice(device: DiscoveredDevice) {
        val current = _discoveredDevices.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.name.equals(device.name, ignoreCase = true) || (it.host == device.host && it.port == device.port)
        }
        if (existingIndex >= 0) {
            current[existingIndex] = device
        } else {
            current.add(device)
        }
        _discoveredDevices.value = current
        _isSearching.value = false
        _hasSearched.value = true
    }

    fun stopDiscovery() {
        timeoutJob?.cancel()
        udpJob?.cancel()
        discoveryListener?.let {
            try {
                nsdManager?.stopServiceDiscovery(it)
            } catch (e: Exception) {
                // ignore
            }
            discoveryListener = null
        }
        _isSearching.value = false
    }
}
