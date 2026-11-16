package com.nexpc.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexpc.app.ui.theme.CardBorder
import com.nexpc.app.ui.theme.SurfaceCard

/**
 * Aurora card — white rounded card with a hairline border.
 * Flat (no blur, no heavy shadow) for smooth scrolling.
 * Name kept as GlassCard to avoid breaking existing call-sites.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = SurfaceCard,
    borderColor: Color = CardBorder,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = shadowElevation,
        tonalElevation = 0.dp
    ) {
        Box {
            content()
        }
    }
}
