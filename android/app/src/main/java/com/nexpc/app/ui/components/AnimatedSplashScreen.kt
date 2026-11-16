package com.nexpc.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.R
import com.nexpc.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    onSplashFinished: () -> Unit
) {
    var animationStarted by remember { mutableStateOf(false) }

    // Spring scale for mascot / logo
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(400),
        label = "logoAlpha"
    )

    // Progress bar fill
    val progress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(1200, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "splashProgress"
    )

    // Pulse shockwave ring
    val pulseTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseRing by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRing"
    )
    val ringAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(1700)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBg),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background aura / circle
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(pulseRing)
                .clip(CircleShape)
                .border(2.dp, PrimaryRose.copy(alpha = ringAlpha), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Mascot & App Icon container with spring bounce
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .size(118.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft glowing background circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(RoseSoft)
                        .border(1.5.dp, RoseBorder, CircleShape)
                )

                // 3D Blush Mascot peeking with high energy!
                BlushBallMascot(
                    size = 90.dp,
                    isWinking = true
                )
            }

            Spacer(Modifier.height(24.dp))

            // App Brand Name with animated letter spacing & soft tone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.scale(logoScale)
            ) {
                Text(
                    text = "nexora",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryRose,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "✨",
                    fontSize = 26.sp
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Arch Linux Remote Companion",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = InkMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // Smooth Pill Progress Bar
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(50))
                        .background(RoseGradient)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (progress < 0.9f) "Menghubungkan ke sistem... ✨" else "Siap! 🚀",
                fontSize = 11.sp,
                color = InkSoft,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
