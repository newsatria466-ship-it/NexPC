package com.nexpc.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.nexpc.app.data.AppNotificationManager
import com.nexpc.app.ui.MainScreen
import com.nexpc.app.ui.screens.ConnectScreen
import com.nexpc.app.ui.theme.NexPCTheme
import com.nexpc.app.viewmodel.ConnectionState
import com.nexpc.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleNotificationIntent(intent)
        requestNotificationPermission()

        // Light status bar icons for light background
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
            NexPCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    val connectionState by viewModel.connectionState.collectAsState()

                    Crossfade(targetState = showSplash, label = "SplashTransition") { isSplash ->
                        if (isSplash) {
                            com.nexpc.app.ui.components.AnimatedSplashScreen(
                                onSplashFinished = { showSplash = false }
                            )
                        } else {
                            Crossfade(
                                targetState = connectionState == ConnectionState.CONNECTED,
                                label = "ConnectionScreenTransition"
                            ) { isConnected ->
                                if (isConnected) {
                                    MainScreen(viewModel = viewModel)
                                } else {
                                    ConnectScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val targetTab = intent?.getStringExtra(AppNotificationManager.EXTRA_TARGET_TAB)
        if (!targetTab.isNullOrEmpty()) {
            viewModel.navigateToTab(targetTab)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
