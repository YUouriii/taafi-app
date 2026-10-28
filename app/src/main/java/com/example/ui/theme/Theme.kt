package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BreakFreeColorScheme = darkColorScheme(
    primary = GreenCalm,
    onPrimary = OnGreenCalm,
    primaryContainer = GreenCalmContainer,
    onPrimaryContainer = GreenCalmLight,
    secondary = WhiteGlowing,
    onSecondary = PureBlack,
    secondaryContainer = BlackElevated,
    onSecondaryContainer = WhiteGlowing,
    tertiary = GreenCalmLight,
    onTertiary = PureBlack,
    tertiaryContainer = GreenCalmContainer,
    onTertiaryContainer = WhiteGlowing,
    error = WhiteGlowing,
    onError = PureBlack,
    errorContainer = BlackElevated,
    onErrorContainer = WhiteGlowing,
    background = PureBlack,
    onBackground = WhiteGlowing,
    surface = BlackCard,
    onSurface = WhiteGlowing,
    surfaceVariant = BlackElevated,
    onSurfaceVariant = WhiteDim,
    outline = BlackBorder,
    outlineVariant = WhiteSubtleBorder
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
