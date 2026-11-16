package com.nexpc.app.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nexpc_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HOST = "saved_host"
        private const val KEY_PORT = "saved_port"
        private const val KEY_TOKEN = "saved_token"
        private const val KEY_DEVICE_NAME = "saved_device_name"
        private const val KEY_BG_MONITORING = "bg_monitoring_enabled"
    }

    var savedHost: String
        get() = prefs.getString(KEY_HOST, "192.168.0.110") ?: "192.168.0.110"
        set(value) = prefs.edit().putString(KEY_HOST, value).apply()

    var savedPort: Int
        get() = prefs.getInt(KEY_PORT, 8765)
        set(value) = prefs.edit().putInt(KEY_PORT, value).apply()

    var savedToken: String
        get() = prefs.getString(KEY_TOKEN, "0909") ?: "0909"
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var savedDeviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, "Arch PC") ?: "Arch PC"
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value).apply()

    var isBackgroundMonitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_BG_MONITORING, true)
        set(value) = prefs.edit().putBoolean(KEY_BG_MONITORING, value).apply()

    var isPaired: Boolean
        get() = prefs.getBoolean("is_paired", false) && savedToken.isNotEmpty()
        set(value) = prefs.edit().putBoolean("is_paired", value).apply()

    fun clearPairing() {
        prefs.edit()
            .putBoolean("is_paired", false)
            .putString(KEY_TOKEN, "")
            .apply()
    }
}
