package com.nexpc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.nexpc.app.ui.components.LinearMetricRow
import com.nexpc.app.ui.theme.*
import com.nexpc.app.viewmodel.MainViewModel

@Composable
fun LaptopScreen(viewModel: MainViewModel) {
    val laptopStatus by viewModel.laptopStatus.collectAsState()
    val battery = laptopStatus.battery
    val profiles = laptopStatus.powerProfiles
    val hardware = laptopStatus.hardware

    LaunchedEffect(Unit) {
        viewModel.loadLaptopStatus()
    }

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
                    text = "Laptop Mode",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Battery telemetry, power profiles, and energy controls.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // 1. Battery Information Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (battery.isCharging) MintBg else IndigoBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                                    contentDescription = null,
                                    tint = if (battery.isCharging) Mint else Indigo,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Battery Status",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        // Charging badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (battery.isCharging) GreenSubtle else SurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (battery.isCharging) EmeraldGreen else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (battery.isCharging) "Charging" else battery.state,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (battery.isCharging) EmeraldGreen else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${battery.percentage}%",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        if (battery.timeRemainingStr != null) {
                            Text(
                                text = battery.timeRemainingStr,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Battery Level Bar
                    LinearProgressIndicator(
                        progress = { (battery.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (battery.percentage > 20) EmeraldGreen else CrimsonDanger,
                        trackColor = SurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Health, Wattage, Cycles Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (battery.healthPercent != null) {
                            Column {
                                Text("Health", fontSize = 11.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${battery.healthPercent}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                        }

                        if (battery.wattage != null) {
                            Column {
                                Text("Power Rate", fontSize = 11.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${battery.wattage} W",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                        }

                        if (battery.cycleCount != null) {
                            Column {
                                Text("Cycles", fontSize = 11.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${battery.cycleCount}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Power Profile Selector
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VioletBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Violet, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Power Profile",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Current: ${profiles.activeProfile.replaceFirstChar { it.uppercase() }}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (profiles.supported) {
                        Spacer(modifier = Modifier.height(16.dp))

                        val profileOptions = listOf(
                            Triple("power-saver", "Power Saver", Icons.Default.Eco),
                            Triple("balanced", "Balanced", Icons.Default.Balance),
                            Triple("performance", "Performance", Icons.Default.Speed)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profileOptions.forEach { (id, label, icon) ->
                                val isSelected = profiles.activeProfile.lowercase().contains(id.replace("-", "")) || profiles.activeProfile.lowercase() == id

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.setPowerProfile(id) },
                                    color = if (isSelected) BlueAccent else SurfaceElevated,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) Color.White else TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Profil daya diatur secara otomatis oleh sistem Windows.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // 3. Hardware Status Summary
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CoralBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = Coral, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hardware",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    LinearMetricRow(
                        label = "CPU Load",
                        percent = hardware.cpuUsagePercent,
                        valueText = "${hardware.cpuFreqMhz.toInt()} MHz",
                        accentColor = BlueAccent
                    )

                    val ramUsedGb = hardware.ramUsedMb / 1024f
                    val ramTotalGb = hardware.ramTotalMb / 1024f
                    LinearMetricRow(
                        label = "Memory",
                        percent = hardware.ramPercent,
                        valueText = String.format("%.1f / %.1f GB", ramUsedGb, ramTotalGb),
                        accentColor = PurpleAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CPU Temperature", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = "${hardware.cpuTemp.toInt()}°C",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hardware.cpuTemp > 75f) AmberWarning else TextPrimary
                        )
                    }
                }
            }
        }
    }
}
