package com.nexpc.app.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════
// Neo-Brutalism Design Tokens (Windows Adaptive Theme)
// Punchy, High-Contrast, Bold Borders & Hard Drop Shadows
// ═══════════════════════════════════════════════════════

val NeoCanvas    = Color(0xFFF7F5EE)   // Warm retro paper cream
val NeoBlack     = Color(0xFF141416)   // Solid ink black for borders & shadows
val NeoWhite     = Color(0xFFFFFFFF)   // Pure white card surfaces
val NeoYellow    = Color(0xFFFFDE59)   // Electric Canary Yellow
val NeoLime      = Color(0xFFA3E635)   // Acid Lime Green (Active / Healthy)
val NeoCyan      = Color(0xFF38BDF8)   // Electric Sky / Cyan
val NeoPink      = Color(0xFFFF6B8B)   // Pop Bubblegum Pink
val NeoOrange    = Color(0xFFFB923C)   // Safety Tangerine Orange
val NeoPurple    = Color(0xFFC084FC)   // Vivid Lavender / Purple
val NeoDarkCard  = Color(0xFF1E1E24)   // Contrast card surface

val NeoBorderThickness = 2.5.dp
val NeoDefaultShadow   = 4.dp
val NeoCornerRadius    = 14.dp

/**
 * Standard Neo-Brutalist Card with solid black border and hard drop shadow.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoWhite,
    borderColor: Color = NeoBlack,
    shadowColor: Color = NeoBlack,
    shadowOffset: Dp = NeoDefaultShadow,
    cornerRadius: Dp = NeoCornerRadius,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Hard Shadow Layer (Offset Box)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )
        // Foreground Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, shape)
                .border(NeoBorderThickness, borderColor, shape),
            content = content
        )
    }
}

/**
 * Interactive Neo-Brutalist Button with tactile mechanical press effect.
 */
@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoYellow,
    contentColor: Color = NeoBlack,
    borderColor: Color = NeoBlack,
    cornerRadius: Dp = 12.dp,
    shadowOffset: Dp = 4.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) 0.dp else shadowOffset,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "neoButtonOffset"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Shadow (visible when not pressed)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(NeoBlack, shape)
        )

        // Foreground Action Area
        Row(
            modifier = Modifier
                .offset(x = shadowOffset - currentOffset, y = shadowOffset - currentOffset)
                .clip(shape)
                .background(backgroundColor)
                .border(NeoBorderThickness, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Neo-Brutalist Pill Badge with black border and heavy monospace label.
 */
@Composable
fun NeoBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoYellow,
    textColor: Color = NeoBlack,
    borderColor: Color = NeoBlack
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(2.dp, borderColor, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Compact Neo-Brutalist Stat Box for hardware metrics.
 */
@Composable
fun NeoMetricBox(
    label: String,
    value: String,
    unit: String = "",
    accentColor: Color = NeoCyan,
    modifier: Modifier = Modifier
) {
    NeoCard(
        modifier = modifier,
        backgroundColor = NeoWhite,
        cornerRadius = 12.dp,
        shadowOffset = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeoBlack.copy(alpha = 0.7f)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, RoundedCornerShape(3.dp))
                        .border(1.5.dp, NeoBlack, RoundedCornerShape(3.dp))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeoBlack
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBlack.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

