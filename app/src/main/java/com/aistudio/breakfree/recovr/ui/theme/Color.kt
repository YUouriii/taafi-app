package com.aistudio.breakfree.recovr.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// THEME STATE
//
// The palette is swapped at runtime (dark / light / system) through this holder.
// The top-level colour values below are thin getters over it, so every screen keeps
// reading `TextPrimary`, `BlackSurface` and friends exactly as before, while the
// actual colour follows the user's choice.
// =========================================================================
data class BreakFreeColors(
    // Depth
    val PureBlack: Color,
    val BlackSurface: Color,
    val BlackCard: Color,
    val BlackElevated: Color,
    val BlackBorder: Color,
    val BlackBorderStrong: Color,
    // Light
    val WhiteGlowing: Color,
    val WhiteDim: Color,
    val WhiteMuted: Color,
    val WhiteGlow: Color,
    val WhiteGlowBorder: Color,
    val WhiteSubtleBorder: Color,
    // Mint accent
    val GreenCalm: Color,
    val GreenCalmLight: Color,
    val GreenCalmDark: Color,
    val GreenCalmContainer: Color,
    val GreenCalmGlow: Color,
    val GreenCalmBorder: Color,
    val OnGreenCalm: Color,
    // Secondary accents
    val AuroraTeal: Color,
    val SoftGold: Color,
    val SoftCoral: Color,
    val RoseWarningDark: Color,
    val DangerRed: Color,
    val DangerRedDark: Color,
    val OnDanger: Color,
    // Gradients
    val BackgroundTop: Color,
    val BackgroundBottom: Color,
    val CardGradientTop: Color,
    val CardGradientBottom: Color,
    val HeroGradientTop: Color,
    val HeroGradientBottom: Color
)

val DarkBreakFreeColors = BreakFreeColors(
    PureBlack = Color(0xFF050806),
    BlackSurface = Color(0xFF0A100D),
    BlackCard = Color(0xFF111A16),
    BlackElevated = Color(0xFF17231D),
    BlackBorder = Color(0xFF223129),
    BlackBorderStrong = Color(0xFF2E4539),
    WhiteGlowing = Color(0xFFEDF6F1),
    WhiteDim = Color(0xFFA8BDB3),
    WhiteMuted = Color(0xFF7A9187),
    WhiteGlow = Color(0x33EDF6F1),
    WhiteGlowBorder = Color(0x38EDF6F1),
    WhiteSubtleBorder = Color(0x1FEDF6F1),
    GreenCalm = Color(0xFF34D399),
    GreenCalmLight = Color(0xFF6EE7B7),
    GreenCalmDark = Color(0xFF0B3B2A),
    GreenCalmContainer = Color(0xFF0E2B21),
    GreenCalmGlow = Color(0x3334D399),
    GreenCalmBorder = Color(0x5534D399),
    OnGreenCalm = Color(0xFF04140E),
    AuroraTeal = Color(0xFF5EEAD4),
    SoftGold = Color(0xFFFCD34D),
    SoftCoral = Color(0xFFFB7185),
    RoseWarningDark = Color(0xFF3B1220),
    DangerRed = Color(0xFFE11D48),
    DangerRedDark = Color(0xFFBE123C),
    OnDanger = Color(0xFFFFFFFF),
    BackgroundTop = Color(0xFF0B1410),
    BackgroundBottom = Color(0xFF050806),
    CardGradientTop = Color(0xFF14201A),
    CardGradientBottom = Color(0xFF0D1512),
    HeroGradientTop = Color(0xFF123027),
    HeroGradientBottom = Color(0xFF0B1813)
)

val LightBreakFreeColors = BreakFreeColors(
    PureBlack = Color(0xFF050806),
    BlackSurface = Color(0xFFF4F7F5),
    BlackCard = Color(0xFFFFFFFF),
    BlackElevated = Color(0xFFECF1EE),
    BlackBorder = Color(0xFFD9E2DD),
    BlackBorderStrong = Color(0xFFBFCFC6),
    WhiteGlowing = Color(0xFF0F1A15),
    WhiteDim = Color(0xFF4B5E55),
    WhiteMuted = Color(0xFF7A8F85),
    WhiteGlow = Color(0x330F1A15),
    WhiteGlowBorder = Color(0x380F1A15),
    WhiteSubtleBorder = Color(0x1F0F1A15),
    GreenCalm = Color(0xFF059669),
    GreenCalmLight = Color(0xFF10B981),
    GreenCalmDark = Color(0xFFD1FAE5),
    GreenCalmContainer = Color(0xFFECFDF5),
    GreenCalmGlow = Color(0x33059669),
    GreenCalmBorder = Color(0x55059669),
    OnGreenCalm = Color(0xFFFFFFFF),
    AuroraTeal = Color(0xFF0D9488),
    SoftGold = Color(0xFFB45309),
    SoftCoral = Color(0xFFFB7185),
    RoseWarningDark = Color(0xFFFCE4EC),
    DangerRed = Color(0xFFE11D48),
    DangerRedDark = Color(0xFFBE123C),
    OnDanger = Color(0xFFFFFFFF),
    BackgroundTop = Color(0xFFF4F7F5),
    BackgroundBottom = Color(0xFFE8EDEA),
    CardGradientTop = Color(0xFFFFFFFF),
    CardGradientBottom = Color(0xFFF4F7F5),
    HeroGradientTop = Color(0xFFECFDF5),
    HeroGradientBottom = Color(0xFFD1FAE5)
)

object BreakFreeThemeState {
    val colors = mutableStateOf(DarkBreakFreeColors)
}

// =========================================================================
// TOP-LEVEL COLOUR VALUES
//
// Kept as getters so every existing call site (`TextPrimary`, `BlackSurface`, ...) keeps
// compiling and reading while the underlying palette follows the chosen theme.
// =========================================================================

// 1. DEPTH
val PureBlack: Color get() = BreakFreeThemeState.colors.value.PureBlack
val BlackSurface: Color get() = BreakFreeThemeState.colors.value.BlackSurface
val BlackCard: Color get() = BreakFreeThemeState.colors.value.BlackCard
val BlackElevated: Color get() = BreakFreeThemeState.colors.value.BlackElevated
val BlackBorder: Color get() = BreakFreeThemeState.colors.value.BlackBorder
val BlackBorderStrong: Color get() = BreakFreeThemeState.colors.value.BlackBorderStrong

// 2. LIGHT
val WhiteGlowing: Color get() = BreakFreeThemeState.colors.value.WhiteGlowing
val WhiteDim: Color get() = BreakFreeThemeState.colors.value.WhiteDim
val WhiteMuted: Color get() = BreakFreeThemeState.colors.value.WhiteMuted
val WhiteGlow: Color get() = BreakFreeThemeState.colors.value.WhiteGlow
val WhiteGlowBorder: Color get() = BreakFreeThemeState.colors.value.WhiteGlowBorder
val WhiteSubtleBorder: Color get() = BreakFreeThemeState.colors.value.WhiteSubtleBorder

// 3. MINT
val GreenCalm: Color get() = BreakFreeThemeState.colors.value.GreenCalm
val GreenCalmLight: Color get() = BreakFreeThemeState.colors.value.GreenCalmLight
val GreenCalmDark: Color get() = BreakFreeThemeState.colors.value.GreenCalmDark
val GreenCalmContainer: Color get() = BreakFreeThemeState.colors.value.GreenCalmContainer
val GreenCalmGlow: Color get() = BreakFreeThemeState.colors.value.GreenCalmGlow
val GreenCalmBorder: Color get() = BreakFreeThemeState.colors.value.GreenCalmBorder
val OnGreenCalm: Color get() = BreakFreeThemeState.colors.value.OnGreenCalm

// 4. SECONDARY ACCENTS
val AuroraTeal: Color get() = BreakFreeThemeState.colors.value.AuroraTeal
val SoftGold: Color get() = BreakFreeThemeState.colors.value.SoftGold
val SoftCoral: Color get() = BreakFreeThemeState.colors.value.SoftCoral
val DangerRed: Color get() = BreakFreeThemeState.colors.value.DangerRed
val DangerRedDark: Color get() = BreakFreeThemeState.colors.value.DangerRedDark
val OnDanger: Color get() = BreakFreeThemeState.colors.value.OnDanger

// =========================================================================
// GRADIENTS & SURFACES
// =========================================================================
val BackgroundTop: Color get() = BreakFreeThemeState.colors.value.BackgroundTop
val BackgroundBottom: Color get() = BreakFreeThemeState.colors.value.BackgroundBottom
val CardGradientTop: Color get() = BreakFreeThemeState.colors.value.CardGradientTop
val CardGradientBottom: Color get() = BreakFreeThemeState.colors.value.CardGradientBottom
val HeroGradientTop: Color get() = BreakFreeThemeState.colors.value.HeroGradientTop
val HeroGradientBottom: Color get() = BreakFreeThemeState.colors.value.HeroGradientBottom

// =========================================================================
// COMPATIBILITY MAPPINGS
// =========================================================================
val MidnightNavy: Color get() = BreakFreeThemeState.colors.value.BlackSurface
val SlateNavy: Color get() = BreakFreeThemeState.colors.value.BlackCard
val ElevatedNavy: Color get() = BreakFreeThemeState.colors.value.BlackElevated
val DarkNavyBorder: Color get() = BreakFreeThemeState.colors.value.BlackBorder
val DarkNavyBackground: Color get() = BreakFreeThemeState.colors.value.BlackSurface
val NavySurface: Color get() = BreakFreeThemeState.colors.value.BlackCard
val NavySurfaceVariant: Color get() = BreakFreeThemeState.colors.value.BlackElevated
val NavyBorder: Color get() = BreakFreeThemeState.colors.value.BlackBorder
val GlassCardBg: Color get() = BreakFreeThemeState.colors.value.BlackCard
val GlassCardBackground: Color get() = BreakFreeThemeState.colors.value.BlackCard
val GlassBottomBarBg: Color get() = BreakFreeThemeState.colors.value.BlackSurface

val EmeraldGreen: Color get() = BreakFreeThemeState.colors.value.GreenCalm
val EmeraldLight: Color get() = BreakFreeThemeState.colors.value.GreenCalmLight
val EmeraldGlow: Color get() = BreakFreeThemeState.colors.value.GreenCalmGlow
val EmeraldContainer: Color get() = BreakFreeThemeState.colors.value.GreenCalmContainer
val OnEmerald: Color get() = BreakFreeThemeState.colors.value.OnGreenCalm
val EmeraldPrimary: Color get() = BreakFreeThemeState.colors.value.GreenCalm

val RoseAlert: Color get() = BreakFreeThemeState.colors.value.SoftCoral
val RoseWarning: Color get() = BreakFreeThemeState.colors.value.SoftCoral
val RoseWarningDark: Color get() = BreakFreeThemeState.colors.value.RoseWarningDark
val RoseContainer: Color get() = BreakFreeThemeState.colors.value.SoftCoral.copy(alpha = 0.13f)
val AmberGold: Color get() = BreakFreeThemeState.colors.value.SoftGold
val AmberAlert: Color get() = BreakFreeThemeState.colors.value.SoftGold
val AmberContainer: Color get() = BreakFreeThemeState.colors.value.SoftGold.copy(alpha = 0.13f)
val CyanAccent: Color get() = BreakFreeThemeState.colors.value.AuroraTeal

val TextPrimary: Color get() = BreakFreeThemeState.colors.value.WhiteGlowing
val TextSecondary: Color get() = BreakFreeThemeState.colors.value.WhiteDim
val TextMuted: Color get() = BreakFreeThemeState.colors.value.WhiteMuted

val GlassBorderSubtle: Color get() = BreakFreeThemeState.colors.value.WhiteSubtleBorder
val GlassBorderEmerald: Color get() = BreakFreeThemeState.colors.value.GreenCalmBorder
val GlassBorderRose: Color get() = BreakFreeThemeState.colors.value.SoftCoral.copy(alpha = 0.33f)
val GlassCardBorder: Color get() = BreakFreeThemeState.colors.value.GreenCalmBorder
val GlassWarningBorder: Color get() = BreakFreeThemeState.colors.value.SoftCoral.copy(alpha = 0.33f)
