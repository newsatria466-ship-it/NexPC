package com.nexpc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.components.GlassCard
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun ControlScreen(viewModel: MainViewModel) {
    var pendingAction by remember { mutableStateOf<String?>(null) }
    var actionTitle by remember { mutableStateOf("") }
    var actionDescription by remember { mutableStateOf("") }
    var actionColor by remember { mutableStateOf<Color>(BlueAccent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PC Control",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Manage Arch Linux system state remotely",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ControlCard(
                    title = "Lock Screen",
                    subtitle = "Secure session",
                    icon = Icons.Default.Lock,
                    accentColor = BlueAccent,
                    onClick = { viewModel.lockPc() }
                )
            }

            item {
                ControlCard(
                    title = "Suspend",
                    subtitle = "Sleep state",
                    icon = Icons.Default.Bedtime,
                    accentColor = PurpleAccent,
                    onClick = {
                        actionTitle = "Suspend PC"
                        actionDescription = "Put Arch Linux to sleep?"
                        actionColor = PurpleAccent
                        pendingAction = "suspend"
                    }
                )
            }

            item {
                ControlCard(
                    title = "Restart",
                    subtitle = "Reboot system",
                    icon = Icons.Default.RestartAlt,
                    accentColor = AmberWarning,
                    onClick = {
                        actionTitle = "Restart PC"
                        actionDescription = "Are you sure you want to reboot the PC?"
                        actionColor = AmberWarning
                        pendingAction = "reboot"
                    }
                )
            }

            item {
                ControlCard(
                    title = "Shutdown",
                    subtitle = "Power off PC",
                    icon = Icons.Default.PowerSettingsNew,
                    accentColor = CrimsonDanger,
                    onClick = {
                        actionTitle = "Shutdown PC"
                        actionDescription = "Are you sure you want to power off the PC?"
                        actionColor = CrimsonDanger
                        pendingAction = "shutdown"
                    }
                )
            }
        }
    }

    // Confirmation Alert Dialog
    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = {
                Text(text = actionTitle, color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = actionDescription, color = TextSecondary, fontSize = 14.sp)
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
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ControlCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
