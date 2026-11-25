package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BerryRose,
    onPrimary = Color.White,
    primaryContainer = SoftBlush,
    onPrimaryContainer = BerryRoseDark,
    secondary = CandleGold,
    onSecondary = EspressoInk,
    secondaryContainer = CandleGoldSoft,
    onSecondaryContainer = EspressoInk,
    tertiary = SageAccent,
    onTertiary = Color.White,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = EspressoInk,
    background = WarmCream,
    onBackground = EspressoInk,
    surface = ParchmentWhite,
    onSurface = EspressoInk,
    surfaceVariant = SoftBlush,
    onSurfaceVariant = CocoaMuted,
    outline = WarmOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBerryPrimary,
    onPrimary = DarkOnBerryPrimary,
    primaryContainer = BerryRoseDark,
    onPrimaryContainer = SoftBlush,
    secondary = DarkGoldSecondary,
    onSecondary = EspressoInk,
    secondaryContainer = Color(0xFF4A3313),
    onSecondaryContainer = CandleGoldSoft,
    tertiary = Color(0xFF9AC5B8),
    onTertiary = Color(0xFF132E27),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = MidnightSurface,
    onSurfaceVariant = Color(0xFFD7C2C6),
    outline = Color(0xFF543C42)
)

@Composable
fun BirthdayGiftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BirthdayTypography,
        content = content
    )
}
