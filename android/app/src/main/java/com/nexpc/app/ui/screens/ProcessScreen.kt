package com.nexpc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.nexpc.app.data.AppItem
import com.nexpc.app.data.ProcessItem
import com.nexpc.app.ui.components.GlassCard
import com.nexpc.app.ui.components.StatusBadge
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun ProcessScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active Apps, 1 = System Processes

    val apps by viewModel.apps.collectAsState()
    val processes by viewModel.processes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf("cpu") }
    var filterRunningAppsOnly by remember { mutableStateOf(false) }

    var processToKill by remember { mutableStateOf<ProcessItem?>(null) }
    var appToStop by remember { mutableStateOf<AppItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadApps()
        viewModel.loadProcesses()
    }

    LaunchedEffect(searchQuery, selectedSort, selectedTab) {
        if (selectedTab == 1) {
            viewModel.loadProcesses(
                search = if (searchQuery.isNotBlank()) searchQuery else null,
                sortBy = selectedSort
            )
        }
    }

    val runningAppsCount = apps.count { it.isRunning }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 1. Top Segmented Switcher (Active Apps vs All Processes)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 0: Desktop Apps
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 0 },
                    color = if (selectedTab == 0) BlueAccent else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Active Apps",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTab == 0) Color.White else TextSecondary
                        )
                        if (runningAppsCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selectedTab == 0) Color.White.copy(alpha = 0.25f) else GreenSubtle)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$runningAppsCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) Color.White else EmeraldGreen
                                )
                            }
                        }
                    }
                }

                // Tab 1: System Processes
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 1 },
                    color = if (selectedTab == 1) BlueAccent else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "System Processes",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTab == 1) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Search & Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (selectedTab == 0) "Search applications..." else "Filter process name...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (selectedTab == 0) viewModel.loadApps() else viewModel.loadProcesses()
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BlueAccent, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Sub-controls
        if (selectedTab == 0) {
            // Apps filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = !filterRunningAppsOnly,
                        onClick = { filterRunningAppsOnly = false },
                        label = { Text("All Apps (${apps.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueSubtle,
                            selectedLabelColor = BlueAccent
                        ),
                        border = BorderStroke(0.5.dp, if (!filterRunningAppsOnly) BlueAccent else CardBorder)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = filterRunningAppsOnly,
                        onClick = { filterRunningAppsOnly = true },
                        label = { Text("Running Only ($runningAppsCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenSubtle,
                            selectedLabelColor = EmeraldGreen
                        ),
                        border = BorderStroke(0.5.dp, if (filterRunningAppsOnly) EmeraldGreen else CardBorder)
                    )
                }
            }
        } else {
            // Processes sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Sort by:", fontSize = 12.sp, color = TextMuted)
                val sorts = listOf("cpu" to "CPU %", "memory" to "RAM", "name" to "Name")
                sorts.forEach { (key, label) ->
                    val isSelected = selectedSort == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BlueSubtle else SurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isSelected) BlueAccent else CardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedSort = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) BlueAccent else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Content List
        if (selectedTab == 0) {
            // Active Desktop Apps List
            val filteredApps = apps.filter { app ->
                (!filterRunningAppsOnly || app.isRunning) &&
                (searchQuery.isBlank() || app.name.contains(searchQuery, ignoreCase = true) || app.categories.any { it.contains(searchQuery, ignoreCase = true) })
            }

            if (filteredApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No applications found.", fontSize = 13.sp, color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps, key = { it.id }) { app ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    // App icon
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (app.isRunning) GreenSubtle else SurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                app.name.contains("browser", true) || app.name.contains("firefox", true) || app.name.contains("chrome", true) || app.name.contains("brave", true) -> Icons.Default.Language
                                                app.name.contains("code", true) || app.name.contains("studio", true) || app.name.contains("terminal", true) -> Icons.Default.Terminal
                                                app.name.contains("music", true) || app.name.contains("spotify", true) -> Icons.Default.MusicNote
                                                app.name.contains("discord", true) || app.name.contains("chat", true) -> Icons.AutoMirrored.Filled.Chat
                                                else -> Icons.Default.Apps
                                            },
                                            contentDescription = null,
                                            tint = if (app.isRunning) EmeraldGreen else BlueAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = app.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            StatusBadge(
                                                text = if (app.isRunning) "RUNNING" else "CLOSED",
                                                isActive = app.isRunning
                                            )
                                            if (app.categories.isNotEmpty()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = app.categories.first(),
                                                    fontSize = 12.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                if (app.isRunning) {
                                    Button(
                                        onClick = { appToStop = app },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CrimsonSubtle,
                                            contentColor = CrimsonDanger
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Close", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.launchApp(app.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        border = BorderStroke(1.dp, BlueAccent),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueAccent)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Launch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // System Processes List
            if (processes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No processes matching filter.", fontSize = 13.sp, color = TextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(processes, key = { it.pid }) { proc ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = proc.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SurfaceCard)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PID ${proc.pid}",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${proc.cpuPercent}% CPU",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (proc.cpuPercent > 50) CrimsonDanger else BlueAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(" • ", color = TextMuted)
                                        Text(
                                            text = "${proc.memoryMb} MB",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = PurpleAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(" • ", color = TextMuted)
                                        Text(
                                            text = proc.username,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { processToKill = proc },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDanger),
                                    border = BorderStroke(1.dp, CrimsonSubtle),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Kill", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Stopping Desktop App
    if (appToStop != null) {
        val target = appToStop!!
        AlertDialog(
            onDismissRequest = { appToStop = null },
            title = {
                Text(text = "Close Application", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to terminate ${target.name} on your PC?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopApp(target.id)
                        appToStop = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close App", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToStop = null }) {
                    Text("Cancel", color = TextSecondary, fontSize = 14.sp)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Confirmation Dialog for Killing Process
    if (processToKill != null) {
        val target = processToKill!!
        var forceKill by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { processToKill = null },
            title = {
                Text(text = "End Process", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "End process ${target.name} (PID: ${target.pid})?",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CrimsonSubtle)
                            .clickable { forceKill = !forceKill }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = forceKill,
                            onCheckedChange = { forceKill = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CrimsonDanger,
                                uncheckedColor = CrimsonDanger.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            text = "Force Kill (SIGKILL)",
                            color = CrimsonDanger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.killProcess(target.pid, force = forceKill)
                        processToKill = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("End Process", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { processToKill = null }) {
                    Text("Cancel", color = TextSecondary, fontSize = 14.sp)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
