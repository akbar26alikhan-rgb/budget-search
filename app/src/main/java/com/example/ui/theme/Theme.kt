package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = Color.White,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = GreenContainer,
    secondary = WarmOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = OnOrangeContainer,
    onSecondaryContainer = OrangeContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = TextTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = WarmOrange,
    onSecondary = Color.White,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OnOrangeContainer,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = PureSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantCream,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun BudgetBitesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
