package com.aistudio.breakfree.recovr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val BreakFreeDarkColorScheme = darkColorScheme(
    primary = DarkBreakFreeColors.GreenCalm,
    onPrimary = DarkBreakFreeColors.OnGreenCalm,
    primaryContainer = DarkBreakFreeColors.GreenCalmContainer,
    onPrimaryContainer = DarkBreakFreeColors.GreenCalmLight,
    secondary = DarkBreakFreeColors.AuroraTeal,
    onSecondary = DarkBreakFreeColors.PureBlack,
    secondaryContainer = DarkBreakFreeColors.BlackElevated,
    onSecondaryContainer = DarkBreakFreeColors.WhiteGlowing,
    tertiary = DarkBreakFreeColors.SoftGold,
    onTertiary = DarkBreakFreeColors.PureBlack,
    tertiaryContainer = DarkBreakFreeColors.BlackElevated,
    onTertiaryContainer = DarkBreakFreeColors.WhiteGlowing,
    error = DarkBreakFreeColors.DangerRed,
    onError = DarkBreakFreeColors.OnDanger,
    errorContainer = DarkBreakFreeColors.RoseWarningDark,
    onErrorContainer = DarkBreakFreeColors.SoftCoral,
    background = DarkBreakFreeColors.BlackSurface,
    onBackground = DarkBreakFreeColors.WhiteGlowing,
    surface = DarkBreakFreeColors.BlackCard,
    onSurface = DarkBreakFreeColors.WhiteGlowing,
    surfaceVariant = DarkBreakFreeColors.BlackElevated,
    onSurfaceVariant = DarkBreakFreeColors.WhiteDim,
    outline = DarkBreakFreeColors.BlackBorderStrong,
    outlineVariant = DarkBreakFreeColors.WhiteSubtleBorder,
    scrim = DarkBreakFreeColors.PureBlack
)

private val BreakFreeLightColorScheme = lightColorScheme(
    primary = LightBreakFreeColors.GreenCalm,
    onPrimary = LightBreakFreeColors.OnGreenCalm,
    primaryContainer = LightBreakFreeColors.GreenCalmContainer,
    onPrimaryContainer = LightBreakFreeColors.GreenCalmDark,
    secondary = LightBreakFreeColors.AuroraTeal,
    onSecondary = LightBreakFreeColors.PureBlack,
    secondaryContainer = LightBreakFreeColors.BlackElevated,
    onSecondaryContainer = LightBreakFreeColors.WhiteGlowing,
    tertiary = LightBreakFreeColors.SoftGold,
    onTertiary = LightBreakFreeColors.PureBlack,
    tertiaryContainer = LightBreakFreeColors.BlackElevated,
    onTertiaryContainer = LightBreakFreeColors.WhiteGlowing,
    error = LightBreakFreeColors.DangerRed,
    onError = LightBreakFreeColors.OnDanger,
    errorContainer = LightBreakFreeColors.RoseWarningDark,
    onErrorContainer = LightBreakFreeColors.SoftCoral,
    background = LightBreakFreeColors.BlackSurface,
    onBackground = LightBreakFreeColors.WhiteGlowing,
    surface = LightBreakFreeColors.BlackCard,
    onSurface = LightBreakFreeColors.WhiteGlowing,
    surfaceVariant = LightBreakFreeColors.BlackElevated,
    onSurfaceVariant = LightBreakFreeColors.WhiteDim,
    outline = LightBreakFreeColors.BlackBorderStrong,
    outlineVariant = LightBreakFreeColors.WhiteSubtleBorder,
    scrim = LightBreakFreeColors.PureBlack
)

/**
 * Softer, larger radii than Material's defaults. Rounded surfaces read as
 * "calm" and match the pill-shaped controls used throughout the app.
 */
private val BreakFreeShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Keep the global colour holder in sync so the top-level getters (TextPrimary, ...)
    // resolve to the right palette during this composition.
    BreakFreeThemeState.colors.value = if (darkTheme) DarkBreakFreeColors else LightBreakFreeColors

    val colorScheme = if (darkTheme) BreakFreeDarkColorScheme else BreakFreeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = BreakFreeShapes,
        content = content
    )
}
