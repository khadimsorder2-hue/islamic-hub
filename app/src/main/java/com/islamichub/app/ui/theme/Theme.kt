package com.islamichub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Light theme — warm white background (Warm Ivory)
private val LightColors = lightColorScheme(
    primary = IslamicViolet,
    onPrimary = Color.White,
    primaryContainer = IslamicVioletSoft,
    onPrimaryContainer = IslamicVioletDark,
    secondary = MutedGold,
    onSecondary = Color.Black,
    secondaryContainer = MutedGoldSoft,
    onSecondaryContainer = Color(0xFF4A3F0A),
    tertiary = IslamicVioletDark,
    background = WarmIvoryBg,            // 0xFFFCFAF7 — warm white
    onBackground = TextPrimaryLight,
    surface = WhiteSurface,              // 0xFFFFFFFF
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    outlineVariant = DividerLight
)

// Warm light theme — warmer background (cream/warm white for reading comfort)
private val WarmLightColors = lightColorScheme(
    primary = IslamicViolet,
    onPrimary = Color.White,
    primaryContainer = IslamicVioletSoft,
    onPrimaryContainer = IslamicVioletDark,
    secondary = MutedGold,
    onSecondary = Color.Black,
    secondaryContainer = MutedGoldSoft,
    onSecondaryContainer = Color(0xFF4A3F0A),
    tertiary = IslamicVioletDark,
    background = Color(0xFFFDF6E3),       // warm cream — Solarized light
    onBackground = Color(0xFF3F3A35),
    surface = Color(0xFFFFFBF0),          // warm white surface
    onSurface = Color(0xFF3F3A35),
    surfaceVariant = Color(0xFFF5EFD9),
    onSurfaceVariant = Color(0xFF7A7264),
    outline = Color(0xFFE0D8C0),
    outlineVariant = Color(0xFFE0D8C0)
)

private val DarkColors = darkColorScheme(
    primary = IslamicVioletNight,
    onPrimary = Color.Black,
    primaryContainer = IslamicVioletDarkNight,
    onPrimaryContainer = Color.White,
    secondary = MutedGoldNight,
    onSecondary = Color.Black,
    secondaryContainer = MutedGoldSoftNight,
    onSecondaryContainer = MutedGoldNight,
    tertiary = IslamicVioletNight,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceAlt,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    outlineVariant = DividerDark
)

private val IslamicHubShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

enum class ThemeMode(val label: String, val labelBn: String) {
    AUTO("Auto (System)", "অটো (সিস্টেম)"),
    LIGHT("Light", "লাইট"),
    DARK("Dark", "ডার্ক"),
    WARM_LIGHT("Warm Light", "ওয়ার্ম লাইট")
}

@Composable
fun IslamicHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: String = "auto",
    content: @Composable () -> Unit
) {
    val colors = when (themeMode.lowercase()) {
        "light" -> LightColors
        "dark" -> DarkColors
        "warm_light" -> WarmLightColors
        else -> if (darkTheme) DarkColors else LightColors  // auto
    }
    MaterialTheme(
        colorScheme = colors,
        typography = IslamicHubTypography,
        shapes = IslamicHubShapes,
        content = content
    )
}
