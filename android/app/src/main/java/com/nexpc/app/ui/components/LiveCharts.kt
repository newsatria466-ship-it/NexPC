package com.nexpc.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.theme.*

@Composable
fun ActivityOverviewChart(
    cpuPoints: List<Float>,
    ramPoints: List<Float>,
    netPoints: List<Float> = emptyList(),
    modifier: Modifier = Modifier,
    height: Dp = 140.dp
) {
    var selectedRange by remember { mutableStateOf("5m") }
    val timeRanges = listOf("5m", "30m", "1h", "6h", "24h")

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header + Time Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Overview",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Filter chips
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    timeRanges.forEach { range ->
                        val isSelected = selectedRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SurfaceCard else Color.Transparent)
                                .clickable { selectedRange = range }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) BlueAccent else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(label = "CPU", color = BlueAccent)
                LegendItem(label = "RAM", color = PurpleAccent)
                if (netPoints.isNotEmpty()) {
                    LegendItem(label = "Network", color = EmeraldGreen)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-series Smooth Wave Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                val width = size.width
                val canvasHeight = size.height

                // Draw horizontal subtle grid lines (0%, 50%, 100%)
                val gridColor = CardBorder.copy(alpha = 0.6f)
                val gridStroke = 1.dp.toPx()
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(width, 0f), strokeWidth = gridStroke)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, canvasHeight / 2f), end = androidx.compose.ui.geometry.Offset(width, canvasHeight / 2f), strokeWidth = gridStroke)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, canvasHeight), end = androidx.compose.ui.geometry.Offset(width, canvasHeight), strokeWidth = gridStroke)

                fun drawSmoothSeries(points: List<Float>, color: Color, maxValue: Float = 100f) {
                    if (points.size < 2) return
                    val stepX = width / (points.size - 1)

                    val linePath = Path()
                    val fillPath = Path()

                    val coords = points.mapIndexed { idx, v ->
                        val norm = (v.coerceIn(0f, maxValue) / maxValue)
                        val x = idx * stepX
                        val y = canvasHeight - (norm * (canvasHeight - 6.dp.toPx())) - 3.dp.toPx()
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    linePath.moveTo(coords[0].x, coords[0].y)
                    fillPath.moveTo(coords[0].x, canvasHeight)
                    fillPath.lineTo(coords[0].x, coords[0].y)

                    for (i in 0 until coords.size - 1) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]
                        val cx1 = p0.x + (p1.x - p0.x) / 2f
                        val cy1 = p0.y
                        val cx2 = cx1
                        val cy2 = p1.y

                        linePath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                        fillPath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                    }

                    fillPath.lineTo(coords.last().x, canvasHeight)
                    fillPath.close()

                    // Fill gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.16f),
                                color.copy(alpha = 0.01f)
                            ),
                            startY = 0f,
                            endY = canvasHeight
                        )
                    )

                    // Line
                    drawPath(
                        path = linePath,
                        color = color,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                if (netPoints.isNotEmpty()) {
                    drawSmoothSeries(netPoints, EmeraldGreen, maxValue = 100f)
                }
                drawSmoothSeries(ramPoints, PurpleAccent, maxValue = 100f)
                drawSmoothSeries(cpuPoints, BlueAccent, maxValue = 100f)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
fun LiveSparklineChart(
    dataPoints: List<Float>,
    title: String,
    currentValue: String,
    modifier: Modifier = Modifier,
    lineColor: Color = BlueAccent,
    height: Dp = 80.dp,
    maxValue: Float = 100f
) {
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = currentValue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = lineColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                if (dataPoints.size < 2) return@Canvas

                val width = size.width
                val canvasHeight = size.height
                val stepX = width / (dataPoints.size - 1)

                val linePath = Path()
                val fillPath = Path()

                dataPoints.forEachIndexed { index, value ->
                    val normalizedValue = (value.coerceIn(0f, maxValue) / maxValue)
                    val x = index * stepX
                    val y = canvasHeight - (normalizedValue * canvasHeight)

                    if (index == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, canvasHeight)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                fillPath.lineTo((dataPoints.size - 1) * stepX, canvasHeight)
                fillPath.close()

                // Draw soft gradient under the line
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.15f),
                            lineColor.copy(alpha = 0.0f)
                        )
                    )
                )

                // Draw line
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }
    }
}

