package com.nexpc.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import androidx.core.content.FileProvider

import com.nexpc.app.BuildConfig
import com.nexpc.app.data.*
import com.nexpc.app.service.ControlPcService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileOutputStream

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class TransferStatus {
    IDLE,
    TRANSFERRING,
    COMPLETED,
    CANCELED,
    FAILED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = PreferencesManager(application)
    val discoveryManager = DiscoveryManager(application)
    val notificationManager = AppNotificationManager(application)
    private val wsManager = WebSocketManager(viewModelScope)

    private var apiService: NexPCApiService? = null
    private var pollingJob: Job? = null

    // UI States
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _status = MutableStateFlow(SystemStatus())
    val status = _status.asStateFlow()

    private val _processes = MutableStateFlow<List<ProcessItem>>(emptyList())
    val processes = _processes.asStateFlow()

    private val _apps = MutableStateFlow<List<AppItem>>(emptyList())
    val apps = _apps.asStateFlow()

    private val _packageUpdates = MutableStateFlow<UpdatesResponse>(UpdatesResponse())
    val packageUpdates = _packageUpdates.asStateFlow()

    private val _packageSearchResults = MutableStateFlow<List<PackageSearchResult>>(emptyList())
    val packageSearchResults = _packageSearchResults.asStateFlow()

    private val _statusMessage = MutableSharedFlow<String>()
    val statusMessage = _statusMessage.asSharedFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError = _connectionError.asStateFlow()

    fun clearConnectionError() {
        _connectionError.value = null
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // File Transfer States & Progress 1-100%
    private val _browseResponse = MutableStateFlow(BrowseResponse())
    val browseResponse = _browseResponse.asStateFlow()

    private val _transferHistory = MutableStateFlow<List<TransferHistoryItem>>(emptyList())
    val transferHistory = _transferHistory.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress = _uploadProgress.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _isTransferring = MutableStateFlow(false)
    val isTransferring = _isTransferring.asStateFlow()

    private val _transferStatus = MutableStateFlow(TransferStatus.IDLE)
    val transferStatus = _transferStatus.asStateFlow()

    private val _transferType = MutableStateFlow("upload") // "upload" or "download"
    val transferType = _transferType.asStateFlow()

    private val _transferTitle = MutableStateFlow("")
    val transferTitle = _transferTitle.asStateFlow()

    private val _transferItemName = MutableStateFlow("")
    val transferItemName = _transferItemName.asStateFlow()

    private val _transferProgress = MutableStateFlow(0f) // 0.0f - 1.0f (1-100%)
    val transferProgress = _transferProgress.asStateFlow()

    private val _transferDetails = MutableStateFlow("")
    val transferDetails = _transferDetails.asStateFlow()

    private val _transferBatchCount = MutableStateFlow(1)
    val transferBatchCount = _transferBatchCount.asStateFlow()

    private val _transferCurrentIndex = MutableStateFlow(1)
    val transferCurrentIndex = _transferCurrentIndex.asStateFlow()

    private var currentTransferJob: Job? = null

    fun cancelTransfer() {
        currentTransferJob?.cancel()
        currentTransferJob = null
        _transferStatus.value = TransferStatus.CANCELED
        _transferTitle.value = "Transfer Dibatalkan"
        _transferDetails.value = "Proses transfer dihentikan oleh pengguna"
        _statusMessage.tryEmit("Transfer dibatalkan")
        _isUploading.value = false
    }

    fun dismissTransferModal() {
        _isTransferring.value = false
        _transferStatus.value = TransferStatus.IDLE
        _transferProgress.value = 0f
        _transferDetails.value = ""
        _isUploading.value = false
        currentTransferJob = null
    }

    fun formatByteSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }

    // Laptop Mode States
    private val _laptopStatus = MutableStateFlow(LaptopStatus())
    val laptopStatus = _laptopStatus.asStateFlow()

    // OTA Update States
    private val _availableUpdate = MutableStateFlow<AppVersionInfo?>(null)
    val availableUpdate = _availableUpdate.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate = _isDownloadingUpdate.asStateFlow()

    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress = _updateProgress.asStateFlow()

    // Navigation request from notifications
    private val _requestedTab = MutableStateFlow<String?>(null)
    val requestedTab = _requestedTab.asStateFlow()

    fun navigateToTab(tabName: String) {
        _requestedTab.value = tabName
    }

    fun clearRequestedTab() {
        _requestedTab.value = null
    }

    // Background Monitoring Toggle State
    val isBgMonitoringEnabled = MutableStateFlow(prefs.isBackgroundMonitoringEnabled)

    fun setBackgroundMonitoring(enabled: Boolean) {
        prefs.isBackgroundMonitoringEnabled = enabled
        isBgMonitoringEnabled.value = enabled
        if (enabled && _connectionState.value == ConnectionState.CONNECTED) {
            ControlPcService.startService(getApplication())
        } else {
            ControlPcService.stopService(getApplication())
        }
    }

    val hostInput = MutableStateFlow(prefs.savedHost)
    val portInput = MutableStateFlow(prefs.savedPort.toString())
    val tokenInput = MutableStateFlow(prefs.savedToken)

    init {
        // Collect telemetry from WebSocket
        viewModelScope.launch {
            wsManager.telemetryFlow.collect { newStatus ->
                _status.value = newStatus

                // Check battery alerts (low battery < 20% or battery full 100%)
                newStatus.battery?.let { bat ->
                    notificationManager.checkAndNotifyBattery(bat.percent, bat.powerPlugged)
                }

                // Check CPU temperature overheat alert (>= 85°C)
                if (newStatus.cpu.temperature > 0f) {
                    notificationManager.checkAndNotifyCpuTemp(newStatus.cpu.temperature)
                }
            }
        }

        viewModelScope.launch {
            wsManager.isConnected.collect { isWsConnected ->
                if (isWsConnected) {
                    _connectionState.value = ConnectionState.CONNECTED
                } else if (_connectionState.value == ConnectionState.CONNECTED) {
                    startPollingFallback()
                }
            }
        }

        // Auto-reconnect if already paired
        if (prefs.isPaired) {
            tryQuickConnectLastKnown()
        }

        // Auto-connect when paired device is discovered via mDNS or UDP
        viewModelScope.launch {
            discoveryManager.discoveredDevices.collect { devices ->
                if (prefs.isPaired && _connectionState.value != ConnectionState.CONNECTED && _connectionState.value != ConnectionState.CONNECTING) {
                    val match = devices.firstOrNull {
                        it.name.equals(prefs.savedDeviceName, ignoreCase = true) || it.host == prefs.savedHost
                    } ?: if (devices.size == 1) devices.first() else null

                    if (match != null) {
                        pairAndConnect(match, prefs.savedToken)
                    }
                }
            }
        }
    }

    fun tryQuickConnectLastKnown() {
        val host = prefs.savedHost
        val port = prefs.savedPort
        val token = prefs.savedToken
        if (host.isNotEmpty() && token.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val testApi = ApiClient.createService(host, port)
                    val testStatus = testApi.getStatus()
                    withContext(Dispatchers.Main) {
                        hostInput.value = host
                        portInput.value = port.toString()
                        tokenInput.value = token
                        connect()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        discoveryManager.startDiscovery()
                    }
                }
            }
        } else {
            discoveryManager.startDiscovery()
        }
    }

    fun pairAndConnect(device: DiscoveredDevice, pin: String) {
        val cleanPin = pin.trim()
        _connectionError.value = null
        hostInput.value = device.host
        portInput.value = device.port.toString()
        tokenInput.value = cleanPin

        prefs.savedHost = device.host
        prefs.savedPort = device.port
        prefs.savedToken = cleanPin
        prefs.savedDeviceName = device.name

        _connectionState.value = ConnectionState.CONNECTING
        apiService = ApiClient.createService(device.host, device.port)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val initialStatus = apiService?.getStatus() ?: throw Exception("PC tidak dapat dijangkau")
                _status.value = initialStatus

                // Verifikasi PIN sebelum koneksi websocket
                try {
                    apiService?.verifyAuth(cleanPin)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 401 || e.code() == 403) {
                        throw Exception("PIN salah! Masukkan PIN yang benar (default: 0909)")
                    }
                    throw e
                }

                wsManager.connect(device.host, device.port, cleanPin)
                prefs.isPaired = true
                _connectionState.value = ConnectionState.CONNECTED
                _connectionError.value = null
                _statusMessage.emit("Terhubung ke ${device.name}")

                if (prefs.isBackgroundMonitoringEnabled) {
                    ControlPcService.startService(getApplication())
                }

                loadProcesses()
                loadLaptopStatus()
                loadTransferHistory()
                browseFiles()
                checkForAppUpdate()
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                prefs.isPaired = false
                val msg = e.localizedMessage ?: "PIN salah atau PC offline"
                _connectionError.value = msg
                _statusMessage.emit("Pairing gagal: $msg")
            }
        }
    }

    fun unpair() {
        disconnect()
        prefs.clearPairing()
        tokenInput.value = ""
        discoveryManager.startDiscovery()
    }

    fun startDiscovery() {
        discoveryManager.startDiscovery()
    }

    fun stopDiscovery() {
        discoveryManager.stopDiscovery()
    }

    fun selectDiscoveredDevice(device: DiscoveredDevice) {
        hostInput.value = device.host
        portInput.value = device.port.toString()
        prefs.savedHost = device.host
        prefs.savedPort = device.port
        prefs.savedDeviceName = device.name
    }

    fun connect() {
        val host = hostInput.value.trim()
        val port = portInput.value.trim().toIntOrNull() ?: 8765
        val token = tokenInput.value.trim()

        _connectionError.value = null
        prefs.savedHost = host
        prefs.savedPort = port
        prefs.savedToken = token

        _connectionState.value = ConnectionState.CONNECTING
        apiService = ApiClient.createService(host, port)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val initialStatus = apiService?.getStatus() ?: throw Exception("Cannot reach host")
                _status.value = initialStatus

                try {
                    apiService?.verifyAuth(token)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 401 || e.code() == 403) {
                        throw Exception("PIN salah! Masukkan PIN yang benar (default: 0909)")
                    }
                    throw e
                }

                wsManager.connect(host, port, token)
                prefs.isPaired = true
                _connectionState.value = ConnectionState.CONNECTED
                _connectionError.value = null
                _statusMessage.emit("Connected to ${initialStatus.hostname}")

                // Start background monitoring service if enabled
                if (prefs.isBackgroundMonitoringEnabled) {
                    ControlPcService.startService(getApplication())
                }

                // Initial data load
                loadProcesses()
                loadLaptopStatus()
                loadTransferHistory()
                browseFiles()
                checkForAppUpdate()
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.ERROR
                val msg = e.localizedMessage ?: "Koneksi gagal"
                _connectionError.value = msg
                _statusMessage.emit("Connection failed: $msg")
            }
        }
    }

    fun disconnect() {
        pollingJob?.cancel()
        wsManager.disconnect()
        ControlPcService.stopService(getApplication())
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectionError.value = null
    }

    private fun startPollingFallback() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            var consecutiveErrors = 0
            while (_connectionState.value == ConnectionState.CONNECTED) {
                try {
                    val s = apiService?.getStatus()
                    if (s != null) {
                        _status.value = s
                        consecutiveErrors = 0
                    }
                } catch (e: Exception) {
                    consecutiveErrors++
                    if (consecutiveErrors >= 5) {
                        _connectionState.value = ConnectionState.ERROR
                        break
                    }
                }
                delay(2000)
            }
        }
    }

    // === File Transfer Methods ===

    fun browseFiles(path: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val res = apiService?.browseFiles(tokenInput.value, path)
                if (res != null) {
                    _browseResponse.value = res
                }
            } catch (e: Exception) {
                _statusMessage.emit("Failed to browse files: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTransferHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.getTransferHistory(tokenInput.value)
                if (res != null) {
                    _transferHistory.value = res
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun uploadFileFromUri(uri: Uri, context: Context, targetDir: String? = null) {
        uploadMultipleFiles(listOf(uri), context, targetDir)
    }

    fun uploadMultipleFiles(uris: List<Uri>, context: Context, targetDir: String? = null) {
        if (uris.isEmpty()) return
        currentTransferJob?.cancel()
        currentTransferJob = viewModelScope.launch(Dispatchers.IO) {
            _isUploading.value = true
            _isTransferring.value = true
            _transferStatus.value = TransferStatus.TRANSFERRING
            _transferType.value = "upload"
            _transferBatchCount.value = uris.size

            var successCount = 0
            var failCount = 0

            try {
                val parentJob = coroutineContext[Job]

                for ((index, uri) in uris.withIndex()) {
                    ensureActive()
                    _transferCurrentIndex.value = index + 1
                    _transferProgress.value = 0.01f
                    _uploadProgress.value = (index.toFloat() / uris.size.toFloat()).coerceIn(0.01f, 1f)

                    var fileName = "file_${System.currentTimeMillis()}"
                    var fileSize = 0L

                    val contentResolver = context.contentResolver
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                            if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                        }
                    }

                    _transferItemName.value = fileName
                    _transferTitle.value = if (uris.size > 1) {
                        "Mengirim (${index + 1}/${uris.size}) ke PC"
                    } else {
                        "Mengirim ke PC"
                    }
                    _transferDetails.value = "Mempersiapkan..."

                    try {
                        val requestBody = object : RequestBody() {
                            override fun contentType() = (contentResolver.getType(uri) ?: "application/octet-stream").toMediaTypeOrNull()
                            override fun contentLength() = fileSize.coerceAtLeast(0L)
                            override fun writeTo(sink: BufferedSink) {
                                val buffer = ByteArray(32 * 1024)
                                var uploadedBytes = 0L
                                val stream = contentResolver.openInputStream(uri) ?: throw Exception("Gagal membaca file $fileName")
                                stream.use { s ->
                                    var read: Int
                                    while (s.read(buffer).also { read = it } != -1) {
                                        if (parentJob?.isActive != true) {
                                            throw CancellationException("Upload dibatalkan")
                                        }
                                        sink.write(buffer, 0, read)
                                        sink.flush()
                                        uploadedBytes += read
                                        val pct = if (fileSize > 0) (uploadedBytes.toFloat() / fileSize.toFloat()).coerceIn(0.01f, 1f) else 0.5f
                                        _transferProgress.value = pct
                                        _uploadProgress.value = ((index.toFloat() + pct) / uris.size.toFloat()).coerceIn(0.01f, 1f)
                                        val upFmt = formatByteSize(uploadedBytes)
                                        val totalFmt = formatByteSize(fileSize)
                                        _transferDetails.value = if (uris.size > 1) {
                                            "File ${index + 1}/${uris.size} · $upFmt / $totalFmt (${(pct * 100).toInt()}%)"
                                        } else {
                                            "$upFmt / $totalFmt (${(pct * 100).toInt()}%)"
                                        }
                                    }
                                }
                            }
                        }

                        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                        val targetDirBody = targetDir?.toRequestBody("text/plain".toMediaTypeOrNull())

                        val response = apiService?.uploadFile(tokenInput.value, part, targetDirBody)
                        if (response?.success == true) {
                            successCount++
                            _transferProgress.value = 1f
                        } else {
                            failCount++
                            val errMsg = response?.message ?: "Gagal upload"
                            _statusMessage.emit("Gagal mengirim $fileName: $errMsg")
                        }
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Exception) {
                        failCount++
                        val err = e.localizedMessage ?: e.message ?: "Error"
                        _statusMessage.emit("Gagal mengirim $fileName: $err")
                    }
                }

                _isUploading.value = false
                _transferProgress.value = 1f
                _uploadProgress.value = 1f

                if (failCount == 0 && successCount > 0) {
                    _transferStatus.value = TransferStatus.COMPLETED
                    _transferTitle.value = if (uris.size > 1) "Pengiriman Selesai!" else "File Berhasil Dikirim!"
                    _transferDetails.value = if (uris.size > 1) {
                        "Semua $successCount file berhasil disimpan di PC"
                    } else {
                        "File tersimpan di ~/Downloads/NexPC"
                    }
                    val summary = if (uris.size > 1) "$successCount file berhasil dikirim ke PC" else "File berhasil dikirim ke PC"
                    _statusMessage.emit(summary)
                    notificationManager.notifyFileTransfer(if (uris.size > 1) "${uris.size} files" else _transferItemName.value, isSuccess = true)
                } else if (successCount > 0) {
                    _transferStatus.value = TransferStatus.COMPLETED
                    _transferTitle.value = "Selesai Sebagian"
                    _transferDetails.value = "$successCount berhasil, $failCount gagal"
                    _statusMessage.emit("$successCount dari ${uris.size} file berhasil dikirim")
                } else {
                    _transferStatus.value = TransferStatus.FAILED
                    _transferTitle.value = "Pengiriman Gagal"
                    _transferDetails.value = "Tidak ada file yang berhasil dikirim"
                }

                loadTransferHistory()
                browseFiles(_browseResponse.value.currentPath)

            } catch (ce: CancellationException) {
                _transferStatus.value = TransferStatus.CANCELED
                _transferTitle.value = "Transfer Dibatalkan"
                _transferDetails.value = "Proses transfer dihentikan oleh pengguna"
                _statusMessage.emit("Transfer dibatalkan")
            } catch (e: Exception) {
                _transferStatus.value = TransferStatus.FAILED
                _transferTitle.value = "Pengiriman Gagal"
                _transferDetails.value = e.localizedMessage ?: "Terjadi kesalahan"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun deleteFileItem(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.deleteFile(tokenInput.value, FileDeleteRequest(path))
                _statusMessage.emit(res?.message ?: "Deleted successfully")
                browseFiles(_browseResponse.value.currentPath)
            } catch (e: Exception) {
                _statusMessage.emit("Delete failed: ${e.message}")
            }
        }
    }

    fun renameFileItem(path: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.renameFile(tokenInput.value, FileRenameRequest(path, newName))
                _statusMessage.emit(res?.message ?: "Renamed successfully")
                browseFiles(_browseResponse.value.currentPath)
            } catch (e: Exception) {
                _statusMessage.emit("Rename failed: ${e.message}")
            }
        }
    }

    fun moveFileItem(sourcePath: String, targetDir: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.moveFile(tokenInput.value, FileMoveRequest(sourcePath, targetDir))
                _statusMessage.emit(res?.message ?: "Moved successfully")
                browseFiles(_browseResponse.value.currentPath)
            } catch (e: Exception) {
                _statusMessage.emit("Move failed: ${e.message}")
            }
        }
    }

    fun createNewFolder(name: String) {
        val current = _browseResponse.value.currentPath
        if (current.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.createFolder(tokenInput.value, CreateFolderRequest(current, name))
                _statusMessage.emit(res?.message ?: "Folder created")
                browseFiles(current)
            } catch (e: Exception) {
                _statusMessage.emit("Failed to create folder: ${e.message}")
            }
        }
    }

    fun downloadFileToPhone(fileItem: FileItem, context: Context) {
        currentTransferJob?.cancel()
        currentTransferJob = viewModelScope.launch(Dispatchers.IO) {
            _isTransferring.value = true
            _transferStatus.value = TransferStatus.TRANSFERRING
            _transferType.value = "download"
            _transferTitle.value = "Mengunduh ke HP"
            _transferItemName.value = fileItem.name
            _transferBatchCount.value = 1
            _transferCurrentIndex.value = 1
            _transferProgress.value = 0.01f
            _transferDetails.value = "Menghubungkan ke PC..."
            _statusMessage.emit("Mengunduh ${fileItem.name}...")

            var targetFile: File? = null

            try {
                val responseBody = apiService?.downloadFile(tokenInput.value, fileItem.path)
                    ?: throw Exception("Tidak ada respon dari server")

                val totalBytes = responseBody.contentLength().let {
                    if (it > 0) it else fileItem.sizeBytes
                }.coerceAtLeast(1L)

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val nexpcDir = File(downloadsDir, "NexPC")
                nexpcDir.mkdirs()
                val outFile = File(nexpcDir, fileItem.name)
                targetFile = outFile

                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(outFile)
                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                var downloadedBytes = 0L

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive()
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val pct = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0.01f, 1f)
                        _transferProgress.value = pct
                        val dlFmt = formatByteSize(downloadedBytes)
                        val totalFmt = formatByteSize(totalBytes)
                        _transferDetails.value = "$dlFmt / $totalFmt (${(pct * 100).toInt()}%)"
                    }
                    outputStream.flush()
                } finally {
                    outputStream.close()
                    inputStream.close()
                }

                _transferProgress.value = 1f
                _transferStatus.value = TransferStatus.COMPLETED
                _transferTitle.value = "Unduhan Selesai!"
                _transferDetails.value = "Tersimpan di: Downloads/NexPC/${fileItem.name}"
                _statusMessage.emit("Selesai diunduh ke: Downloads/NexPC/${fileItem.name}")
                notificationManager.notifyFileTransfer(fileItem.name, isSuccess = true)
                loadTransferHistory()

            } catch (ce: CancellationException) {
                targetFile?.let { if (it.exists()) it.delete() }
                _transferStatus.value = TransferStatus.CANCELED
                _transferTitle.value = "Unduhan Dibatalkan"
                _transferDetails.value = "Pengunduhan dihentikan oleh pengguna"
                _statusMessage.emit("Unduhan dibatalkan")
            } catch (e: Exception) {
                targetFile?.let { if (it.exists()) it.delete() }
                _transferStatus.value = TransferStatus.FAILED
                _transferTitle.value = "Gagal Mengunduh"
                _transferDetails.value = e.localizedMessage ?: "Terjadi kesalahan"
                _statusMessage.emit("Gagal mengunduh: ${e.message}")
                notificationManager.notifyFileTransfer(fileItem.name, isSuccess = false, errorMessage = e.message)
            }
        }
    }

    // === Laptop Mode Methods ===

    fun loadLaptopStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.getLaptopStatus(tokenInput.value)
                if (res != null) {
                    _laptopStatus.value = res
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun setPowerProfile(profile: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.setPowerProfile(tokenInput.value, PowerProfileBody(profile))
                if (res != null) {
                    _statusMessage.emit(res.message)
                    loadLaptopStatus()
                }
            } catch (e: Exception) {
                _statusMessage.emit("Failed to change profile: ${e.message}")
            }
        }
    }

    // === Process & App & Control Methods ===

    fun loadProcesses(search: String? = null, sortBy: String = "cpu") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = apiService?.getProcesses(tokenInput.value, limit = 60, sortBy = sortBy, search = search)
                if (list != null) {
                    _processes.value = list
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun killProcess(pid: Int, force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.killProcess(pid, tokenInput.value, NexPCApiService.KillBody(force))
                if (res != null) {
                    _statusMessage.emit(res.message)
                    loadProcesses()
                }
            } catch (e: Exception) {
                _statusMessage.emit("Failed to kill process: ${e.message}")
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = apiService?.getApps(tokenInput.value)
                if (list != null) {
                    _apps.value = list
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun launchApp(desktopId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.launchApp(desktopId, tokenInput.value)
                if (res != null) {
                    _statusMessage.emit(res.message)
                    delay(1000)
                    loadApps()
                }
            } catch (e: Exception) {
                _statusMessage.emit("Failed to launch app: ${e.message}")
            }
        }
    }

    fun stopApp(desktopId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.stopApp(desktopId, tokenInput.value)
                if (res != null) {
                    _statusMessage.emit(res.message)
                    delay(1000)
                    loadApps()
                }
            } catch (e: Exception) {
                _statusMessage.emit("Failed to stop app: ${e.message}")
            }
        }
    }

    fun lockPc() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.lockPc(tokenInput.value)
                _statusMessage.emit(res?.message ?: "Screen locked")
            } catch (e: Exception) {
                _statusMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun suspendPc() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.suspendPc(tokenInput.value)
                _statusMessage.emit(res?.message ?: "Suspend initiated")
            } catch (e: Exception) {
                _statusMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun rebootPc() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.rebootPc(tokenInput.value)
                _statusMessage.emit(res?.message ?: "Reboot initiated")
            } catch (e: Exception) {
                _statusMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun shutdownPc() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.shutdownPc(tokenInput.value)
                _statusMessage.emit(res?.message ?: "Shutdown initiated")
            } catch (e: Exception) {
                _statusMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun controlVolume(action: String, value: Int = 5) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiService?.controlVolume(tokenInput.value, NexPCApiService.VolumeBody(action, value))
                _statusMessage.emit(res?.message ?: "Volume updated")
            } catch (e: Exception) {
                _statusMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun checkPackageUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val res = apiService?.getPackageUpdates(tokenInput.value)
                if (res != null) {
                    _packageUpdates.value = res
                }
            } catch (e: Exception) {
                _statusMessage.emit("Failed to check updates: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPackages(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val res = apiService?.searchPackages(tokenInput.value, query)
                if (res != null) {
                    _packageSearchResults.value = res
                }
            } catch (e: Exception) {
                _statusMessage.emit("Search error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // === Pacman Package Management ===

    fun installPackage(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _statusMessage.emit("Memulai instalasi $packageName di PC...")
                val res = apiService?.installPackage(tokenInput.value, PackageInstallRequest(packageName))
                if (res?.success == true) {
                    _statusMessage.emit("Instalasi $packageName berhasil dijalankan di PC.")
                } else {
                    _statusMessage.emit(res?.message ?: "Gagal memulai instalasi.")
                }
            } catch (e: Exception) {
                _statusMessage.emit("Error: ${e.message}")
            }
        }
    }

    // === Universal Shared Clipboard (HP ⇄ PC) ===

    private val _pcClipboardText = MutableStateFlow("")
    val pcClipboardText: StateFlow<String> = _pcClipboardText.asStateFlow()

    private val _isClipboardSyncing = MutableStateFlow(false)
    val isClipboardSyncing: StateFlow<Boolean> = _isClipboardSyncing.asStateFlow()

    fun fetchPcClipboard(onSuccess: ((String) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isClipboardSyncing.value = true
            try {
                val res = apiService?.getClipboard(tokenInput.value)
                val text = res?.text ?: ""
                _pcClipboardText.value = text
                withContext(Dispatchers.Main) {
                    onSuccess?.invoke(text)
                }
            } catch (e: Exception) {
                _statusMessage.emit("Gagal membaca clipboard PC: ${e.message}")
            } finally {
                _isClipboardSyncing.value = false
            }
        }
    }

    fun sendTextToPcClipboard(text: String, onSuccess: (() -> Unit)? = null) {
        if (text.isBlank()) {
            viewModelScope.launch { _statusMessage.emit("Ketik atau tempel teks terlebih dahulu") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isClipboardSyncing.value = true
            try {
                val res = apiService?.setClipboard(tokenInput.value, SetClipboardRequest(text))
                if (res?.success == true || res?.status == "ok") {
                    _pcClipboardText.value = text
                    _statusMessage.emit("Tersalin di PC! Tinggal tekan Ctrl+V di PC ✨")
                    withContext(Dispatchers.Main) {
                        onSuccess?.invoke()
                    }
                } else {
                    _statusMessage.emit("Gagal menyalin ke PC: ${res?.message}")
                }
            } catch (e: Exception) {
                _statusMessage.emit("Gagal mengirim ke PC: ${e.message}")
            } finally {
                _isClipboardSyncing.value = false
            }
        }
    }

    fun copyPcClipboardToPhone(context: Context, onCopied: ((String) -> Unit)? = null) {
        fetchPcClipboard { text ->
            if (text.isNotBlank()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
                val clip = ClipData.newPlainText("Nexora PC", text)
                clipboard.setPrimaryClip(clip)
                viewModelScope.launch {
                    _statusMessage.emit("Teks terbaru dari laptop disalin ke HP! 📋")
                }
                onCopied?.invoke(text)
            } else {
                viewModelScope.launch {
                    _statusMessage.emit("Clipboard laptop kamu sedang kosong")
                }
                onCopied?.invoke("")
            }
        }
    }


    fun sendPhoneClipboardToPc(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        val clip = clipboard.primaryClip
        val text = if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).text?.toString() ?: ""
        } else ""

        if (text.isBlank()) {
            viewModelScope.launch {
                _statusMessage.emit("Clipboard HP kamu masih kosong")
            }
            return
        }
        sendTextToPcClipboard(text)
    }

    // === In-App OTA Update ===

    fun checkForAppUpdate(isManualCheck: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val versionInfo = apiService?.getAppVersion()
                // If PC has version > installed version
                if (versionInfo != null && versionInfo.versionCode > BuildConfig.VERSION_CODE) {
                    _availableUpdate.value = versionInfo
                    notificationManager.notifyUpdateAvailable(
                        versionCode = versionInfo.versionCode,
                        versionName = versionInfo.versionName,
                        changelog = versionInfo.changelog
                    )
                    _statusMessage.emit("Pembaruan v${versionInfo.versionName} tersedia!")
                } else if (isManualCheck) {
                    _statusMessage.emit("Aplikasi sudah versi terbaru (v${BuildConfig.VERSION_NAME})")
                }
            } catch (e: Exception) {
                if (isManualCheck) {
                    _statusMessage.emit("Gagal memeriksa update: ${e.message}")
                }
            }
        }
    }

    fun downloadAndInstallUpdate(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isDownloadingUpdate.value = true
            _updateProgress.value = 0.05f
            try {
                _statusMessage.emit("Downloading Nexora update from PC...")
                val body = apiService?.downloadLatestApk() ?: throw Exception("Cannot download APK from PC")
                val updateFile = File(context.cacheDir, "Nexora_update.apk")
                if (updateFile.exists()) updateFile.delete()

                val totalBytes = body.contentLength().coerceAtLeast(1L)
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(updateFile)
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var downloaded = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    _updateProgress.value = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                _updateProgress.value = 1f
                _statusMessage.emit("Download complete. Opening installer...")

                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    updateFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                _statusMessage.emit("Update failed: ${e.message}")
            } finally {
                _isDownloadingUpdate.value = false
                _updateProgress.value = 0f
            }
        }
    }
}
