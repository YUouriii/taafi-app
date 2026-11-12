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

// "night ink" — the site's dark theme: the journal after dark.
// paper #16120D · ink #ECE3CF · dawn #E0662F · sage #8FB3A0 · night #0F0C09
val DarkBreakFreeColors = BreakFreeColors(
    PureBlack = Color(0xFF0F0C09),
    BlackSurface = Color(0xFF16120D),
    BlackCard = Color(0xFF1F1912),
    BlackElevated = Color(0xFF262015),
    BlackBorder = Color(0x26ECE3CF),
    BlackBorderStrong = Color(0x6BECE3CF),
    WhiteGlowing = Color(0xFFECE3CF),
    WhiteDim = Color(0xFFA2937A),
    WhiteMuted = Color(0xFF877963),
    WhiteGlow = Color(0x2EECE3CF),
    WhiteGlowBorder = Color(0x42ECE3CF),
    WhiteSubtleBorder = Color(0x26ECE3CF),
    GreenCalm = Color(0xFFE0662F),
    GreenCalmLight = Color(0xFFF2A877),
    GreenCalmDark = Color(0xFFEF8F57),
    GreenCalmContainer = Color(0xFF2E1A10),
    GreenCalmGlow = Color(0x33E0662F),
    GreenCalmBorder = Color(0x55E0662F),
    OnGreenCalm = Color(0xFFFDF7EC),
    AuroraTeal = Color(0xFF8FB3A0),
    SoftGold = Color(0xFFD9A85F),
    SoftCoral = Color(0xFFE08A6D),
    RoseWarningDark = Color(0xFF2E1810),
    DangerRed = Color(0xFFE4593C),
    DangerRedDark = Color(0xFFC44327),
    OnDanger = Color(0xFFFDF7EC),
    BackgroundTop = Color(0xFF16120D),
    BackgroundBottom = Color(0xFF0F0C09),
    CardGradientTop = Color(0xFF221C13),
    CardGradientBottom = Color(0xFF1A150E),
    HeroGradientTop = Color(0xFF2B1C10),
    HeroGradientBottom = Color(0xFF16120D)
)

// "dawn" — the site's light theme: a recovery journal at dawn.
// paper #F6F1E7 · ink #211D16 · dawn #C14E24 · sage #41624F · night #171310
val LightBreakFreeColors = BreakFreeColors(
    PureBlack = Color(0xFF171310),
    BlackSurface = Color(0xFFF6F1E7),
    BlackCard = Color(0xFFFBF6EC),
    BlackElevated = Color(0xFFEFE6D3),
    BlackBorder = Color(0x29211D16),
    BlackBorderStrong = Color(0x6B211D16),
    WhiteGlowing = Color(0xFF211D16),
    WhiteDim = Color(0xFF6E6554),
    WhiteMuted = Color(0xFF8B7F6B),
    WhiteGlow = Color(0x2E211D16),
    WhiteGlowBorder = Color(0x42211D16),
    WhiteSubtleBorder = Color(0x29211D16),
    GreenCalm = Color(0xFFC14E24),
    GreenCalmLight = Color(0xFFE8824C),
    GreenCalmDark = Color(0xFF9A3B1A),
    GreenCalmContainer = Color(0xFFF7E4D7),
    GreenCalmGlow = Color(0x33C14E24),
    GreenCalmBorder = Color(0x55C14E24),
    OnGreenCalm = Color(0xFFFDF7EC),
    AuroraTeal = Color(0xFF41624F),
    SoftGold = Color(0xFFA16207),
    SoftCoral = Color(0xFFB4552D),
    RoseWarningDark = Color(0xFFF6E2D7),
    DangerRed = Color(0xFFB23A20),
    DangerRedDark = Color(0xFF8F2E19),
    OnDanger = Color(0xFFFDF7EC),
    BackgroundTop = Color(0xFFF6F1E7),
    BackgroundBottom = Color(0xFFEBE2CF),
    CardGradientTop = Color(0xFFFCF8EF),
    CardGradientBottom = Color(0xFFF3ECDD),
    HeroGradientTop = Color(0xFFF6E7D2),
    HeroGradientBottom = Color(0xFFEFE2CB)
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
