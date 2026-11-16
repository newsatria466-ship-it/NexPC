package com.nexpc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.data.PackageSearchResult
import com.nexpc.app.ui.components.GlassCard
import com.nexpc.app.ui.components.ActionBubble
import com.nexpc.app.ui.components.SegmentedPill
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun PackageManagerScreen(viewModel: MainViewModel) {
    val updates by viewModel.packageUpdates.collectAsState()
    val searchResults by viewModel.packageSearchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var pkgToInstall by remember { mutableStateOf<PackageSearchResult?>(null) }

    LaunchedEffect(Unit) {
        viewModel.checkPackageUpdates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SegmentedPill(
            options = listOf("Updates (${updates.totalUpdates})", "Package Search"),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Updates Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (updates.totalUpdates > 0) AmberSubtle else GreenSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (updates.totalUpdates > 0) Icons.Default.SystemUpdate else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (updates.totalUpdates > 0) AmberWarning else EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${updates.totalUpdates} Pending Updates",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                ActionBubble(
                    icon = Icons.Default.Refresh,
                    label = "Check",
                    onClick = { viewModel.checkPackageUpdates() },
                    color = Indigo,
                    modifier = Modifier
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueAccent)
                }
            } else if (updates.updates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your Arch Linux system is up to date! ✨",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(updates.updates) { u ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = u.pkg,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = u.oldVersion,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextSecondary
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp).padding(horizontal = 4.dp)
                                        )
                                        Text(
                                            text = u.newVersion,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium,
                                            color = BlueAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Search Tab
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search pacman packages...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = BlueAccent)
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

                ActionBubble(
                    icon = Icons.Default.Search,
                    label = "Search",
                    onClick = { viewModel.searchPackages(searchQuery) },
                    color = Indigo,
                    modifier = Modifier.height(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueAccent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { item ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (item.version.isNotEmpty()) {
                                                Text(
                                                    text = item.version,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = TextMuted
                                                )
                                            }
                                            if (item.source.isNotEmpty()) {
                                                Text(
                                                    text = "(${item.source})",
                                                    fontSize = 11.sp,
                                                    color = BlueAccent
                                                )
                                            }
                                        }
                                    }

                                    ActionBubble(
                                        icon = if (item.installed) Icons.Default.Refresh else Icons.Default.Download,
                                        label = if (item.installed) "Reinstall" else "Install",
                                        onClick = { pkgToInstall = item },
                                        color = if (item.installed) Indigo else Pink
                                    )
                                }

                                if (item.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.description,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Install Confirmation Dialog
        pkgToInstall?.let { pkg ->
            AlertDialog(
                onDismissRequest = { pkgToInstall = null },
                shape = RoundedCornerShape(16.dp),
                containerColor = SurfaceCard,
                icon = {
                    Icon(Icons.Default.Download, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(28.dp))
                },
                title = {
                    Text(
                        text = "Install Package Pacman",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Apakah Anda ingin memasang \"${pkg.name}\" di Arch Linux?",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Proses instalasi pacman akan dijalankan di laptop Anda.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.installPackage(pkg.name)
                            pkgToInstall = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Text("Install Sekarang", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pkgToInstall = null }) {
                        Text("Batal", color = TextMuted)
                    }
                }
            )
        }
    }
}
