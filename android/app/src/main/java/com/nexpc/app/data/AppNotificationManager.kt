package com.nexpc.app.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexpc.app.MainActivity
import com.nexpc.app.R

class AppNotificationManager(private val context: Context) {

    companion object {
        const val EXTRA_TARGET_TAB = "extra_target_tab"
        const val TAB_HOME = "HOME"
        const val TAB_PACKAGES = "PACKAGES"
        const val TAB_FILES = "FILES"
        const val TAB_LAPTOP = "LAPTOP"
        const val TAB_PROCESSES = "PROCESSES"
        const val TAB_SETTINGS = "SETTINGS"

        private const val CHANNEL_ALERTS = "controlpc_channel_alerts"
        private const val CHANNEL_UPDATES = "controlpc_channel_updates"
        private const val CHANNEL_EVENTS = "controlpc_channel_events"

        private const val NOTIF_ID_LOW_BATTERY = 101
        private const val NOTIF_ID_BATTERY_FULL = 102
        private const val NOTIF_ID_HIGH_TEMP = 103
        private const val NOTIF_ID_APP_UPDATE = 104
        private const val NOTIF_ID_FILE_TRANSFER = 105
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    // Anti-spam & Cooldown state tracking
    private var hasAlertedLowBattery20 = false
    private var hasAlertedLowBattery10 = false
    private var hasAlertedBatteryFull = false
    private var lastHighTempAlertTime = 0L
    private var lastNotifiedUpdateVersion = 0

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Critical PC Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan kritis seperti baterai laptop low atau suhu panas ekstrem"
                enableVibration(true)
            }

            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi ketersediaan update versi baru aplikasi Nexora"
            }

            val eventsChannel = NotificationChannel(
                CHANNEL_EVENTS,
                "PC & Transfer Events",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi transfer file dan baterai terisi penuh"
            }

            systemManager.createNotificationChannels(listOf(alertsChannel, updatesChannel, eventsChannel))
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (!notificationManager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun createPendingIntent(targetTab: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_TARGET_TAB, targetTab)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, targetTab.hashCode(), intent, flags)
    }

    // ==========================================
    // 1. Low Battery Notification (< 20% and < 10%)
    // ==========================================
    fun checkAndNotifyBattery(percent: Float, isPlugged: Boolean) {
        if (isPlugged) {
            // Reset low battery flags when laptop is plugged in
            hasAlertedLowBattery20 = false
            hasAlertedLowBattery10 = false

            // Check if battery reached full charge (100%)
            if (percent >= 100f && !hasAlertedBatteryFull) {
                hasAlertedBatteryFull = true
                showBatteryFullNotification()
            } else if (percent < 95f) {
                hasAlertedBatteryFull = false
            }
            return
        }

        // Laptop is running on battery (discharging)
        hasAlertedBatteryFull = false

        if (percent > 25f) {
            // Reset when battery is above 25%
            hasAlertedLowBattery20 = false
            hasAlertedLowBattery10 = false
            return
        }

        if (percent <= 10f && !hasAlertedLowBattery10) {
            hasAlertedLowBattery10 = true
            hasAlertedLowBattery20 = true
            showNotification(
                id = NOTIF_ID_LOW_BATTERY,
                channelId = CHANNEL_ALERTS,
                title = "🚨 Baterai Laptop Kritis (${percent.toInt()}%)",
                message = "Baterai laptop hampir habis! Segera sambungkan charger sekarang agar PC tidak mati tiba-tiba.",
                targetTab = TAB_LAPTOP,
                priority = NotificationCompat.PRIORITY_MAX
            )
        } else if (percent <= 20f && !hasAlertedLowBattery20) {
            hasAlertedLowBattery20 = true
            showNotification(
                id = NOTIF_ID_LOW_BATTERY,
                channelId = CHANNEL_ALERTS,
                title = "⚠️ Baterai Laptop Lemah (${percent.toInt()}%)",
                message = "Sisa baterai laptop di bawah 20%. Sambungkan ke charger jika perlu melanjutkan aktivitas.",
                targetTab = TAB_LAPTOP,
                priority = NotificationCompat.PRIORITY_HIGH
            )
        }
    }

    // ==========================================
    // 2. Battery Full Notification (100%)
    // ==========================================
    private fun showBatteryFullNotification() {
        showNotification(
            id = NOTIF_ID_BATTERY_FULL,
            channelId = CHANNEL_EVENTS,
            title = "🔋 Baterai Laptop Penuh (100%)",
            message = "Pengisian daya baterai laptop telah selesai. Anda dapat mencabut charger jika ingin menghemat listrik.",
            targetTab = TAB_LAPTOP,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    // ==========================================
    // 3. High CPU Temperature Alert (>= 85°C)
    // ==========================================
    fun checkAndNotifyCpuTemp(temp: Float) {
        val now = System.currentTimeMillis()
        // Cooldown 10 minutes between temp alerts to prevent spam
        if (temp >= 85.0f && (now - lastHighTempAlertTime > 10 * 60 * 1000L)) {
            lastHighTempAlertTime = now
            showNotification(
                id = NOTIF_ID_HIGH_TEMP,
                channelId = CHANNEL_ALERTS,
                title = "🔥 Peringatan Suhu Laptop Panas (${temp.toInt()}°C)",
                message = "Suhu CPU laptop terdeteksi tinggi. Periksa proses yang berat di Process Manager atau ventilasi laptop.",
                targetTab = TAB_PROCESSES,
                priority = NotificationCompat.PRIORITY_HIGH
            )
        }
    }

    // ==========================================
    // 4. App Update Available Alert
    // ==========================================
    fun notifyUpdateAvailable(versionCode: Int, versionName: String, changelog: String) {
        if (lastNotifiedUpdateVersion >= versionCode) return
        lastNotifiedUpdateVersion = versionCode

        showNotification(
            id = NOTIF_ID_APP_UPDATE,
            channelId = CHANNEL_UPDATES,
            title = "🚀 Pembaruan Nexora v$versionName Tersedia!",
            message = if (changelog.isNotEmpty()) changelog else "Tersedia pembaruan baru dari PC Anda. Ketuk untuk download & pasang.",
            targetTab = TAB_SETTINGS,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    // ==========================================
    // 5. File Transfer Completion Alert
    // ==========================================
    fun notifyFileTransfer(fileName: String, isSuccess: Boolean, errorMessage: String? = null) {
        val title = if (isSuccess) "✅ Pengiriman File Berhasil" else "❌ Pengiriman File Gagal"
        val message = if (isSuccess) {
            "File \"$fileName\" telah berhasil ditransfer ke PC."
        } else {
            "Gagal mengirim \"$fileName\": ${errorMessage ?: "Kesalahan jaringan"}"
        }

        showNotification(
            id = NOTIF_ID_FILE_TRANSFER,
            channelId = CHANNEL_EVENTS,
            title = title,
            message = message,
            targetTab = TAB_FILES,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    // Helper method to build and dispatch notification
    private fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        message: String,
        targetTab: String,
        priority: Int
    ) {
        if (!hasNotificationPermission()) return

        try {
            val pendingIntent = createPendingIntent(targetTab)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_nexora_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            // Permission denied at runtime
        } catch (e: Exception) {
            // Ignore system notification errors
        }
    }
}
