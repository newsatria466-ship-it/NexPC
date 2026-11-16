package com.nexpc.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tactile bouncy click modifier that squishes slightly on press
 * and springs back with a lively, organic bounce on release.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            onClick()
        }
}

/**
 * Continuous subtle floating/breathing animation for mascots.
 */
fun Modifier.breatheFloat(
    minScale: Float = 0.96f,
    maxScale: Float = 1.04f,
    durationMillis: Int = 1800
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "breatheTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Continuous smooth vertical floating bob motion (sinusoidal).
 */
fun Modifier.floatingBob(
    offsetY: Float = 8f,
    durationMillis: Int = 2200
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "floatingTransition")
    val translationY by infiniteTransition.animateFloat(
        initialValue = -offsetY,
        targetValue = offsetY,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingY"
    )

    this.graphicsLayer {
        this.translationY = translationY
    }
}

/**
 * Continuous gentle swaying rotation motion for characters and icons.
 */
fun Modifier.gentleSway(
    maxDegrees: Float = 4f,
    durationMillis: Int = 2400
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "swayTransition")
    val rotationZ by infiniteTransition.animateFloat(
        initialValue = -maxDegrees,
        targetValue = maxDegrees,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swayRotation"
    )

    this.graphicsLayer {
        this.rotationZ = rotationZ
    }
}

/**
 * Continuous animated shimmering sweep across container surfaces.
 */
fun Modifier.shimmerSweep(
    highlightColor: Color = Color.White.copy(alpha = 0.35f),
    durationMillis: Int = 2600
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    this.drawWithContent {
        drawContent()
        val width = size.width
        val x = progress * width
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                highlightColor,
                Color.Transparent
            ),
            start = Offset(x - width * 0.4f, 0f),
            end = Offset(x + width * 0.4f, size.height)
        )
        drawRect(brush = brush)
    }
}
