package com.nexpc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.components.GlassCard
import com.nexpc.app.ui.components.ActionBubble
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val status by viewModel.status.collectAsState()
    val host by viewModel.hostInput.collectAsState()
    val port by viewModel.portInput.collectAsState()
    val token by viewModel.tokenInput.collectAsState()
    val isBgMonitoring by viewModel.isBgMonitoringEnabled.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connection credentials, updates, and system information.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // 1. Connection Status Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(IndigoBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = Indigo, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsRow("Connected Host", "$host:$port")
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsRow("Host Machine", status.hostname.ifEmpty { "Arch Linux" })
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsRow("Password / PIN", if (token.isNotEmpty()) "••••••••" else "(None)")
                }
            }
        }

        // 2. Background Monitoring Toggle
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MintBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Mint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Background Monitoring",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tetap pantau baterai & suhu di luar aplikasi",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Switch(
                            checked = isBgMonitoring,
                            onCheckedChange = { viewModel.setBackgroundMonitoring(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BlueAccent
                            )
                        )
                    }
                }
            }
        }

        // 3. Disconnect Action
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CoralBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, tint = Coral, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Session",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionBubble(
                            icon = Icons.Default.LinkOff,
                            label = "Disconnect",
                            onClick = { viewModel.disconnect() },
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        ActionBubble(
                            icon = Icons.Default.DeleteForever,
                            label = "Unpair PC",
                            onClick = { viewModel.unpair() },
                            color = Coral,
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }
        }

        // 3. App Updates (LAN OTA)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SkyBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Sky, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "App Updates",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Installed Version", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("v${com.nexpc.app.BuildConfig.VERSION_NAME} (Build #${com.nexpc.app.BuildConfig.VERSION_CODE})", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }

                        ActionBubble(
                            icon = Icons.Default.Refresh,
                            label = "Check",
                            onClick = { viewModel.checkForAppUpdate(isManualCheck = true) },
                            color = Sky,
                            modifier = Modifier
                        )
                    }
                }
            }
        }

        // 4. About
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nexora v${com.nexpc.app.BuildConfig.VERSION_NAME}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Arch Linux Remote Control & Monitoring",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
