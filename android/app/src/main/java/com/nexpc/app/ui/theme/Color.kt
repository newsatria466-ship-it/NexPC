package com.nexpc.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════
// Nexora v2.4 — "Soft Pastel Bloom & Claymorphic Charm"
// Inspired by Clean Pastel Bento & Warm Glowing Mobile UI
// Primary: Radiant Rose Pink · Healthy: Soft Mint · Alert: Warm Amber & Coral
// Background: Cozy Warm Blush-Cream · Surfaces: Pure White Cards
// ═══════════════════════════════════════════════════════

// ── Neutral Foundations (Warm Creamy Blush Canvas) ────────
val CanvasBg       = Color(0xFFFFF7F9)   // Ultra soft warm blush-cream
val SurfaceWhite   = Color(0xFFFFFFFF)   // Pure white for cards & containers
val SurfaceSoft    = Color(0xFFFFF0F4)   // Soft tinted blush-pink container
val Line           = Color(0xFFFFE4EC)   // Delicate soft pinkish border (1.dp)
val LineSoft       = Color(0xFFFFF0F5)   // Extra subtle dividers

// ── Typography (Soft Charcoal & Rose Accents) ─────────────
val Ink            = Color(0xFF1E293B)   // Slate-900: sharp titles & metrics
val InkSoft        = Color(0xFF475569)   // Slate-600: legible descriptions
val InkMuted       = Color(0xFF94A3B8)   // Slate-400: subtle metadata & placeholders

// ── Color 1: Primary Brand (Radiant Rose Pink) ─────────────
val PrimaryRose    = Color(0xFFFF5C8A)   // Vibrant friendly rose-pink from reference
val RosePrimary    = PrimaryRose
val PrimaryIndigo  = PrimaryRose         // Seamless backward compatibility
val PrimarySoft    = Color(0xFFFFF0F5)   // Soft pastel rose chip background
val PrimaryBorder  = Color(0xFFFFD1DC)   // Soft pastel rose border
val RoseDeep       = Color(0xFFE11D48)   // Darker rose for high-contrast elements
val RoseSoft       = PrimarySoft
val RoseBorder     = PrimaryBorder

// ── Color 2: System Health & Active (Soft Fresh Mint) ──────
val Emerald        = Color(0xFF10B981)   // Fresh healthy green
val EmeraldSoft    = Color(0xFFECFDF5)   // Mint pill container
val EmeraldBorder  = Color(0xFFA7F3D0)
val MintFresh      = Emerald
val MintFreshBg    = EmeraldSoft

// ── Color 3: Warm Sunshine & Warning (Sunny Amber) ────────
val AmberWarm      = Color(0xFFF59E0B)   // Warm golden sun amber
val AmberSoft      = Color(0xFFFEF3C7)   // Sunshine pill background
val AmberBorder    = Color(0xFFFDE68A)
val SunYellow      = AmberWarm
val SunYellowBg    = AmberSoft

// ── Color 4: Alert & Critical (Vibrant Coral/Ruby) ─────────
val CoralDanger    = Color(0xFFF43F5E)   // Playful vibrant coral-red
val CoralSoft      = Color(0xFFFFF1F2)
val CoralBorder    = Color(0xFFFFCCD3)

// ── Color 5: Dreamy Pastel Lavender ────────────────────────
val PastelLavender   = Color(0xFF8B5CF6)
val PastelLavenderBg = Color(0xFFF3E8FF)

// ── Gradients (Warm, Dreamy & Smooth) ─────────────────────
val RoseGradient       = Brush.linearGradient(listOf(Color(0xFFFF5C8A), Color(0xFFFF85A1)))
val AuroraGradient     = RoseGradient
val AuroraSoftGradient = Brush.linearGradient(listOf(Color(0xFFFFF0F5), Color(0xFFFFF7F9)))
val FreshGradient      = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
val SunsetGradient     = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)))
val DreamGradient      = Brush.linearGradient(listOf(Color(0xFFFF85A1), Color(0xFFA78BFA)))
val AuroraHorizontal   = Brush.horizontalGradient(listOf(Color(0xFFFF5C8A), Color(0xFFFF85A1)))

// ═══════════════════════════════════════════════════════
// Unified Color Tokens (Harmonizing all previous pastels)
// Maps previous 8+ rainbow pastels cleanly to the 3-tone system!
// ═══════════════════════════════════════════════════════

// Primary family
val Indigo     = PrimaryIndigo
val IndigoBg   = PrimarySoft
val Violet     = PrimaryIndigo
val VioletBg   = PrimarySoft
val Pink       = PrimaryIndigo
val PinkBg     = PrimarySoft
val Lavender   = PrimaryIndigo
val LavenderBg = PrimarySoft

// Healthy / Fresh family
val Mint       = Emerald
val MintBg     = EmeraldSoft
val Sky        = Emerald
val SkyBg      = EmeraldSoft

// Alert / Warning / Accent family
val Lemon      = AmberWarm
val LemonBg    = AmberSoft
val Peach      = AmberWarm
val PeachBg    = AmberSoft
val Coral      = CoralDanger
val CoralBg    = CoralSoft

// Legacy Aliases
val BgLight          = CanvasBg
val BgDark           = CanvasBg
val SurfaceCard      = SurfaceWhite
val SurfaceDark      = SurfaceWhite
val SurfaceElevated  = SurfaceSoft
val CardBorder       = Line
val CardBorderSubtle = LineSoft

val BlueAccent  = PrimaryIndigo
val BlueSubtle  = PrimarySoft
val BlueMuted   = PrimaryBorder

val PurpleAccent = PrimaryIndigo
val PurpleSubtle = PrimarySoft
val PurpleMuted  = PrimaryBorder

val EmeraldGreen = Emerald
val GreenSubtle  = EmeraldSoft
val GreenMuted   = EmeraldBorder

val AmberWarning = AmberWarm
val AmberSubtle  = AmberSoft
val AmberMuted   = AmberBorder

val CrimsonDanger = CoralDanger
val CrimsonSubtle = CoralSoft
val CrimsonMuted  = CoralBorder

val CyanAccent = Emerald
val CyanSubtle = EmeraldSoft
val CyanGlow   = EmeraldSoft

val TextPrimary   = Ink
val TextSecondary = InkSoft
val TextMuted     = InkMuted
