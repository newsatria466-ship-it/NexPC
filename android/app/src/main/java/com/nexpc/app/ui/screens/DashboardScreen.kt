package com.nexpc.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import android.content.ClipboardManager as AndroidClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.NavigationTab
import com.nexpc.app.ui.components.*
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (NavigationTab) -> Unit = {}
) {
    val status by viewModel.status.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val updateProgress by viewModel.updateProgress.collectAsState()
    val transferHistory by viewModel.transferHistory.collectAsState()
    val laptopStatus by viewModel.laptopStatus.collectAsState()
    val context = LocalContext.current

    var pendingAction by remember { mutableStateOf<String?>(null) }
    var actionTitle by remember { mutableStateOf("") }
    var actionDesc by remember { mutableStateOf("") }
    var actionColor by remember { mutableStateOf(PrimaryIndigo) }
    var actionIcon by remember { mutableStateOf(Icons.Default.PowerSettingsNew) }

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    val hostname = status.hostname.ifEmpty { "Arch-PC" }
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    val usedGb = status.ram.usedMb / 1024f
    val totalGb = status.ram.totalMb / 1024f
    val temp = status.cpu.temperature
    val firstPartition = status.disk.partitions.firstOrNull()
    val usedDiskGb = firstPartition?.usedGb?.toInt() ?: 0
    val totalDiskGb = firstPartition?.totalGb?.toInt() ?: 0

    val downSpeed = if (status.network.downloadKbps >= 1024f) {
        String.format("%.1f MB/s", status.network.downloadKbps / 1024f)
    } else {
        String.format("%.0f KB/s", status.network.downloadKbps)
    }
    val upSpeed = if (status.network.uploadKbps >= 1024f) {
        String.format("%.1f MB/s", status.network.uploadKbps / 1024f)
    } else {
        String.format("%.0f KB/s", status.network.uploadKbps)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── OTA Update banner ───────────────────────────────
        if (availableUpdate != null) {
            item {
                val update = availableUpdate!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimarySoft)
                        .border(1.5.dp, PrimaryBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBubble(Icons.Default.SystemUpdate, tint = Color.White, background = PrimaryIndigo, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Update tersedia ✨", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("Nexora v${update.versionName} · Build #${update.versionCode}", fontSize = 12.sp, color = InkSoft)
                        }
                    }
                    if (update.changelog.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(update.changelog, fontSize = 12.sp, color = InkSoft)
                    }
                    Spacer(Modifier.height(14.dp))
                    if (isDownloadingUpdate) {
                        Text(
                            "Mengunduh… ${(updateProgress * 100).toInt()}%",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryIndigo
                        )
                        Spacer(Modifier.height(6.dp))
                        PillProgress(updateProgress, brush = AuroraHorizontal, track = Color.White)
                    } else {
                        Button(
                            onClick = { viewModel.downloadAndInstallUpdate(context) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download & Install", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // ── Top Greeting with Sun & Cloud Mascot (Reference Style) ──
        item {
            var selectedMode by remember { mutableStateOf(0) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting,",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "${status.hostname.ifEmpty { "Satria" }}! ✨",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryRose
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Kondisi PC adem & siap beraksi ✨",
                            fontSize = 12.sp,
                            color = InkMuted
                        )
                    }
                    SunCloudMascot(size = 58.dp)
                }

                // ── Hero Card: "Today's PC Mood" (Inspired by Today's Skin Mood) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = PrimaryRose.copy(alpha = 0.25f), spotColor = PrimaryRose.copy(alpha = 0.25f))
                        .clip(RoundedCornerShape(26.dp))
                        .background(RoseGradient)
                        .shimmerSweep(highlightColor = Color.White.copy(alpha = 0.22f), durationMillis = 2800)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive 3D Blush Ball Mascot
                        BlushBallMascot(
                            size = 72.dp,
                            isWinking = true,
                            onClick = {
                                viewModel.fetchPcClipboard()
                            }
                        )

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Today's PC Mood",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                            Text(
                                "Looking good! ✨",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Suhu ${temp.toInt()}°C · RAM ${status.ram.percent.toInt()}% · Adem",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                            Spacer(Modifier.height(10.dp))

                            // White Pill CTA Button with Bouncy Tap
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White)
                                    .bounceClick {
                                        viewModel.fetchPcClipboard()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Sinkron Clipboard",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRose
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = PrimaryRose,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // ── Live PC Companion Archie (Continuous Looping Character) ──
                AnimatedPcBuddy(
                    cpuUsage = status.cpu.usagePercent,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.fetchPcClipboard()
                    }
                )

                // ── Segmented Pill Mode Switch (Like Morning/Night in Reference) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceWhite)
                        .border(1.dp, Line, RoundedCornerShape(50))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("☀️ Ringkasan", "⚡ Performa", "🎮 Kontrol").forEachIndexed { index, modeTitle ->
                        val isSel = selectedMode == index
                        val pillBg by animateColorAsState(if (isSel) PrimaryRose else Color.Transparent, tween(200), label = "pillBg")
                        val pillFg by animateColorAsState(if (isSel) Color.White else InkSoft, tween(200), label = "pillFg")

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(50))
                                .background(pillBg)
                                .bounceClick { selectedMode = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = modeTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = pillFg
                            )
                        }
                    }
                }
            }
        }


        // ── 1. Universal Shared Clipboard (Bento Tile Lebar & Besar) ──
        item {
            val pcClipboardText by viewModel.pcClipboardText.collectAsState()
            val isClipboardSyncing by viewModel.isClipboardSyncing.collectAsState()
            var clipboardInputText by remember { mutableStateOf("") }
            var autoSyncEnabled by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                viewModel.fetchPcClipboard()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(SurfaceWhite)
                    .border(1.5.dp, Line, RoundedCornerShape(26.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Universal Shared Clipboard",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                "Kirim & Ambil Instan",
                                fontSize = 11.sp,
                                color = InkSoft
                            )
                        }
                    }

                    // Auto-Sync Switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Auto-Sync", fontSize = 11.sp, color = InkMuted, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(6.dp))
                        Switch(
                            checked = autoSyncEnabled,
                            onCheckedChange = { autoSyncEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Emerald,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = SurfaceSoft
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Input Box Melengkung
                OutlinedTextField(
                    value = clipboardInputText,
                    onValueChange = { clipboardInputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "https://github.com/nexora-project/...",
                            fontSize = 13.sp,
                            color = InkMuted
                        )
                    },
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceSoft,
                        unfocusedContainerColor = SurfaceSoft,
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Line,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    trailingIcon = {
                        if (clipboardInputText.isNotBlank()) {
                            IconButton(onClick = { clipboardInputText = "" }) {
                                Icon(Icons.Default.Close, "Hapus", tint = InkMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Sub-Row: Status + Shortcut Paste dari HP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(EmeraldSoft)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Copied! ✨", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Emerald)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Di PC tinggal tekan Ctrl+V",
                            fontSize = 11.sp,
                            color = InkMuted
                        )
                    }

                    Text(
                        "Tempel dari HP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRose,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .bounceClick {
                                val clipMgr = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? AndroidClipboardManager
                                val item = clipMgr?.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!item.isNullOrBlank()) {
                                    clipboardInputText = item
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Dua Tombol Chunky Tactile Empuk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol Kirim ke PC (Radiant Rose)
                    Row(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(50))
                            .background(PrimaryRose)
                            .bounceClick {
                                val textToSend = if (clipboardInputText.isNotBlank()) {
                                    clipboardInputText
                                } else {
                                    val clipMgr = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? AndroidClipboardManager
                                    clipMgr?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                }
                                viewModel.sendTextToPcClipboard(textToSend)
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kirim ke PC", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Tombol Salin dari PC (Fresh Emerald)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Emerald)
                            .bounceClick {
                                viewModel.copyPcClipboardToPhone(context) { copiedText ->
                                    if (copiedText.isNotBlank()) {
                                        clipboardInputText = copiedText
                                    }
                                }
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Salin dari PC", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // ── 2. Bento Grid Telemetri 2×2 (Character-Rich Mascots) ─────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Baris Atas: CPU & RAM
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricGaugeCard(
                        title = "CPU",
                        percent = status.cpu.usagePercent,
                        valueText = "${status.cpu.usagePercent.toInt()}%",
                        statusText = "Normal & Stabil",
                        accentColor = PrimaryIndigo,
                        mascot = { CpuMascot(size = 38.dp) },
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(NavigationTab.PROCESSES) }
                    )

                    MetricGaugeCard(
                        title = "RAM",
                        percent = status.ram.percent,
                        valueText = "${status.ram.percent.toInt()}%",
                        statusText = if (totalGb > 0f) String.format("%.1f / %.1f GB", usedGb, totalGb) else "—",
                        accentColor = PrimaryIndigo,
                        mascot = { RamMascot(size = 38.dp) },
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(NavigationTab.PROCESSES) }
                    )
                }

                // Baris Bawah: Suhu PC & Penyimpanan
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricNumberCard(
                        title = "Suhu PC",
                        valueText = "${temp.toInt()}°C",
                        statusText = when {
                            temp > 75f -> "Panas 🔥"
                            temp > 60f -> "Hangat"
                            else -> "Aman 👍"
                        },
                        accentColor = if (temp > 75f) CoralDanger else AmberWarm,
                        mascot = { TempMascot(size = 38.dp) },
                        modifier = Modifier.weight(1f)
                    )

                    val freePercent = (100f - status.disk.primaryPercent).coerceIn(0f, 100f)
                    MetricNumberCard(
                        title = "Penyimpanan",
                        valueText = "${freePercent.toInt()}% Free",
                        statusText = if (totalDiskGb > 0) "$usedDiskGb / $totalDiskGb GB" else "Partisi Utama",
                        accentColor = PrimaryIndigo,
                        mascot = { StorageMascot(size = 38.dp) },
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTab(NavigationTab.FILES) }
                    )
                }
            }
        }

        // ── 3. Kecepatan Jaringan (Live Network Speed Tile) ─────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceWhite)
                    .border(1.5.dp, Line, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wifi, null, tint = Emerald, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Network Speed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("WiFi Lokal • Terhubung", fontSize = 11.sp, color = InkSoft)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val arrowTrans = rememberInfiniteTransition(label = "arrowFloat")
                    val upY by arrowTrans.animateFloat(
                        initialValue = -2.5f,
                        targetValue = 2.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "upY"
                    )
                    val downY by arrowTrans.animateFloat(
                        initialValue = 2.5f,
                        targetValue = -2.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(650, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "downY"
                    )

                    // Upload
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            null,
                            tint = PrimaryRose,
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer { translationY = upY }
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(upSpeed, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryRose)
                    }
                    Spacer(Modifier.width(14.dp))
                    // Download
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            null,
                            tint = Emerald,
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer { translationY = downY }
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(downSpeed, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald)
                    }
                }
            }
        }

        // ── 4. Remote Power & Quick Actions (Tactile Chunky Buttons) ─
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(SurfaceWhite)
                    .border(1.5.dp, Line, RoundedCornerShape(26.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🕹️", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Remote Power", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Kendali penuh dari kasur", fontSize = 11.sp, color = InkSoft)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChunkyPowerButton("Lock", Icons.Default.Lock, PrimaryIndigo, Modifier.weight(1f)) {
                        viewModel.lockPc()
                    }
                    ChunkyPowerButton("Sleep", Icons.Default.Bedtime, PrimaryIndigo, Modifier.weight(1f)) {
                        actionTitle = "Tidurkan PC?"
                        actionDesc = "PC akan masuk mode sleep (suspend)."
                        actionColor = PrimaryIndigo
                        actionIcon = Icons.Default.Bedtime
                        pendingAction = "suspend"
                    }
                    ChunkyPowerButton("Restart", Icons.Default.RestartAlt, AmberWarm, Modifier.weight(1f)) {
                        actionTitle = "Restart PC?"
                        actionDesc = "Semua aplikasi akan ditutup dan PC dinyalakan ulang."
                        actionColor = AmberWarm
                        actionIcon = Icons.Default.RestartAlt
                        pendingAction = "reboot"
                    }
                    ChunkyPowerButton("Shutdown", Icons.Default.PowerSettingsNew, CoralDanger, Modifier.weight(1f)) {
                        actionTitle = "Matikan PC?"
                        actionDesc = "PC akan dimatikan sepenuhnya."
                        actionColor = CoralDanger
                        actionIcon = Icons.Default.PowerSettingsNew
                        pendingAction = "shutdown"
                    }
                }
            }
        }

        // ── 5. Media Player Bento Tile ──────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(PrimaryIndigo)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Retro cartoon album art cover
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF818CF8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📻", fontSize = 26.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Media Player", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Retro Cartoon • Audio PC", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Dynamic jumping sound waveform visualization
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val waveTransition = rememberInfiniteTransition(label = "waveAnim")
                        val w1 by waveTransition.animateFloat(12f, 26f, infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "w1")
                        val w2 by waveTransition.animateFloat(24f, 10f, infiniteRepeatable(tween(520, easing = LinearEasing), RepeatMode.Reverse), label = "w2")
                        val w3 by waveTransition.animateFloat(14f, 28f, infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse), label = "w3")
                        val w4 by waveTransition.animateFloat(26f, 16f, infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse), label = "w4")
                        val w5 by waveTransition.animateFloat(10f, 22f, infiniteRepeatable(tween(580, easing = LinearEasing), RepeatMode.Reverse), label = "w5")
                        val w6 by waveTransition.animateFloat(20f, 12f, infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "w6")

                        listOf(w1, w2, w3, w4, w5, w6).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.9f))
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Curved volume control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.controlVolume("mute") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeMute, "Mute", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.controlVolume("down", 5) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = { viewModel.controlVolume("mute") },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(Icons.Default.Pause, "Play/Pause", tint = PrimaryIndigo, modifier = Modifier.size(26.dp))
                        }

                        IconButton(
                            onClick = { viewModel.controlVolume("up", 5) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    IconButton(
                        onClick = { viewModel.controlVolume("up", 5) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, "Vol Up", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ── 6. Pacman Updates & Package Manager ─────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceWhite)
                    .border(1.5.dp, Line, RoundedCornerShape(24.dp))
                    .clickable { onNavigateToTab(NavigationTab.PACKAGES) }
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AmberSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📦", fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Pacman Updates", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(
                            "Paket sistem Arch Linux",
                            fontSize = 11.sp,
                            color = AmberWarm,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { onNavigateToTab(NavigationTab.PACKAGES) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Periksa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // ── 7. Active Apps / Process Manager ─────────────────────────
        item {
            val runningApps = apps.filter { it.isRunning }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceWhite)
                    .border(1.5.dp, Line, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Apps, null, tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Aplikasi Aktif", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("${runningApps.size} aplikasi berjalan di PC", fontSize = 11.sp, color = InkSoft)
                        }
                    }

                    Tag("${runningApps.size} Aktif", color = Color.White, background = PrimaryIndigo)
                }

                if (runningApps.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    runningApps.take(4).forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Terminal, null, tint = InkSoft, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(app.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
                                    Text("Desktop App", fontSize = 10.sp, color = InkMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CoralSoft)
                                    .clickable { onNavigateToTab(NavigationTab.PROCESSES) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, "Kelola", tint = CoralDanger, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog
    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            icon = {
                IconBubble(actionIcon, tint = Color.White, background = actionColor, size = 56.dp, iconSize = 28.dp, shape = CircleShape)
            },
            title = {
                Text(actionTitle, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            },
            text = {
                Text(actionDesc, color = InkSoft, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (pendingAction) {
                            "suspend" -> viewModel.suspendPc()
                            "reboot" -> viewModel.rebootPc()
                            "shutdown" -> viewModel.shutdownPc()
                        }
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Ya, lanjut", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Batal", color = InkSoft, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = SurfaceWhite,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

/**
 * Chunky, tactile circular push button for Remote Power.
 */
@Composable
private fun ChunkyPowerButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.bounceClick { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape, spotColor = color.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkSoft, maxLines = 1)
    }
}
