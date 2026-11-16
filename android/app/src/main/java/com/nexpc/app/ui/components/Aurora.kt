package com.nexpc.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexpc.app.ui.theme.*

// ═══════════════════════════════════════════════════════
// Aurora Pop component kit
// ═══════════════════════════════════════════════════════

/** Press-scale modifier for tactile feedback. */
@Composable
fun Modifier.pressScale(interaction: MutableInteractionSource): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.95f else 1f, tween(120), label = "press")
    return this.scale(s)
}

/** Vibrant gradient hero card (use once per screen). */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    brush: Brush = AuroraGradient,
    contentPadding: PaddingValues = PaddingValues(22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
    ) {
        // Decorative soft circles (cheap: two solid translucent shapes)
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = 50.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/** Translucent white chip used on top of hero gradients. */
@Composable
fun HeroChip(label: String, value: String? = null, icon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        if (value != null) {
            Spacer(Modifier.width(5.dp))
            Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Colored rounded icon container. */
@Composable
fun IconBubble(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(size * 0.34f)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Pastel bento tile with big number + mini progress. */
@Composable
fun BentoTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    progress: Float? = null,
    onClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val animated by animateFloatAsState((progress ?: 0f).coerceIn(0f, 1f), tween(500), label = "bento")
    Column(
        modifier = modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(1.dp, Line, RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable(interaction, null) { onClick() } else Modifier)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon, tint = accent, background = Color.White.copy(alpha = 0.7f), size = 32.dp, iconSize = 17.dp)
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink.copy(alpha = 0.75f), maxLines = 1)
        }
        Spacer(Modifier.height(14.dp))
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Ink, maxLines = 1, letterSpacing = (-0.5).sp)
        if (subtitle != null) {
            Text(subtitle, fontSize = 11.sp, color = Ink.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (progress != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.7f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animated)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                )
            }
        }
    }
}

/** Round colorful action button with label underneath. */
@Composable
fun ActionBubble(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .pressScale(interaction)
            .clickable(interaction, null, enabled = enabled) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (enabled) color else InkMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = InkSoft, maxLines = 1)
    }
}

/** Small colored shortcut tile (icon + label) for navigation grids. */
@Composable
fun ShortcutTile(
    label: String,
    icon: ImageVector,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(interaction, null) { onClick() }
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
    }
}

/** Pill-style segmented control. */
@Composable
fun SegmentedPill(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<ImageVector>? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceSoft)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            val bg by animateColorAsState(if (selected) SurfaceWhite else Color.Transparent, tween(180), label = "segBg")
            val fg by animateColorAsState(if (selected) Indigo else InkSoft, tween(180), label = "segFg")
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .then(if (selected) Modifier.border(1.dp, Line, RoundedCornerShape(50)) else Modifier)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icons?.getOrNull(i)?.let {
                    Icon(it, null, tint = fg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Rounded search field. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(50))
            .background(SurfaceWhite)
            .border(1.dp, Line, RoundedCornerShape(50))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, null, tint = Indigo, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = InkMuted, fontSize = 14.sp, maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Ink, fontSize = 14.sp, fontFamily = Jakarta),
                cursorBrush = SolidColor(Indigo),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Default.Close, "Clear", tint = InkMuted,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") }
            )
        }
        trailing?.let {
            Spacer(Modifier.width(6.dp))
            it()
        }
    }
}

/** Section title row with optional action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
            if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = InkMuted)
        }
        if (action != null && onAction != null) {
            Text(
                action,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Indigo,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onAction() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

/** Light list row: icon bubble + title/subtitle + trailing slot. */
@Composable
fun ListRow(
    title: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon, tint = accent, background = accentBg, size = 42.dp, iconSize = 21.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = InkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke(this)
    }
}

/** Small colored tag/badge. */
@Composable
fun Tag(text: String, color: Color, background: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** Big friendly empty state. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accent: Color = Indigo,
    accentBg: Color = IndigoBg
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBubble(icon, tint = accent, background = accentBg, size = 72.dp, iconSize = 34.dp, shape = CircleShape)
        Spacer(Modifier.height(14.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = InkMuted)
    }
}

/** Thin rounded progress bar. */
@Composable
fun PillProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Indigo,
    track: Color = SurfaceSoft,
    height: Dp = 8.dp,
    brush: Brush? = null
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(400), label = "pill")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .then(if (brush != null) Modifier.background(brush) else Modifier.background(color))
        )
    }
}

/** Primary gradient pill button. */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    brush: Brush = AuroraHorizontal,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(interaction)
            .height(50.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) brush else SolidColor(InkMuted))
            .clickable(interaction, null, enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

/** Returns accent/background pair by a file name/extension. */
fun fileAccent(name: String, isDir: Boolean): Pair<Color, Color> {
    if (isDir) return Lemon to LemonBg
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "heic" -> Pink to PinkBg
        "mp4", "mkv", "avi", "mov", "webm" -> Coral to CoralBg
        "mp3", "wav", "flac", "ogg", "m4a" -> Violet to VioletBg
        "zip", "tar", "gz", "xz", "7z", "rar", "zst" -> Lavender to LavenderBg
        "apk", "deb", "rpm", "appimage", "sh" -> Mint to MintBg
        "pdf", "doc", "docx", "txt", "md", "odt", "xls", "xlsx", "ppt", "pptx" -> Sky to SkyBg
        "kt", "py", "js", "ts", "java", "c", "cpp", "rs", "go", "json", "xml", "html", "css" -> Indigo to IndigoBg
        else -> Sky to SkyBg
    }
}
