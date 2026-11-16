package com.nexpc.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.NavigationTab
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun WindowsDashboardScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (NavigationTab) -> Unit
) {
    val status by viewModel.status.collectAsState()
    val laptopStatus by viewModel.laptopStatus.collectAsState()
    val transferHistory by viewModel.transferHistory.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val context = LocalContext.current

    val battery = laptopStatus.battery
    var clipboardInputText by remember { mutableStateOf("") }
    var lastFetchedClipboard by remember { mutableStateOf("") }
    var isFetchingClipboard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadLaptopStatus()
        viewModel.loadTransferHistory()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoCanvas),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Top Neo Header Card ─────────────────────────────
        item {
            NeoCard(
                backgroundColor = NeoYellow,
                cornerRadius = 16.dp,
                shadowOffset = 5.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🪟",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WINDOWS PC",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeoBlack
                            )
                        }

                        NeoBadge(
                            text = "ONLINE",
                            backgroundColor = NeoLime,
                            textColor = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = status.hostname.ifEmpty { "Windows-PC" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = NeoBlack
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = status.os.ifEmpty { "Windows 10/11" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeoBlack.copy(alpha = 0.75f)
                        )
                        Text(
                            text = status.network.interfaces.firstOrNull()?.ip ?: "127.0.0.1",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeoBlack.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        // ── 2. Feature 8: OTA Update Banner (If available) ─────
        if (availableUpdate != null) {
            item {
                NeoCard(
                    backgroundColor = NeoPink,
                    cornerRadius = 14.dp,
                    shadowOffset = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = NeoWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UPDATE TERSEDIA: v${availableUpdate?.versionName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeoWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = availableUpdate?.changelog ?: "Pembaruan fitur terbaru untuk Windows & Linux.",
                            fontSize = 12.sp,
                            color = NeoWhite.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NeoButton(
                            onClick = { viewModel.downloadAndInstallUpdate(context) },
                            backgroundColor = NeoWhite,
                            contentColor = NeoBlack
                        ) {
                            Text(
                                text = if (isDownloadingUpdate) "MENGUNDUH..." else "DOWNLOAD & UPDATE",
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Feature 5: Laptop Battery Module ────────────────
        item {
            NeoCard(
                backgroundColor = NeoWhite,
                cornerRadius = 16.dp,
                shadowOffset = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                contentDescription = null,
                                tint = if (battery.isCharging) Color(0xFF15803D) else NeoBlack,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LAPTOP BATTERY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeoBlack
                            )
                        }

                        NeoBadge(
                            text = if (battery.isCharging) "CHARGING ⚡" else "ON BATTERY 🔋",
                            backgroundColor = if (battery.isCharging) NeoLime else NeoOrange,
                            textColor = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${battery.percentage}%",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = NeoBlack
                        )

                        Text(
                            text = battery.timeRemainingStr ?: (if (battery.isCharging) "Charging..." else "Battery active"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeoBlack.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chunky Brutalist Battery Gauge Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(NeoCanvas, RoundedCornerShape(9.dp))
                            .border(2.dp, NeoBlack, RoundedCornerShape(9.dp))
                            .padding(2.dp)
                    ) {
                        val fillFrac = (battery.percentage / 100f).coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillFrac)
                                .fillMaxHeight()
                                .background(
                                    if (battery.percentage < 20) NeoPink else if (battery.isCharging) NeoLime else NeoCyan,
                                    RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
        }

        // ── 4. Feature 3: Clipboard Sync Module ────────────────
        item {
            NeoCard(
                backgroundColor = NeoWhite,
                cornerRadius = 16.dp,
                shadowOffset = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = NeoBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLIPBOARD SYNC",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeoBlack
                            )
                        }

                        NeoBadge(
                            text = "2-WAY SYNC",
                            backgroundColor = NeoCyan,
                            textColor = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Input for sending to Windows Clipboard
                    OutlinedTextField(
                        value = clipboardInputText,
                        onValueChange = { clipboardInputText = it },
                        placeholder = {
                            Text(
                                "Ketik atau paste teks yang mau dikirim ke PC Windows...",
                                fontSize = 12.sp,
                                color = NeoBlack.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .border(2.dp, NeoBlack, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NeoCanvas,
                            unfocusedContainerColor = NeoCanvas,
                            focusedBorderColor = NeoBlack,
                            unfocusedBorderColor = NeoBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Action 1: Send to Windows
                        NeoButton(
                            onClick = {
                                if (clipboardInputText.isNotBlank()) {
                                    viewModel.sendTextToPcClipboard(clipboardInputText) {
                                        clipboardInputText = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoLime,
                            contentColor = NeoBlack
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KIRIM KE PC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Action 2: Get from Windows
                        NeoButton(
                            onClick = {
                                viewModel.copyPcClipboardToPhone(context) { pcText ->
                                    lastFetchedClipboard = pcText
                                }
                            },
                            modifier = Modifier.weight(1f),
                            backgroundColor = NeoYellow,
                            contentColor = NeoBlack
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AMBIL DARI PC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (lastFetchedClipboard.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeoCanvas, RoundedCornerShape(8.dp))
                                .border(1.5.dp, NeoBlack, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "HASIL CLIPBOARD WINDOWS (TELAH DI-COPY):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeoBlack.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastFetchedClipboard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NeoBlack,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 5. Feature 2: File Transfer & Manager Quick Nav ────
        item {
            NeoCard(
                backgroundColor = NeoWhite,
                cornerRadius = 16.dp,
                shadowOffset = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = NeoBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FILE SHARING & STORAGE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeoBlack
                            )
                        }

                        NeoBadge(
                            text = "LAN ONLY",
                            backgroundColor = NeoOrange,
                            textColor = NeoBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Akses dan kirim dokumen, gambar, atau video langsung ke folder Downloads\\NexPC di Windows.",
                        fontSize = 12.sp,
                        color = NeoBlack.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NeoButton(
                        onClick = { onNavigateToTab(NavigationTab.FILES) },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = NeoCyan,
                        contentColor = NeoBlack
                    ) {
                        Icon(Icons.Default.DriveFolderUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BUKA FILE MANAGER WINDOWS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ── 6. Hardware Quick Metrics ──────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeoMetricBox(
                    label = "CPU USAGE",
                    value = "${status.cpu.usagePercent.toInt()}",
                    unit = "%",
                    accentColor = NeoPink,
                    modifier = Modifier.weight(1f)
                )

                val usedRamGb = status.ram.usedMb / 1024f
                val totalRamGb = status.ram.totalMb / 1024f
                NeoMetricBox(
                    label = "RAM USED",
                    value = String.format("%.1f", usedRamGb),
                    unit = "/ ${totalRamGb.toInt()} GB",
                    accentColor = NeoCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

