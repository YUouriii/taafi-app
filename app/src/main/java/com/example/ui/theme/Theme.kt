package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BreakFreeColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = OnEmerald,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldLight,
    secondary = CyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF81ECFF),
    tertiary = RoseWarning,
    onTertiary = Color.White,
    tertiaryContainer = RoseWarningDark,
    onTertiaryContainer = Color(0xFFFFD9DF),
    error = RoseWarning,
    onError = Color.White,
    errorContainer = RoseWarningDark,
    onErrorContainer = Color(0xFFFFD9DF),
    background = DarkNavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = GlassCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BreakFreeColorScheme,
        typography = Typography,
        content = content
    )
}
