package com.nexpc.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AuroraScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoBg,
    onPrimaryContainer = Indigo,
    secondary = Violet,
    onSecondary = Color.White,
    secondaryContainer = VioletBg,
    onSecondaryContainer = Violet,
    tertiary = Pink,
    onTertiary = Color.White,
    tertiaryContainer = PinkBg,
    onTertiaryContainer = Pink,
    background = CanvasBg,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = InkSoft,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = SurfaceSoft,
    surfaceContainerLow = CanvasBg,
    outline = Line,
    outlineVariant = LineSoft,
    error = Coral,
    onError = Color.White,
    errorContainer = CoralBg,
    onErrorContainer = Coral,
    inverseSurface = Ink,
    inverseOnSurface = SurfaceWhite,
    scrim = Color(0xFF1E1B3A).copy(alpha = 0.28f)
)

private val AuroraShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NexPCTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = CanvasBg.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = CanvasBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = AuroraScheme,
        typography = Typography,
        shapes = AuroraShapes,
        content = content
    )
}
