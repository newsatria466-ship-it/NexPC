package com.nexpc.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Adorable 3D claymorphic-styled mascots drawn natively with Jetpack Compose.
 * Featuring continuous looping motions, expressive reactions, and micro-animations.
 */

@Composable
fun LaptopMascot(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val glanceTransition = rememberInfiniteTransition(label = "laptopGlance")
    val glanceX by glanceTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glanceX"
    )
    val lidBounce by glanceTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lidBounce"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Screen lid (Slate Indigo) with subtle bob
            drawRoundRect(
                color = Color(0xFF6366F1),
                topLeft = Offset(w * 0.18f, h * 0.12f + lidBounce),
                size = Size(w * 0.64f, h * 0.52f),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Screen face background (Clean White/Soft)
            drawRoundRect(
                color = Color(0xFFEFF6FF),
                topLeft = Offset(w * 0.24f, h * 0.18f + lidBounce),
                size = Size(w * 0.52f, h * 0.40f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Cute Glasses (circular frames)
            val glassY = h * 0.34f + lidBounce
            val r = w * 0.08f
            // Left eye lens
            drawCircle(color = Color.White, radius = r, center = Offset(w * 0.38f, glassY))
            drawCircle(color = Color(0xFF1E293B), radius = r, center = Offset(w * 0.38f, glassY), style = Stroke(width = 3.5f))
            // Right eye lens
            drawCircle(color = Color.White, radius = r, center = Offset(w * 0.62f, glassY))
            drawCircle(color = Color(0xFF1E293B), radius = r, center = Offset(w * 0.62f, glassY), style = Stroke(width = 3.5f))
            // Glasses bridge
            drawLine(color = Color(0xFF1E293B), start = Offset(w * 0.46f, glassY), end = Offset(w * 0.54f, glassY), strokeWidth = 3.5f)

            // Pupils (glancing left/right smoothly)
            drawCircle(color = Color(0xFF0F172A), radius = r * 0.4f, center = Offset(w * 0.39f + glanceX, glassY))
            drawCircle(color = Color(0xFF0F172A), radius = r * 0.4f, center = Offset(w * 0.63f + glanceX, glassY))

            // Cheeks (blush pink)
            drawCircle(color = Color(0xFFF472B6).copy(alpha = 0.6f), radius = 4f, center = Offset(w * 0.28f, h * 0.44f + lidBounce))
            drawCircle(color = Color(0xFFF472B6).copy(alpha = 0.6f), radius = 4f, center = Offset(w * 0.72f, h * 0.44f + lidBounce))

            // Smile
            val smilePath = Path().apply {
                moveTo(w * 0.45f, h * 0.45f + lidBounce)
                quadraticTo(w * 0.50f, h * 0.51f + lidBounce, w * 0.55f, h * 0.45f + lidBounce)
            }
            drawPath(smilePath, color = Color(0xFF1E293B), style = Stroke(width = 3.5f))

            // Laptop Base (Keyboard deck)
            drawRoundRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(w * 0.08f, h * 0.66f),
                size = Size(w * 0.84f, h * 0.18f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            // Trackpad
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(w * 0.40f, h * 0.70f),
                size = Size(w * 0.20f, h * 0.10f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
fun CpuMascot(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val cpuAnim = rememberInfiniteTransition(label = "cpuAnim")
    val pinMarquee by cpuAnim.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pinMarquee"
    )
    val eyeLook by cpuAnim.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyeLook"
    )
    val sweatScale by cpuAnim.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweatScale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // CPU body (Slate Indigo chip)
            drawRoundRect(
                color = Color(0xFF4F46E5),
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.64f, h * 0.64f),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Inner silicon plate
            drawRoundRect(
                color = Color(0xFFEEF2FF),
                topLeft = Offset(w * 0.26f, h * 0.26f),
                size = Size(w * 0.48f, h * 0.48f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Little legs / pins with sequential glowing pulse (data bus animation)
            val activePinIdx = pinMarquee.toInt() % 3
            val pinW = 3.5f
            for (i in 0..2) {
                val offset = w * (0.32f + i * 0.18f)
                val pinCol = if (i == activePinIdx) Color(0xFFFBBF24) else Color(0xFF818CF8)
                // Top pins
                drawLine(pinCol, Offset(offset, h * 0.06f), Offset(offset, h * 0.18f), strokeWidth = pinW)
                // Bottom pins
                drawLine(pinCol, Offset(offset, h * 0.82f), Offset(offset, h * 0.94f), strokeWidth = pinW)
            }

            // Eyes (glancing left/right)
            drawCircle(color = Color(0xFF0F172A), radius = 3.5f, center = Offset(w * 0.40f + eyeLook, h * 0.46f))
            drawCircle(color = Color(0xFF0F172A), radius = 3.5f, center = Offset(w * 0.60f + eyeLook, h * 0.46f))

            // Smile
            val mouthPath = Path().apply {
                moveTo(w * 0.45f, h * 0.55f)
                quadraticTo(w * 0.50f, h * 0.61f, w * 0.55f, h * 0.55f)
            }
            drawPath(mouthPath, color = Color(0xFF1E293B), style = Stroke(width = 3f))

            // Little working sweat drop (pulsing size)
            val dropPath = Path().apply {
                val dx = w * 0.68f
                val dy = h * 0.28f
                moveTo(dx, dy)
                quadraticTo(dx + 4f * sweatScale, dy + 5f * sweatScale, dx, dy + 8f * sweatScale)
                quadraticTo(dx - 4f * sweatScale, dy + 5f * sweatScale, dx, dy)
            }
            drawPath(dropPath, color = Color(0xFF38BDF8), style = Fill)
        }
    }
}

@Composable
fun RamMascot(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val ramAnim = rememberInfiniteTransition(label = "ramAnim")
    val tailFlutter by ramAnim.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailFlutter"
    )
    val jogBounce by ramAnim.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jogBounce"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // RAM stick body (Green PCB) with jog rhythm
            drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(w * 0.28f, h * 0.12f + jogBounce),
                size = Size(w * 0.44f, h * 0.76f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Red/Coral Fitness Headband (Cute sportive look)
            drawRoundRect(
                color = Color(0xFFF43F5E),
                topLeft = Offset(w * 0.24f, h * 0.22f + jogBounce),
                size = Size(w * 0.52f, h * 0.14f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Headband stripe
            drawLine(Color.White, Offset(w * 0.26f, h * 0.29f + jogBounce), Offset(w * 0.74f, h * 0.29f + jogBounce), strokeWidth = 2f)

            // Fluttering headband knot / tails (Right side)
            val knotPath = Path().apply {
                moveTo(w * 0.76f, h * 0.26f + jogBounce)
                quadraticTo(w * 0.88f, h * 0.22f + jogBounce + tailFlutter, w * 0.94f, h * 0.32f + jogBounce + tailFlutter)
                quadraticTo(w * 0.85f, h * 0.36f + jogBounce, w * 0.76f, h * 0.32f + jogBounce)
                close()
            }
            drawPath(knotPath, color = Color(0xFFE11D48), style = Fill)

            // Eyes
            drawCircle(color = Color.White, radius = 4f, center = Offset(w * 0.40f, h * 0.44f + jogBounce))
            drawCircle(color = Color(0xFF0F172A), radius = 2.5f, center = Offset(w * 0.40f, h * 0.44f + jogBounce))
            drawCircle(color = Color.White, radius = 4f, center = Offset(w * 0.60f, h * 0.44f + jogBounce))
            drawCircle(color = Color(0xFF0F172A), radius = 2.5f, center = Offset(w * 0.60f, h * 0.44f + jogBounce))

            // Big determined smile
            val smilePath = Path().apply {
                moveTo(w * 0.42f, h * 0.56f + jogBounce)
                quadraticTo(w * 0.50f, h * 0.64f + jogBounce, w * 0.58f, h * 0.56f + jogBounce)
            }
            drawPath(smilePath, color = Color(0xFF064E3B), style = Stroke(width = 3f))

            // Gold connectors at bottom
            for (i in 0..3) {
                val x = w * (0.33f + i * 0.10f)
                drawLine(Color(0xFFFCD34D), Offset(x, h * 0.88f + jogBounce), Offset(x, h * 0.94f + jogBounce), strokeWidth = 3f)
            }
        }
    }
}

@Composable
fun TempMascot(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val tempAnim = rememberInfiniteTransition(label = "tempAnim")
    val fluidPulse by tempAnim.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fluidPulse"
    )
    val steamY by tempAnim.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steamY"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Rising warm steam bubble
            val steamAlpha = (1f - (-steamY / 12f)).coerceIn(0f, 1f)
            drawCircle(
                color = Color(0xFFF59E0B).copy(alpha = steamAlpha * 0.5f),
                radius = 3.5f,
                center = Offset(w * 0.50f, h * 0.12f + steamY)
            )

            // Thermometer Glass Tube
            drawRoundRect(
                color = Color(0xFFFFFBEB),
                topLeft = Offset(w * 0.38f, h * 0.10f),
                size = Size(w * 0.24f, h * 0.60f),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Bottom Bulb
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = w * 0.22f,
                center = Offset(w * 0.50f, h * 0.72f)
            )

            // Warm fluid inside tube (breathing height)
            val fluidTop = h * 0.35f + fluidPulse
            drawRoundRect(
                color = Color(0xFFF59E0B),
                topLeft = Offset(w * 0.44f, fluidTop),
                size = Size(w * 0.12f, (h * 0.70f) - fluidTop),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Cute Scarf (Mint / Cozy Green)
            val scarfY = h * 0.50f
            drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(w * 0.30f, scarfY),
                size = Size(w * 0.40f, h * 0.14f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Scarf tail hanging
            drawRoundRect(
                color = Color(0xFF059669),
                topLeft = Offset(w * 0.56f, scarfY + h * 0.08f),
                size = Size(w * 0.14f, h * 0.18f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Happy eyes on bulb
            drawCircle(color = Color.White, radius = 3.5f, center = Offset(w * 0.42f, h * 0.70f))
            drawCircle(color = Color(0xFF78350F), radius = 2f, center = Offset(w * 0.42f, h * 0.70f))
            drawCircle(color = Color.White, radius = 3.5f, center = Offset(w * 0.58f, h * 0.70f))
            drawCircle(color = Color(0xFF78350F), radius = 2f, center = Offset(w * 0.58f, h * 0.70f))

            // Little smile
            val smilePath = Path().apply {
                moveTo(w * 0.46f, h * 0.78f)
                quadraticTo(w * 0.50f, h * 0.83f, w * 0.54f, h * 0.78f)
            }
            drawPath(smilePath, color = Color(0xFF78350F), style = Stroke(width = 2.5f))
        }
    }
}

@Composable
fun StorageMascot(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val docAnim = rememberInfiniteTransition(label = "storageDoc")
    val sheetBounce by docAnim.animateFloat(
        initialValue = 0f,
        targetValue = -3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sheetBounce"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Back folder tab
            drawRoundRect(
                color = Color(0xFFFDE68A),
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.34f, h * 0.20f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Documents sticking out (colorful tabs with bouncing animation)
            drawRoundRect(
                color = Color(0xFF60A5FA),
                topLeft = Offset(w * 0.28f, h * 0.24f + sheetBounce),
                size = Size(w * 0.44f, h * 0.35f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFFF472B6),
                topLeft = Offset(w * 0.36f, h * 0.20f + sheetBounce * 1.3f),
                size = Size(w * 0.40f, h * 0.35f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Front folder body
            drawRoundRect(
                color = Color(0xFFFBBF24),
                topLeft = Offset(w * 0.14f, h * 0.38f),
                size = Size(w * 0.72f, h * 0.50f),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Eyes
            drawCircle(color = Color(0xFF78350F), radius = 3.5f, center = Offset(w * 0.38f, h * 0.60f))
            drawCircle(color = Color(0xFF78350F), radius = 3.5f, center = Offset(w * 0.62f, h * 0.60f))

            // Pink Cheeks
            drawCircle(color = Color(0xFFF472B6).copy(alpha = 0.7f), radius = 4f, center = Offset(w * 0.28f, h * 0.66f))
            drawCircle(color = Color(0xFFF472B6).copy(alpha = 0.7f), radius = 4f, center = Offset(w * 0.72f, h * 0.66f))

            // Cheerful open mouth
            val mouthPath = Path().apply {
                moveTo(w * 0.44f, h * 0.68f)
                quadraticTo(w * 0.50f, h * 0.78f, w * 0.56f, h * 0.68f)
                close()
            }
            drawPath(mouthPath, color = Color(0xFF78350F), style = Fill)
        }
    }
}

/**
 * Character-rich circular gauge Bento card with mascot and interactive spring press.
 */
@Composable
fun MetricGaugeCard(
    title: String,
    percent: Float,
    valueText: String,
    statusText: String,
    accentColor: Color,
    mascot: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val animPercent by animateFloatAsState(
        targetValue = percent.coerceIn(0f, 100f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "gaugeAnim"
    )

    Column(
        modifier = modifier
            .bounceClick { onClick?.invoke() }
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceWhite)
            .border(1.5.dp, Line, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            mascot()
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(66.dp)) {
                val strokePx = 6.dp.toPx()
                val pad = strokePx / 2
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                val topLeft = Offset(pad, pad)

                // Track
                drawArc(
                    color = accentColor.copy(alpha = 0.12f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Progress sweep
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = (animPercent / 100f) * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            Text(
                text = valueText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(accentColor.copy(alpha = 0.10f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                statusText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Character-rich large-number Bento card with mascot and interactive spring press.
 */
@Composable
fun MetricNumberCard(
    title: String,
    valueText: String,
    statusText: String,
    accentColor: Color,
    mascot: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .bounceClick { onClick?.invoke() }
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceWhite)
            .border(1.5.dp, Line, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            mascot()
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valueText,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(accentColor.copy(alpha = 0.10f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                statusText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * 3D Claymorphic Blush Ball Mascot with continuous rich looping motions:
 * - Natural eye blinking every ~3.5 seconds.
 * - Continuous sinusoidal floating bob and subtle sway tilt.
 * - Oscillating antenna heart spring.
 * - 3 Orbiting sparkling stars (✦) circling around in 3D depth with twinkling alpha!
 * - Tap interaction: 360-degree backflip jump with floating heart particle burst!
 */
@Composable
fun BlushBallMascot(
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    isWinking: Boolean = false,
    onClick: () -> Unit = {}
) {
    var tapped by remember { mutableStateOf(false) }

    // Tap Jump & Flip Animation
    val jumpRotation by animateFloatAsState(
        targetValue = if (tapped) 360f else 0f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "jumpRotate",
        finishedListener = { tapped = false }
    )
    val jumpY by animateFloatAsState(
        targetValue = if (tapped) -28f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 350f),
        label = "jumpY"
    )

    // Continuous Looping Transitions
    val ballLoops = rememberInfiniteTransition(label = "blushBallLoops")
    val floatY by ballLoops.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val tiltAngle by ballLoops.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiltAngle"
    )
    val antennaWobble by ballLoops.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antennaWobble"
    )
    val blinkCycle by ballLoops.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blinkCycle"
    )
    val orbitAngle by ballLoops.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )

    // Eyes blink naturally for 180ms every 3.6 seconds
    val isNaturallyBlinking = blinkCycle > 0.95f

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationY = floatY + jumpY
                rotationZ = tiltAngle + jumpRotation
            }
            .bounceClick {
                tapped = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (minOf(w, h) / 2f) * 0.85f

            // 1. Soft 3D drop shadow
            drawCircle(
                color = Color(0xFFFF5C8A).copy(alpha = 0.20f),
                radius = radius * 0.95f,
                center = Offset(cx, cy + radius * 0.12f)
            )

            // 2. Main Sphere Body (Rose-pink clay gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFB3C6),
                        Color(0xFFFF7597),
                        Color(0xFFF43F5E)
                    ),
                    center = Offset(cx - radius * 0.28f, cy - radius * 0.28f),
                    radius = radius * 1.3f
                ),
                radius = radius,
                center = Offset(cx, cy)
            )

            // 3. Glossy 3D Highlight bubble (Top-Left)
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.80f), Color.White.copy(alpha = 0f)),
                    start = Offset(cx - radius * 0.55f, cy - radius * 0.55f),
                    end = Offset(cx - radius * 0.15f, cy - radius * 0.15f)
                ),
                topLeft = Offset(cx - radius * 0.60f, cy - radius * 0.65f),
                size = Size(radius * 0.55f, radius * 0.35f)
            )

            // 4. Rosy Blushing Cheeks (left and right)
            val cheekR = radius * 0.22f
            drawCircle(
                color = Color(0xFFE11D48).copy(alpha = 0.35f),
                radius = cheekR,
                center = Offset(cx - radius * 0.48f, cy + radius * 0.14f)
            )
            drawCircle(
                color = Color(0xFFE11D48).copy(alpha = 0.35f),
                radius = cheekR,
                center = Offset(cx + radius * 0.48f, cy + radius * 0.14f)
            )

            // 5. Eyes (with natural blinking loop or winking)
            val eyeY = cy - radius * 0.04f
            val leftEyeX = cx - radius * 0.26f
            val rightEyeX = cx + radius * 0.26f

            if (isWinking || tapped) {
                // Winking left eye
                val winkPath = Path().apply {
                    moveTo(leftEyeX - 6f, eyeY)
                    quadraticTo(leftEyeX, eyeY - 8f, leftEyeX + 6f, eyeY)
                }
                drawPath(winkPath, color = Color(0xFF1E293B), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                // Right eye open with sparkle
                drawCircle(color = Color(0xFF1E293B), radius = 5.5f, center = Offset(rightEyeX, eyeY))
                drawCircle(color = Color.White, radius = 2.2f, center = Offset(rightEyeX - 1.8f, eyeY - 1.8f))
            } else if (isNaturallyBlinking) {
                // Closed smiling eyes while blinking
                val blinkLeft = Path().apply {
                    moveTo(leftEyeX - 5f, eyeY)
                    quadraticTo(leftEyeX, eyeY - 6f, leftEyeX + 5f, eyeY)
                }
                val blinkRight = Path().apply {
                    moveTo(rightEyeX - 5f, eyeY)
                    quadraticTo(rightEyeX, eyeY - 6f, rightEyeX + 5f, eyeY)
                }
                drawPath(blinkLeft, color = Color(0xFF1E293B), style = Stroke(width = 3.2f, cap = StrokeCap.Round))
                drawPath(blinkRight, color = Color(0xFF1E293B), style = Stroke(width = 3.2f, cap = StrokeCap.Round))
            } else {
                // Both open eyes with cute sparkle catchlights
                drawCircle(color = Color(0xFF1E293B), radius = 5.5f, center = Offset(leftEyeX, eyeY))
                drawCircle(color = Color.White, radius = 2.2f, center = Offset(leftEyeX - 1.8f, eyeY - 1.8f))

                drawCircle(color = Color(0xFF1E293B), radius = 5.5f, center = Offset(rightEyeX, eyeY))
                drawCircle(color = Color.White, radius = 2.2f, center = Offset(rightEyeX - 1.8f, eyeY - 1.8f))
            }

            // 6. Smiling Mouth
            val smilePath = Path().apply {
                moveTo(cx - radius * 0.16f, cy + radius * 0.12f)
                quadraticTo(cx, cy + radius * 0.28f, cx + radius * 0.16f, cy + radius * 0.12f)
            }
            drawPath(smilePath, color = Color(0xFF1E293B), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

            // 7. Little cute heart or antenna on top right with spring oscillation
            val heartBaseX = cx + radius * 0.35f
            val heartBaseY = cy - radius * 0.75f
            val heartTipX = cx + radius * 0.65f + (antennaWobble * 0.5f)
            val heartTipY = cy - radius * 0.90f
            val stemPath = Path().apply {
                moveTo(heartBaseX, heartBaseY)
                quadraticTo(cx + radius * 0.50f, cy - radius * 1.05f, heartTipX, heartTipY)
            }
            drawPath(stemPath, color = Color(0xFFF43F5E), style = Stroke(width = 3f, cap = StrokeCap.Round))
            drawCircle(
                color = Color(0xFFFF5C8A),
                radius = 5.5f,
                center = Offset(heartTipX, heartTipY)
            )

            // 8. Orbiting 3D Sparkling Stars (✦) circling around the character
            for (i in 0..2) {
                val starAngleDeg = orbitAngle + (i * 120f)
                val starRad = starAngleDeg * (Math.PI / 180f)
                val orbitRx = radius * 1.35f
                val orbitRy = radius * 0.55f
                val starX = cx + (orbitRx * cos(starRad)).toFloat()
                val starY = cy + (orbitRy * sin(starRad)).toFloat()

                // Stars in front are brighter and slightly larger
                val isFront = sin(starRad) > 0
                val starAlpha = if (isFront) 0.90f else 0.40f
                val starSize = if (isFront) 4f else 2.5f

                // Draw 4-point sparkle star
                val starCol = Color(0xFFFBBF24).copy(alpha = starAlpha)
                drawLine(starCol, Offset(starX - starSize, starY), Offset(starX + starSize, starY), strokeWidth = 2f, cap = StrokeCap.Round)
                drawLine(starCol, Offset(starX, starY - starSize), Offset(starX, starY + starSize), strokeWidth = 2f, cap = StrokeCap.Round)
            }

            // 9. Floating heart particles when tapped
            if (tapped) {
                for (j in 0..2) {
                    val hx = cx + ((j - 1) * 22f)
                    val hy = cy - radius - 16f - (j * 12f)
                    drawCircle(Color(0xFFFF5C8A).copy(alpha = 0.85f), radius = 4.5f, center = Offset(hx, hy))
                }
            }
        }
    }
}

/**
 * Sun & Cloud Mascot with continuous 360-degree rotating sun rays,
 * gentle floating cloud puffs, and sparkling stars.
 */
@Composable
fun SunCloudMascot(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunCloudAnim")
    val sunFloat by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunFloat"
    )
    val sunRayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRayRotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Golden Sun peeking from top-left of cloud
            val sunCx = w * 0.50f
            val sunCy = (h * 0.32f) + sunFloat
            val sunR = w * 0.20f

            // Continuously rotating sun rays around sun center
            rotate(degrees = sunRayRotation, pivot = Offset(sunCx, sunCy)) {
                for (i in 0..7) {
                    val angle = (i * 45f) * (Math.PI / 180f)
                    val rx1 = sunCx + (sunR * 1.15f * cos(angle)).toFloat()
                    val ry1 = sunCy + (sunR * 1.15f * sin(angle)).toFloat()
                    val rx2 = sunCx + (sunR * 1.45f * cos(angle)).toFloat()
                    val ry2 = sunCy + (sunR * 1.45f * sin(angle)).toFloat()
                    drawLine(
                        color = Color(0xFFFBBF24),
                        start = Offset(rx1, ry1),
                        end = Offset(rx2, ry2),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Sun Body (Warm gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B)),
                    center = Offset(sunCx, sunCy),
                    radius = sunR
                ),
                radius = sunR,
                center = Offset(sunCx, sunCy)
            )

            // Fluffy Cloud (Soft Pastel Rose/White puffs)
            val cloudColor = Color(0xFFFFD1DC)
            val cloudHighlight = Color(0xFFFFF0F5)

            // 3 overlapping cloud puffs
            drawCircle(color = cloudColor, radius = w * 0.22f, center = Offset(w * 0.36f, h * 0.65f))
            drawCircle(color = cloudColor, radius = w * 0.27f, center = Offset(w * 0.58f, h * 0.58f))
            drawCircle(color = cloudColor, radius = w * 0.20f, center = Offset(w * 0.78f, h * 0.68f))

            // Soft highlight overlay
            drawCircle(color = cloudHighlight.copy(alpha = 0.8f), radius = w * 0.22f, center = Offset(w * 0.56f, h * 0.56f))

            // Little sparkling stars
            drawCircle(color = Color(0xFFFBBF24), radius = 2.5f, center = Offset(w * 0.15f, h * 0.28f))
            drawCircle(color = Color(0xFFFF5C8A), radius = 2f, center = Offset(w * 0.88f, h * 0.35f))
        }
    }
}

/**
 * Dedicated Animated Arch PC Companion ("Archie")!
 * A character that moves continuously in an active loop:
 * - Floating on a soft cloud/desk pad.
 * - Digital glowing visor with expressive eyes.
 * - When CPU is busy: typing furiously on a glowing keyboard with alternating hands and flying code sparks!
 * - When CPU is normal: waving hand with floating music notes (♪) drifting up.
 * - Speech bubble showing live system mood.
 * - Tappable with celebratory jump and heart particle burst!
 */
@Composable
fun AnimatedPcBuddy(
    cpuUsage: Float = 0f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var tapped by remember { mutableStateOf(false) }
    val isBusy = cpuUsage > 45f

    val jumpAnim by animateFloatAsState(
        targetValue = if (tapped) -24f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
        label = "buddyJump",
        finishedListener = { tapped = false }
    )

    val companionLoops = rememberInfiniteTransition(label = "companionLoops")
    val floatY by companionLoops.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buddyFloat"
    )
    val typingLeftHand by companionLoops.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBusy) 180 else 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typeLeft"
    )
    val typingRightHand by companionLoops.animateFloat(
        initialValue = 3f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBusy) 180 else 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typeRight"
    )
    val musicNoteFloat by companionLoops.animateFloat(
        initialValue = 0f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "musicFloat"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceWhite)
            .border(1.5.dp, Line, RoundedCornerShape(24.dp))
            .bounceClick {
                tapped = true
                onClick()
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Animated Chibi Character
        Box(
            modifier = Modifier
                .size(62.dp)
                .graphicsLayer {
                    translationY = floatY + jumpAnim
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // 1. Shadow beneath character
                drawOval(
                    color = Color(0xFF6366F1).copy(alpha = 0.14f),
                    topLeft = Offset(w * 0.18f, h * 0.82f),
                    size = Size(w * 0.64f, h * 0.16f)
                )

                // 2. Chibi Robot Head / Helmet (Arch Indigo)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    topLeft = Offset(w * 0.15f, h * 0.15f),
                    size = Size(w * 0.70f, h * 0.62f),
                    cornerRadius = CornerRadius(18f, 18f)
                )

                // Cute Antenna on top with blinking tip
                drawLine(
                    color = Color(0xFF818CF8),
                    start = Offset(cx, h * 0.15f),
                    end = Offset(cx, h * 0.05f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = if (isBusy) Color(0xFFF43F5E) else Color(0xFF10B981),
                    radius = 4f,
                    center = Offset(cx, h * 0.05f)
                )

                // 3. Glowing Visor Screen (Deep Navy)
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(w * 0.22f, h * 0.24f),
                    size = Size(w * 0.56f, h * 0.38f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // 4. Digital Expressive Eyes
                val eyeColor = if (isBusy) Color(0xFFFBBF24) else Color(0xFF38BDF8)
                if (isBusy) {
                    // Determined / energetic eyes (> <)
                    drawLine(eyeColor, Offset(w * 0.32f, h * 0.38f), Offset(w * 0.42f, h * 0.44f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(eyeColor, Offset(w * 0.42f, h * 0.44f), Offset(w * 0.32f, h * 0.50f), strokeWidth = 2.5f, cap = StrokeCap.Round)

                    drawLine(eyeColor, Offset(w * 0.68f, h * 0.38f), Offset(w * 0.58f, h * 0.44f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(eyeColor, Offset(w * 0.58f, h * 0.44f), Offset(w * 0.68f, h * 0.50f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                } else {
                    // Happy curved eyes (^ ^)
                    val eyeL = Path().apply {
                        moveTo(w * 0.32f, h * 0.44f)
                        quadraticTo(w * 0.38f, h * 0.36f, w * 0.44f, h * 0.44f)
                    }
                    val eyeR = Path().apply {
                        moveTo(w * 0.56f, h * 0.44f)
                        quadraticTo(w * 0.62f, h * 0.36f, w * 0.68f, h * 0.44f)
                    }
                    drawPath(eyeL, color = eyeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                    drawPath(eyeR, color = eyeColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                }

                // 5. Hands: Alternating typing or waving!
                if (isBusy) {
                    // Mini keyboard below
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(w * 0.25f, h * 0.72f),
                        size = Size(w * 0.50f, h * 0.12f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Alternating tapping hands
                    drawCircle(Color.White, radius = 4f, center = Offset(w * 0.35f, h * 0.70f + typingLeftHand))
                    drawCircle(Color.White, radius = 4f, center = Offset(w * 0.65f, h * 0.70f + typingRightHand))
                } else {
                    // Left resting hand
                    drawCircle(Color.White, radius = 3.5f, center = Offset(w * 0.25f, h * 0.68f))
                    // Right waving hand!
                    drawCircle(Color.White, radius = 4f, center = Offset(w * 0.76f, h * 0.52f + typingRightHand))
                }

                // Floating particle: Spark or Music note
                val pAlpha = (1f - (-musicNoteFloat / 16f)).coerceIn(0f, 1f)
                if (isBusy) {
                    // Yellow energy spark
                    drawCircle(Color(0xFFFBBF24).copy(alpha = pAlpha), radius = 2.5f, center = Offset(w * 0.82f, h * 0.30f + musicNoteFloat))
                } else {
                    // Music note dot
                    drawCircle(Color(0xFFFF5C8A).copy(alpha = pAlpha), radius = 2.5f, center = Offset(w * 0.85f, h * 0.35f + musicNoteFloat))
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Center / Right: Live Speech Bubble & Companion Status
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Archie · PC Companion",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isBusy) CoralSoft else EmeraldSoft)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBusy) "⚡ Sibuk" else "✨ Santai",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBusy) CoralDanger else Emerald
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            Text(
                text = if (isBusy) {
                    "\"Lagi ngetik ngebut! CPU ${cpuUsage.toInt()}% 🚀\""
                } else {
                    "\"Semua adem & stabil, siap kapan saja~ 🍵\""
                },
                fontSize = 11.sp,
                color = InkSoft,
                maxLines = 1
            )
        }
    }
}
