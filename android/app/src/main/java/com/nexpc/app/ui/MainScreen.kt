package com.nexpc.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.data.AppNotificationManager
import com.nexpc.app.ui.components.*
import com.nexpc.app.ui.screens.*
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PROCESSES("Apps", Icons.Default.Apps),
    PACKAGES("Pacman", Icons.Default.Inventory2),
    FILES("Files", Icons.Default.Folder),
    LAPTOP("Laptop", Icons.Default.Laptop),
    SETTINGS("Settings", Icons.Default.Settings)
}

/** Per-tab accent matching the Tri-Tone Harmony palette. */
private fun NavigationTab.accent(): Pair<Color, Color> = when (this) {
    NavigationTab.HOME -> PrimaryIndigo to PrimarySoft
    NavigationTab.PROCESSES -> PrimaryIndigo to PrimarySoft
    NavigationTab.PACKAGES -> PrimaryIndigo to PrimarySoft
    NavigationTab.FILES -> Emerald to EmeraldSoft
    NavigationTab.LAPTOP -> Emerald to EmeraldSoft
    NavigationTab.SETTINGS -> PrimaryIndigo to PrimarySoft
}


private fun NavigationTab.subtitle(): String = when (this) {
    NavigationTab.HOME -> "Ringkasan PC kamu"
    NavigationTab.PROCESSES -> "Aplikasi & proses berjalan"
    NavigationTab.PACKAGES -> "Update & install paket"
    NavigationTab.FILES -> "Kirim & kelola file"
    NavigationTab.LAPTOP -> "Baterai & mode daya"
    NavigationTab.SETTINGS -> "Koneksi & aplikasi"
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    val status by viewModel.status.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val requestedTab by viewModel.requestedTab.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val updateProgress by viewModel.updateProgress.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showQuickActionsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(availableUpdate) {
        if (availableUpdate != null) {
            showUpdateDialog = true
        }
    }

    // Handle deep navigation from system notifications
    LaunchedEffect(requestedTab) {
        requestedTab?.let { tabStr ->
            val target = when (tabStr) {
                AppNotificationManager.TAB_HOME -> NavigationTab.HOME
                AppNotificationManager.TAB_PACKAGES -> NavigationTab.PACKAGES
                AppNotificationManager.TAB_FILES -> NavigationTab.FILES
                AppNotificationManager.TAB_LAPTOP -> NavigationTab.LAPTOP
                AppNotificationManager.TAB_PROCESSES -> NavigationTab.PROCESSES
                AppNotificationManager.TAB_SETTINGS -> NavigationTab.SETTINGS
                else -> null
            }
            if (target != null) {
                selectedTab = target
            }
            viewModel.clearRequestedTab()
        }
    }

    // Live Clock State
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeStr = timeFmt.format(now)
            currentDateStr = dateFmt.format(now)
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.statusMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val hostname = status.hostname.ifEmpty { "Satria-PC" }
    val ip = status.network.interfaces.firstOrNull()?.ip ?: "—"
    val isWindows = status.isWindows()

    val screenContent: @Composable (NavigationTab) -> Unit = { tab ->
        if (isWindows) {
            when (tab) {
                NavigationTab.HOME -> WindowsDashboardScreen(viewModel, onNavigateToTab = { selectedTab = it })
                NavigationTab.FILES -> FilesScreen(viewModel)
                NavigationTab.LAPTOP -> LaptopScreen(viewModel)
                NavigationTab.SETTINGS -> SettingsScreen(viewModel)
                else -> WindowsDashboardScreen(viewModel, onNavigateToTab = { selectedTab = it })
            }
        } else {
            when (tab) {
                NavigationTab.HOME -> DashboardScreen(viewModel, onNavigateToTab = { selectedTab = it })
                NavigationTab.PACKAGES -> PackageManagerScreen(viewModel)
                NavigationTab.FILES -> FilesScreen(viewModel)
                NavigationTab.LAPTOP -> LaptopScreen(viewModel)
                NavigationTab.PROCESSES -> ProcessScreen(viewModel)
                NavigationTab.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isWindows) NeoCanvas else CanvasBg)
    ) {
        val isWideScreen = maxWidth >= 760.dp

        if (isWideScreen) {
            // ═══════════════════════════════════════════════════════
            // Tablet / Wide Layout — Aurora sidebar
            // ═══════════════════════════════════════════════════════
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceWhite)
                        .border(1.dp, Line, RoundedCornerShape(28.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 26.dp, top = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.nexpc.app.R.drawable.ic_nexora_logo),
                                contentDescription = "Nexora Logo",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Nexora", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                                Text("Your PC · In Your Hand", fontSize = 11.sp, color = InkMuted)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            NavigationTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                val (accent, accentBg) = tab.accent()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) accentBg else Color.Transparent)
                                        .clickable { selectedTab = tab }
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconBubble(
                                        icon = tab.icon,
                                        tint = if (isSelected) Color.White else accent,
                                        background = if (isSelected) accent else accentBg,
                                        size = 34.dp,
                                        iconSize = 18.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = tab.title,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Ink else InkSoft,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (tab == NavigationTab.SETTINGS && availableUpdate != null) {
                                        Box(Modifier.size(8.dp).clip(CircleShape).background(Coral))
                                    }
                                }
                            }
                        }
                    }

                    // Connected PC card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(AuroraGradient)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(Color.White))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PC Online", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(hostname, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                        Text(
                            "${status.os.ifEmpty { "Arch Linux" }} · ${status.desktopEnv.ifEmpty { "KDE Plasma" }}",
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1
                        )
                        Text(ip, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Top header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(selectedTab.title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                            Text(selectedTab.subtitle(), fontSize = 13.sp, color = InkMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(currentTimeStr, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                            Text(currentDateStr, fontSize = 11.sp, color = InkMuted)
                        }
                        Spacer(Modifier.width(14.dp))
                        BellButton(hasUpdate = availableUpdate != null) { selectedTab = NavigationTab.SETTINGS }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Crossfade(targetState = selectedTab, animationSpec = tween(180), label = "tabWide") { tab ->
                            screenContent(tab)
                        }
                    }
                }
            }
        } else {
            // ═══════════════════════════════════════════════════════
            // Mobile Layout — Aurora header + floating pill nav
            // ═══════════════════════════════════════════════════════
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (isWindows) {
                        WindowsMobileHeader(
                            tab = selectedTab,
                            hostname = hostname,
                            hasUpdate = availableUpdate != null,
                            onBack = { selectedTab = NavigationTab.HOME },
                            onSettings = { selectedTab = NavigationTab.SETTINGS }
                        )
                    } else {
                        MobileHeader(
                            tab = selectedTab,
                            hostname = hostname,
                            hasUpdate = availableUpdate != null,
                            onBack = { selectedTab = NavigationTab.HOME },
                            onBell = { selectedTab = NavigationTab.SETTINGS }
                        )
                    }
                },
                bottomBar = {
                    if (isWindows) {
                        WindowsNeoNavBar(
                            selected = selectedTab,
                            hasUpdate = availableUpdate != null,
                            onSelect = { selectedTab = it }
                        )
                    } else {
                        FloatingNavBar(
                            selected = selectedTab,
                            hasUpdate = availableUpdate != null,
                            onSelect = { selectedTab = it },
                            onFabClick = { showQuickActionsSheet = true }
                        )
                    }
                },
                containerColor = if (isWindows) NeoCanvas else CanvasBg
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Crossfade(targetState = selectedTab, animationSpec = tween(180), label = "tabMobile") { tab ->
                        screenContent(tab)
                    }
                }
            }
        }
    }

    if (showUpdateDialog && availableUpdate != null) {
        val update = availableUpdate!!
        AlertDialog(
            onDismissRequest = { if (!isDownloadingUpdate) showUpdateDialog = false },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(SkyBg), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Sky, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Update Tersedia!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("Versi terbaru v${update.versionName} tersedia untuk diunduh.", fontSize = 13.sp, color = TextSecondary)
                    if (update.changelog.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(update.changelog, fontSize = 12.sp, color = TextPrimary)
                    }
                    if (isDownloadingUpdate) {
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { updateProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Sky,
                            trackColor = SurfaceElevated
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Mengunduh... ${(updateProgress * 100).toInt()}%", fontSize = 11.sp, color = TextMuted)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.downloadAndInstallUpdate(context) },
                    enabled = !isDownloadingUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = Sky, disabledContainerColor = SkyBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isDownloadingUpdate) "Mengunduh..." else "Update Sekarang", fontWeight = FontWeight.SemiBold, color = if (isDownloadingUpdate) Sky else Color.White)
                }
            },
            dismissButton = {
                if (!isDownloadingUpdate) {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Nanti Saja", color = TextSecondary)
                    }
                }
            }
        )
    }

    if (showQuickActionsSheet) {
        AlertDialog(
            onDismissRequest = { showQuickActionsSheet = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(RoseSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Aksi Cepat", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                        Text("Pintasan instan untuk PC kamu", fontSize = 11.sp, color = InkMuted)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Item 1: Pacman Package Updates
                    QuickActionTile(
                        title = "Pacman Updates",
                        subtitle = "Cek & install update paket Linux",
                        icon = Icons.Default.Inventory2,
                        accent = SunYellow,
                        accentBg = SunYellowBg
                    ) {
                        showQuickActionsSheet = false
                        selectedTab = NavigationTab.PACKAGES
                    }

                    // Item 2: Laptop Battery & Power
                    QuickActionTile(
                        title = "Mode Daya Laptop",
                        subtitle = "Baterai, mode performa & charging",
                        icon = Icons.Default.Laptop,
                        accent = Emerald,
                        accentBg = EmeraldSoft
                    ) {
                        showQuickActionsSheet = false
                        selectedTab = NavigationTab.LAPTOP
                    }

                    // Item 3: Lock Screen
                    QuickActionTile(
                        title = "Kunci Layar PC",
                        subtitle = "Amankan PC seketika",
                        icon = Icons.Default.Lock,
                        accent = PrimaryRose,
                        accentBg = RoseSoft
                    ) {
                        viewModel.lockPc()
                        showQuickActionsSheet = false
                    }

                    // Item 4: Sinkronkan Clipboard
                    QuickActionTile(
                        title = "Ambil Clipboard PC",
                        subtitle = "Salin teks terbaru PC ke ponsel",
                        icon = Icons.Default.ContentPaste,
                        accent = PastelLavender,
                        accentBg = PastelLavenderBg
                    ) {
                        viewModel.copyPcClipboardToPhone(context)
                        showQuickActionsSheet = false
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQuickActionsSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceSoft)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .bounceClick { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(subtitle, fontSize = 11.sp, color = InkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = InkMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun BellButton(hasUpdate: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(SurfaceWhite)
            .border(1.dp, Line, CircleShape)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = if (hasUpdate) Indigo else InkSoft,
            modifier = Modifier.size(20.dp)
        )
        if (hasUpdate) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 9.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Coral)
            )
        }
    }
}

@Composable
private fun MobileHeader(
    tab: NavigationTab,
    hostname: String,
    hasUpdate: Boolean,
    onBack: () -> Unit,
    onBell: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CanvasBg)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tab == NavigationTab.HOME) {
            Image(
                painter = painterResource(id = com.nexpc.app.R.drawable.ic_nexora_logo),
                contentDescription = "Nexora Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("nexora", fontSize = 22.sp, fontWeight = FontWeight.Black, color = PrimaryIndigo, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.width(3.dp))
                    Text("✨", fontSize = 16.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Emerald))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "$hostname • Online",
                        fontSize = 11.sp, color = InkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
 else {
            val (accent, accentBg) = tab.accent()
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(1.dp, Line, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Home", tint = Ink, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            IconBubble(tab.icon, tint = accent, background = accentBg, size = 42.dp, iconSize = 21.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(tab.title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(tab.subtitle(), fontSize = 12.sp, color = InkMuted, maxLines = 1)
            }
        }
        BellButton(hasUpdate = hasUpdate, onClick = onBell)
    }
}

@Composable
private fun FloatingNavBar(
    selected: NavigationTab,
    hasUpdate: Boolean,
    onSelect: (NavigationTab) -> Unit,
    onFabClick: () -> Unit
) {
    val leftTabs = listOf(NavigationTab.HOME, NavigationTab.PROCESSES)
    val rightTabs = listOf(NavigationTab.FILES, NavigationTab.SETTINGS)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = PrimaryRose.copy(alpha = 0.20f),
                    spotColor = PrimaryRose.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(50))
                .background(SurfaceWhite)
                .border(1.dp, Line, RoundedCornerShape(50))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left tabs (Home, Apps)
            leftTabs.forEach { tab ->
                NavTabItem(tab = tab, isSelected = tab == selected, hasUpdate = false) {
                    onSelect(tab)
                }
            }

            // Center Prominent Rose Pink Circular Floating Action Button (+)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(8.dp, CircleShape, ambientColor = PrimaryRose.copy(alpha = 0.35f), spotColor = PrimaryRose.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(RoseGradient)
                    .bounceClick { onFabClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Aksi Cepat",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right tabs (Files, Settings)
            rightTabs.forEach { tab ->
                NavTabItem(
                    tab = tab,
                    isSelected = tab == selected,
                    hasUpdate = tab == NavigationTab.SETTINGS && hasUpdate
                ) {
                    onSelect(tab)
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavTabItem(
    tab: NavigationTab,
    isSelected: Boolean,
    hasUpdate: Boolean,
    onClick: () -> Unit
) {
    val weight by animateFloatAsState(
        if (isSelected) 1.5f else 1f,
        spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "navTabWeight"
    )
    val bg by animateColorAsState(if (isSelected) RoseSoft else Color.Transparent, tween(200), label = "navTabBg")
    val fg by animateColorAsState(if (isSelected) PrimaryRose else InkMuted, tween(200), label = "navTabFg")

    Row(
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .bounceClick { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Icon(tab.icon, contentDescription = tab.title, tint = fg, modifier = Modifier.size(20.dp))
            if (hasUpdate && !isSelected) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(CoralDanger)
                )
            }
        }
        if (isSelected) {
            Spacer(Modifier.width(6.dp))
            Text(
                tab.title,
                color = PrimaryRose,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Neo-Brutalist Navigation & Header for Windows Mode
// ═══════════════════════════════════════════════════════

@Composable
private fun WindowsMobileHeader(
    tab: NavigationTab,
    hostname: String,
    hasUpdate: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoCanvas)
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (tab != NavigationTab.HOME) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(NeoWhite, RoundedCornerShape(10.dp))
                        .border(2.dp, NeoBlack, RoundedCornerShape(10.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(NeoYellow, RoundedCornerShape(10.dp))
                        .border(2.dp, NeoBlack, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪟", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = if (tab == NavigationTab.HOME) hostname else tab.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeoBlack
                )
                Text(
                    text = if (tab == NavigationTab.HOME) "Windows Mode" else "NexPC // Windows",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeoBlack.copy(alpha = 0.6f)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            NeoBadge(
                text = "WIN",
                backgroundColor = NeoLime,
                textColor = NeoBlack
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(NeoWhite, RoundedCornerShape(10.dp))
                    .border(2.dp, NeoBlack, RoundedCornerShape(10.dp))
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NeoBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WindowsNeoNavBar(
    selected: NavigationTab,
    hasUpdate: Boolean,
    onSelect: (NavigationTab) -> Unit
) {
    val windowsTabs = listOf(
        NavigationTab.HOME,
        NavigationTab.FILES,
        NavigationTab.LAPTOP,
        NavigationTab.SETTINGS
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Shadow Box
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(NeoBlack, RoundedCornerShape(16.dp))
        )
        // Foreground Dock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeoWhite, RoundedCornerShape(16.dp))
                .border(2.5.dp, NeoBlack, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            windowsTabs.forEach { tab ->
                val isSelected = selected == tab
                val tabBg = when {
                    isSelected && tab == NavigationTab.HOME -> NeoYellow
                    isSelected && tab == NavigationTab.FILES -> NeoCyan
                    isSelected && tab == NavigationTab.LAPTOP -> NeoLime
                    isSelected && tab == NavigationTab.SETTINGS -> NeoPurple
                    else -> Color.Transparent
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(tabBg)
                        .then(
                            if (isSelected) Modifier.border(2.dp, NeoBlack, RoundedCornerShape(10.dp)) else Modifier
                        )
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = NeoBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.title.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = NeoBlack
                        )
                    }
                }
            }
        }
    }
}
