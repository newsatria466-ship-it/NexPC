package com.nexpc.app.data

import com.google.gson.annotations.SerializedName

data class SystemStatus(
    @SerializedName("status") val status: String = "offline",
    @SerializedName("timestamp") val timestamp: Double = 0.0,
    @SerializedName("hostname") val hostname: String = "",
    @SerializedName("os") val os: String = "",
    @SerializedName("os_family") val osFamily: String = "",
    @SerializedName("uptime_seconds") val uptimeSeconds: Long = 0L,
    @SerializedName("uptime_formatted") val uptimeFormatted: String = "",
    @SerializedName("session_type") val sessionType: String = "",
    @SerializedName("desktop_env") val desktopEnv: String = "",
    @SerializedName("cpu") val cpu: CpuInfo = CpuInfo(),
    @SerializedName("ram") val ram: MemoryInfo = MemoryInfo(),
    @SerializedName("swap") val swap: SwapInfo = SwapInfo(),
    @SerializedName("disk") val disk: DiskRootInfo = DiskRootInfo(),
    @SerializedName("network") val network: NetworkInfo = NetworkInfo(),
    @SerializedName("battery") val battery: BatteryInfo? = null
) {
    fun isWindows(): Boolean = osFamily.equals("windows", ignoreCase = true) || os.contains("windows", ignoreCase = true)
    fun isLinux(): Boolean = !isWindows()
}

data class CpuInfo(
    @SerializedName("usage_percent") val usagePercent: Float = 0f,
    @SerializedName("cores_usage") val coresUsage: List<Float> = emptyList(),
    @SerializedName("cores_count") val coresCount: Int = 0,
    @SerializedName("frequency_mhz") val frequencyMhz: Float = 0f,
    @SerializedName("temperature") val temperature: Float = 0f,
    @SerializedName("all_temperatures") val allTemperatures: Map<String, Float> = emptyMap()
)

data class MemoryInfo(
    @SerializedName("total_mb") val totalMb: Float = 0f,
    @SerializedName("used_mb") val usedMb: Float = 0f,
    @SerializedName("free_mb") val freeMb: Float = 0f,
    @SerializedName("percent") val percent: Float = 0f
)

data class SwapInfo(
    @SerializedName("total_mb") val totalMb: Float = 0f,
    @SerializedName("used_mb") val usedMb: Float = 0f,
    @SerializedName("percent") val percent: Float = 0f
)

data class DiskRootInfo(
    @SerializedName("primary_percent") val primaryPercent: Float = 0f,
    @SerializedName("partitions") val partitions: List<DiskPartition> = emptyList()
)

data class DiskPartition(
    @SerializedName("device") val device: String = "",
    @SerializedName("mountpoint") val mountpoint: String = "",
    @SerializedName("fstype") val fstype: String = "",
    @SerializedName("total_gb") val totalGb: Float = 0f,
    @SerializedName("used_gb") val usedGb: Float = 0f,
    @SerializedName("free_gb") val freeGb: Float = 0f,
    @SerializedName("percent") val percent: Float = 0f
)

data class NetworkInfo(
    @SerializedName("download_kbps") val downloadKbps: Float = 0f,
    @SerializedName("upload_kbps") val uploadKbps: Float = 0f,
    @SerializedName("bytes_recv_total") val bytesRecvTotal: Long = 0L,
    @SerializedName("bytes_sent_total") val bytesSentTotal: Long = 0L,
    @SerializedName("interfaces") val interfaces: List<NetworkInterface> = emptyList()
)

data class NetworkInterface(
    @SerializedName("interface") val iface: String = "",
    @SerializedName("ip") val ip: String = ""
)

data class BatteryInfo(
    @SerializedName("percent") val percent: Float = 0f,
    @SerializedName("power_plugged") val powerPlugged: Boolean = false,
    @SerializedName("secsleft") val secsleft: Long = -1L
)

data class ProcessItem(
    @SerializedName("pid") val pid: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("cpu_percent") val cpuPercent: Float = 0f,
    @SerializedName("memory_percent") val memoryPercent: Float = 0f,
    @SerializedName("memory_mb") val memoryMb: Float = 0f,
    @SerializedName("status") val status: String = ""
)

data class ProcessDetail(
    @SerializedName("pid") val pid: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("exe") val exe: String = "",
    @SerializedName("cmdline") val cmdline: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("cpu_percent") val cpuPercent: Float = 0f,
    @SerializedName("memory_percent") val memoryPercent: Float = 0f,
    @SerializedName("memory_mb") val memoryMb: Float = 0f,
    @SerializedName("status") val status: String = "",
    @SerializedName("num_threads") val numThreads: Int = 0
)

data class AppItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("exec") val exec: String = "",
    @SerializedName("binary") val binary: String = "",
    @SerializedName("icon") val icon: String = "",
    @SerializedName("categories") val categories: List<String> = emptyList(),
    @SerializedName("is_running") val isRunning: Boolean = false
)

data class PackageUpdate(
    @SerializedName("package") val pkg: String = "",
    @SerializedName("old_version") val oldVersion: String = "",
    @SerializedName("new_version") val newVersion: String = ""
)

data class UpdatesResponse(
    @SerializedName("total_updates") val totalUpdates: Int = 0,
    @SerializedName("updates") val updates: List<PackageUpdate> = emptyList()
)

data class PackageSearchResult(
    @SerializedName("source") val source: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("version") val version: String = "",
    @SerializedName("installed") val installed: Boolean = false,
    @SerializedName("description") val description: String = ""
)

data class SimpleResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("status") val status: String = "",
    @SerializedName("message") val message: String = ""
)


data class PackageInstallRequest(
    @SerializedName("package_name") val packageName: String
)

data class DiscoveredDevice(
    val name: String,
    val host: String,
    val port: Int,
    val os: String = "Arch Linux",
    val osFamily: String = "",
    val requiresAuth: Boolean = true
) {
    fun isWindows(): Boolean = osFamily.equals("windows", ignoreCase = true) || os.contains("windows", ignoreCase = true)
    fun isLinux(): Boolean = !isWindows()
}

// === File Transfer Models ===

data class FileItem(
    @SerializedName("name") val name: String = "",
    @SerializedName("path") val path: String = "",
    @SerializedName("is_dir") val isDir: Boolean = false,
    @SerializedName("size_bytes") val sizeBytes: Long = 0L,
    @SerializedName("size_formatted") val sizeFormatted: String = "",
    @SerializedName("modified") val modified: Long = 0L
)

data class AllowedRoot(
    @SerializedName("name") val name: String = "",
    @SerializedName("path") val path: String = ""
)

data class BrowseResponse(
    @SerializedName("current_path") val currentPath: String = "",
    @SerializedName("parent_path") val parentPath: String? = null,
    @SerializedName("allowed_roots") val allowedRoots: List<AllowedRoot> = emptyList(),
    @SerializedName("items") val items: List<FileItem> = emptyList()
)

data class UploadResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("filename") val filename: String = "",
    @SerializedName("path") val path: String = "",
    @SerializedName("size_bytes") val sizeBytes: Long = 0L,
    @SerializedName("size_formatted") val sizeFormatted: String = ""
)

data class TransferHistoryItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("filename") val filename: String = "",
    @SerializedName("size_bytes") val sizeBytes: Long = 0L,
    @SerializedName("size_formatted") val sizeFormatted: String = "",
    @SerializedName("timestamp") val timestamp: Double = 0.0,
    @SerializedName("time_formatted") val timeFormatted: String = "",
    @SerializedName("direction") val direction: String = "upload",
    @SerializedName("status") val status: String = "completed"
)

data class FileDeleteRequest(
    @SerializedName("path") val path: String
)

data class FileRenameRequest(
    @SerializedName("path") val path: String,
    @SerializedName("new_name") val newName: String
)

data class FileMoveRequest(
    @SerializedName("source_path") val sourcePath: String,
    @SerializedName("target_dir") val targetDir: String
)

data class CreateFolderRequest(
    @SerializedName("parent_path") val parentPath: String,
    @SerializedName("name") val name: String
)

// === Laptop Mode Models ===

data class BatteryDetails(
    @SerializedName("is_present") val isPresent: Boolean = false,
    @SerializedName("percentage") val percentage: Int = 0,
    @SerializedName("state") val state: String = "unknown",
    @SerializedName("is_charging") val isCharging: Boolean = false,
    @SerializedName("health_percent") val healthPercent: Float? = null,
    @SerializedName("time_remaining_str") val timeRemainingStr: String? = null,
    @SerializedName("time_remaining_seconds") val timeRemainingSeconds: Long? = null,
    @SerializedName("wattage") val wattage: Float? = null,
    @SerializedName("cycle_count") val cycleCount: Int? = null
)

data class PowerProfilesInfo(
    @SerializedName("supported") val supported: Boolean = false,
    @SerializedName("active_profile") val activeProfile: String = "balanced",
    @SerializedName("available_profiles") val availableProfiles: List<String> = emptyList(),
    @SerializedName("backend") val backend: String = ""
)

data class LaptopHardware(
    @SerializedName("cpu_usage_percent") val cpuUsagePercent: Float = 0f,
    @SerializedName("cpu_temp") val cpuTemp: Float = 0f,
    @SerializedName("cpu_freq_mhz") val cpuFreqMhz: Float = 0f,
    @SerializedName("ram_percent") val ramPercent: Float = 0f,
    @SerializedName("ram_used_mb") val ramUsedMb: Float = 0f,
    @SerializedName("ram_total_mb") val ramTotalMb: Float = 0f
)

data class LaptopStatus(
    @SerializedName("battery") val battery: BatteryDetails = BatteryDetails(),
    @SerializedName("power_profiles") val powerProfiles: PowerProfilesInfo = PowerProfilesInfo(),
    @SerializedName("hardware") val hardware: LaptopHardware = LaptopHardware()
)

data class PowerProfileBody(
    @SerializedName("profile") val profile: String
)

data class AppVersionInfo(
    @SerializedName("version_code") val versionCode: Int = 1,
    @SerializedName("version_name") val versionName: String = "1.0.0",
    @SerializedName("changelog") val changelog: String = "",
    @SerializedName("apk_available") val apkAvailable: Boolean = false,
    @SerializedName("apk_size") val apkSize: Long = 0L
)

// === Clipboard Models ===

data class ClipboardResponse(
    @SerializedName("text") val text: String = "",
    @SerializedName("status") val status: String = "ok",
    @SerializedName("length") val length: Int = 0,
    @SerializedName("message") val message: String = ""
)

data class SetClipboardRequest(
    @SerializedName("text") val text: String
)

