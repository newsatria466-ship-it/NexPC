package com.nexpc.app.data

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface NexPCApiService {
    @GET("api/status")
    suspend fun getStatus(): SystemStatus

    @GET("api/auth/verify")
    suspend fun verifyAuth(@Header("X-NexPC-Token") token: String): Map<String, Any>

    @GET("api/processes")
    suspend fun getProcesses(
        @Header("X-NexPC-Token") token: String,
        @Query("limit") limit: Int = 60,
        @Query("sort_by") sortBy: String = "cpu",
        @Query("search") search: String? = null
    ): List<ProcessItem>

    @GET("api/processes/{pid}")
    suspend fun getProcessDetail(
        @Path("pid") pid: Int,
        @Header("X-NexPC-Token") token: String
    ): ProcessDetail

    data class KillBody(val force: Boolean = false)

    @POST("api/processes/{pid}/kill")
    suspend fun killProcess(
        @Path("pid") pid: Int,
        @Header("X-NexPC-Token") token: String,
        @Body body: KillBody = KillBody()
    ): SimpleResponse

    @GET("api/apps")
    suspend fun getApps(@Header("X-NexPC-Token") token: String): List<AppItem>

    @POST("api/apps/{desktop_id}/launch")
    suspend fun launchApp(
        @Path("desktop_id") desktopId: String,
        @Header("X-NexPC-Token") token: String
    ): SimpleResponse

    @POST("api/apps/{desktop_id}/stop")
    suspend fun stopApp(
        @Path("desktop_id") desktopId: String,
        @Header("X-NexPC-Token") token: String
    ): SimpleResponse

    @POST("api/control/lock")
    suspend fun lockPc(@Header("X-NexPC-Token") token: String): SimpleResponse

    @POST("api/control/suspend")
    suspend fun suspendPc(@Header("X-NexPC-Token") token: String): SimpleResponse

    @POST("api/control/reboot")
    suspend fun rebootPc(@Header("X-NexPC-Token") token: String): SimpleResponse

    @POST("api/control/shutdown")
    suspend fun shutdownPc(@Header("X-NexPC-Token") token: String): SimpleResponse

    data class VolumeBody(val action: String, val value: Int = 5)

    @POST("api/control/volume")
    suspend fun controlVolume(
        @Header("X-NexPC-Token") token: String,
        @Body body: VolumeBody
    ): SimpleResponse

    @GET("api/packages/updates")
    suspend fun getPackageUpdates(@Header("X-NexPC-Token") token: String): UpdatesResponse

    @GET("api/packages/search")
    suspend fun searchPackages(
        @Header("X-NexPC-Token") token: String,
        @Query("query") query: String
    ): List<PackageSearchResult>

    @POST("api/packages/install")
    suspend fun installPackage(
        @Header("X-NexPC-Token") token: String,
        @Body body: PackageInstallRequest
    ): SimpleResponse

    // === File Transfer & Management Endpoints ===

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(
        @Header("X-NexPC-Token") token: String,
        @Part file: MultipartBody.Part,
        @Part("target_dir") targetDir: RequestBody? = null
    ): UploadResponse

    @GET("api/files/browse")
    suspend fun browseFiles(
        @Header("X-NexPC-Token") token: String,
        @Query("path") path: String? = null
    ): BrowseResponse

    @Streaming
    @GET("api/files/download")
    suspend fun downloadFile(
        @Header("X-NexPC-Token") token: String,
        @Query("path") path: String
    ): ResponseBody

    @POST("api/files/delete")
    suspend fun deleteFile(
        @Header("X-NexPC-Token") token: String,
        @Body body: FileDeleteRequest
    ): SimpleResponse

    @POST("api/files/rename")
    suspend fun renameFile(
        @Header("X-NexPC-Token") token: String,
        @Body body: FileRenameRequest
    ): SimpleResponse

    @POST("api/files/move")
    suspend fun moveFile(
        @Header("X-NexPC-Token") token: String,
        @Body body: FileMoveRequest
    ): SimpleResponse

    @POST("api/files/mkdir")
    suspend fun createFolder(
        @Header("X-NexPC-Token") token: String,
        @Body body: CreateFolderRequest
    ): SimpleResponse

    @GET("api/files/history")
    suspend fun getTransferHistory(
        @Header("X-NexPC-Token") token: String
    ): List<TransferHistoryItem>

    // === Laptop Mode Endpoints ===

    @GET("api/laptop/status")
    suspend fun getLaptopStatus(
        @Header("X-NexPC-Token") token: String
    ): LaptopStatus

    @POST("api/laptop/power-profile")
    suspend fun setPowerProfile(
        @Header("X-NexPC-Token") token: String,
        @Body body: PowerProfileBody
    ): SimpleResponse

    // === Shared Clipboard Endpoints ===

    @GET("api/clipboard")
    suspend fun getClipboard(
        @Header("X-NexPC-Token") token: String
    ): ClipboardResponse

    @POST("api/clipboard")
    suspend fun setClipboard(
        @Header("X-NexPC-Token") token: String,
        @Body body: SetClipboardRequest
    ): SimpleResponse

    // === App OTA Auto-Update Endpoints ===

    @GET("api/app/version")
    suspend fun getAppVersion(): AppVersionInfo

    @Streaming
    @GET("api/app/download")
    suspend fun downloadLatestApk(): ResponseBody
}


object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // longer timeout for file transfer
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    fun createService(host: String, port: Int): NexPCApiService {
        val baseUrl = "http://$host:$port/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NexPCApiService::class.java)
    }
}
