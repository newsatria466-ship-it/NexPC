package com.nexpc.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.data.DiscoveredDevice
import com.nexpc.app.ui.components.GlassCard
import com.nexpc.app.ui.components.ActionBubble
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.ConnectionState
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun ConnectScreen(viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()
    val discoveredDevices by viewModel.discoveryManager.discoveredDevices.collectAsState()
    val isSearching by viewModel.discoveryManager.isSearching.collectAsState()
    val hasSearched by viewModel.discoveryManager.hasSearched.collectAsState()

    val host by viewModel.hostInput.collectAsState()
    val port by viewModel.portInput.collectAsState()
    val token by viewModel.tokenInput.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var selectedDeviceForPairing by remember { mutableStateOf<DiscoveredDevice?>(null) }
    var pinInputValue by remember { mutableStateOf(viewModel.prefs.savedToken.ifEmpty { "0909" }) }
    var isPinVisible by remember { mutableStateOf(false) }

    var showManualSettings by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.startDiscovery()
    }

    LaunchedEffect(Unit) {
        viewModel.statusMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgLight
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // App Brand Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = com.nexpc.app.R.drawable.ic_nexora_logo),
                        contentDescription = "Nexora Logo",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Nexora",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Your PC · In Your Hand",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error Notification Banner
                AnimatedVisibility(visible = connectionState == ConnectionState.ERROR || connectionError != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        backgroundColor = CrimsonDanger.copy(alpha = 0.12f),
                        borderColor = CrimsonDanger.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = CrimsonDanger,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gagal Terhubung",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonDanger
                                )
                                Text(
                                    text = connectionError ?: "Periksa koneksi Wi-Fi atau PIN Anda (default: 0909)",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            TextButton(
                                onClick = {
                                    if (discoveredDevices.isNotEmpty()) {
                                        selectedDeviceForPairing = discoveredDevices.first()
                                        pinInputValue = "0909"
                                        showPinDialog = true
                                    } else {
                                        viewModel.startDiscovery()
                                    }
                                }
                            ) {
                                Text("Input PIN", fontSize = 12.sp, color = BlueAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ==========================================
                // DISCOVERY FLOW STATES
                // ==========================================

                if (isSearching && discoveredDevices.isEmpty()) {
                    // STATE 1: Searching for your PC
                    SearchingView(onManualOpen = { showManualSettings = !showManualSettings })
                } else if (discoveredDevices.isNotEmpty()) {
                    // STATE 2: Device(s) Found
                    DeviceFoundView(
                        devices = discoveredDevices,
                        isSearching = isSearching,
                        onRefresh = { viewModel.startDiscovery() },
                        onConnectClick = { dev ->
                            selectedDeviceForPairing = dev
                            pinInputValue = viewModel.prefs.savedToken.ifEmpty { "0909" }
                            showPinDialog = true
                        }
                    )
                } else if (hasSearched) {
                    // STATE 3: PC Not Found
                    DeviceNotFoundView(
                        onRetry = { viewModel.startDiscovery() },
                        onManualOpen = { showManualSettings = true }
                    )
                } else {
                    // Default search trigger
                    SearchingView(onManualOpen = { showManualSettings = !showManualSettings })
                }

                Spacer(modifier = Modifier.weight(1f))

                // Manual Connection Fallback Toggle
                TextButton(
                    onClick = { showManualSettings = !showManualSettings },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = if (showManualSettings) Icons.Default.ExpandLess else Icons.Default.Tune,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showManualSettings) "Sembunyikan Atur Manual" else "Atur IP Manual",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            AnimatedVisibility(visible = showManualSettings) {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Koneksi Manual", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = host,
                            onValueChange = { viewModel.hostInput.value = it },
                            label = { Text("IP Laptop", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = port,
                                onValueChange = { viewModel.portInput.value = it },
                                label = { Text("Port", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = token,
                                onValueChange = { viewModel.tokenInput.value = it },
                                label = { Text("PIN (0909)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.connect() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                        ) {
                            Text("Connect Manual", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // PIN Pairing Dialog
        if (showPinDialog && selectedDeviceForPairing != null) {
            val dev = selectedDeviceForPairing!!
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                containerColor = SurfaceElevated,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Pairing PIN", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(dev.name, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Masukkan PIN otentikasi dari laptop Anda (Default: 0909):",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = pinInputValue,
                            onValueChange = { pinInputValue = it },
                            label = { Text("PIN Pairing") },
                            singleLine = true,
                            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            trailingIcon = {
                                IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                    Icon(
                                        imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPinDialog = false
                            viewModel.pairAndConnect(dev, pinInputValue)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Connect", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("Batal", color = TextSecondary)
                    }
                }
            )
        }

        // Connecting Modal Overlay
        if (connectionState == ConnectionState.CONNECTING) {
            AlertDialog(
                onDismissRequest = { viewModel.disconnect() },
                containerColor = SurfaceElevated,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BlueAccent,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Menghubungkan...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                text = {
                    Text(
                        "Sedang memverifikasi PIN & membangun koneksi ke PC...",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.disconnect() }) {
                        Text("Batal", color = CrimsonDanger, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    }
}
}

// ==========================================
// STATE 1: SEARCHING VIEW
// ==========================================
@Composable
fun SearchingView(onManualOpen: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(BlueSubtle)
                    .border(2.dp, BlueAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = BlueAccent,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Searching for your PC...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Mencari laptop Arch Linux di jaringan Wi-Fi lokal",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = BlueAccent,
                trackColor = SurfaceElevated
            )
        }
    }
}

// ==========================================
// STATE 2: DEVICE FOUND VIEW
// ==========================================
@Composable
fun DeviceFoundView(
    devices: List<DiscoveredDevice>,
    isSearching: Boolean,
    onRefresh: () -> Unit,
    onConnectClick: (DiscoveredDevice) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Found ${devices.size} device${if (devices.size > 1) "s" else ""}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isSearching) BlueAccent else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(devices) { dev ->
                if (dev.isWindows()) {
                    // Neo-Brutalist Card for Windows PC
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = NeoWhite,
                        cornerRadius = 14.dp,
                        shadowOffset = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(NeoYellow, RoundedCornerShape(10.dp))
                                        .border(2.dp, NeoBlack, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🪟", fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dev.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = NeoBlack
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        NeoBadge(
                                            text = "WIN",
                                            backgroundColor = NeoCyan,
                                            textColor = NeoBlack
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${dev.os} • ${dev.host}",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeoBlack.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(NeoLime, RoundedCornerShape(2.dp))
                                                .border(1.dp, NeoBlack, RoundedCornerShape(2.dp))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Online",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = NeoBlack
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            NeoButton(
                                onClick = { onConnectClick(dev) },
                                backgroundColor = NeoLime,
                                contentColor = NeoBlack,
                                cornerRadius = 10.dp,
                                shadowOffset = 3.dp
                            ) {
                                Text(
                                    text = "CONNECT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                } else {
                    // Sleek Modern Card for Linux PC
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BlueSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Computer,
                                        contentDescription = null,
                                        tint = BlueAccent,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dev.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(IndigoBg)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "LINUX",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Indigo
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${dev.os} • ${dev.host}",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Online",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            ActionBubble(
                                icon = Icons.Default.Login,
                                label = "Connect",
                                onClick = { onConnectClick(dev) },
                                color = Indigo
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STATE 3: DEVICE NOT FOUND VIEW
// ==========================================
@Composable
fun DeviceNotFoundView(
    onRetry: () -> Unit,
    onManualOpen: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PC tidak ditemukan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pastikan laptop dan HP terhubung ke Wi-Fi yang sama dan Nexora Agent sedang aktif di laptop Anda.",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionBubble(
                icon = Icons.Default.Refresh,
                label = "Coba Lagi",
                onClick = onRetry,
                color = Indigo
            )
        }
    }
}
