package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// STRICT THREE-COLOR THEME: Black, White Glowing, Green Calm
// =========================================================================

// 1. BLACK (Deep pure black and dark elevated surfaces)
val PureBlack = Color(0xFF000000)            // Deep background
val BlackSurface = Color(0xFF080D0A)         // Primary surface
val BlackCard = Color(0xFF0E1511)            // Cards & containers
val BlackElevated = Color(0xFF141E18)        // Elevated dialogs & sheets
val BlackBorder = Color(0xFF1D2A21)          // Subtle structural borders

// 2. WHITE GLOWING (Pure crisp white with luminous glowing accents)
val WhiteGlowing = Color(0xFFFFFFFF)         // Glowing white text & primary accents
val WhiteDim = Color(0xCCFFFFFF)             // High-contrast readable secondary text
val WhiteMuted = Color(0x80FFFFFF)           // Muted labels & placeholders
val WhiteGlow = Color(0x4DFFFFFF)            // Glowing white luminescence
val WhiteGlowBorder = Color(0x38FFFFFF)      // Glowing white rim stroke
val WhiteSubtleBorder = Color(0x1FFFFFFF)    // Subtle white glassmorphic border

// 3. GREEN CALM (Soothing, serene healing green)
val GreenCalm = Color(0xFF10B981)            // Serene emerald / calm mint green
val GreenCalmLight = Color(0xFF34D399)       // Bright calm green highlight
val GreenCalmDark = Color(0xFF064E3B)        // Deep calm green
val GreenCalmContainer = Color(0xFF032B1F)   // Calm green pill container
val GreenCalmGlow = Color(0x3310B981)        // Gentle calm green aura
val GreenCalmBorder = Color(0x4D10B981)      // Calm green border
val OnGreenCalm = Color(0xFF021B11)          // Text on calm green

// =========================================================================
// COMPATIBILITY MAPPINGS: Mapped strictly to Black, White Glowing, Green Calm
// =========================================================================

// Surfaces & Backgrounds -> Black
val MidnightNavy = PureBlack
val SlateNavy = BlackCard
val ElevatedNavy = BlackElevated
val DarkNavyBorder = BlackBorder
val DarkNavyBackground = PureBlack
val NavySurface = BlackCard
val NavySurfaceVariant = BlackElevated
val NavyBorder = BlackBorder
val GlassCardBg = BlackCard
val GlassCardBackground = BlackCard
val GlassBottomBarBg = Color(0xF2070C09)

// Accent & Primary -> Green Calm
val EmeraldGreen = GreenCalm
val EmeraldLight = GreenCalmLight
val EmeraldGlow = GreenCalmGlow
val EmeraldContainer = GreenCalmContainer
val OnEmerald = OnGreenCalm
val EmeraldPrimary = GreenCalm

// Alerts & Urgent States -> White Glowing / Green Calm (Strictly No Red/Amber/Blue)
val RoseAlert = WhiteGlowing
val RoseWarning = WhiteGlowing
val RoseWarningDark = BlackElevated
val RoseContainer = Color(0x22FFFFFF)
val AmberGold = WhiteGlowing
val AmberAlert = WhiteGlowing
val AmberContainer = Color(0x22FFFFFF)
val CyanAccent = GreenCalmLight

// Typography -> White Glowing / Dim / Muted
val TextPrimary = WhiteGlowing
val TextSecondary = WhiteDim
val TextMuted = WhiteMuted

// Glassmorphism borders -> White Glowing or Green Calm
val GlassBorderSubtle = WhiteSubtleBorder
val GlassBorderEmerald = GreenCalmBorder
val GlassBorderRose = WhiteGlowBorder
val GlassCardBorder = GreenCalmBorder
val GlassWarningBorder = WhiteGlowBorder
