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

// Light theme — v5.7.0 "invisible box" design: the canvas and the card surface
// share the SAME white color, so cards have no visible box at all — grouping is
// done purely by spacing and typography (user request: card boxes fully invisible).
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
    background = WhiteSurface,           // same as surface → card boxes invisible
    onBackground = TextPrimaryLight,
    surface = WhiteSurface,              // 0xFFFFFFFF
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceAlt,         // 0xFFF7F4F8 (chips / text-field fills stay)
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFD9D3E2),         // only for text-field borders etc.
    outlineVariant = Color(0xFFE4E0EB),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),      // M3 Card default → blends in
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF7F5FA),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE8E4EE)
)

// Warm light theme — v5.7.0: same "invisible box" treatment — warm-white
// canvas identical to the card surface.
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
    background = Color(0xFFFFFBF0),       // same as surface → card boxes invisible
    onBackground = Color(0xFF3F3A35),
    surface = Color(0xFFFFFBF0),          // warm white surface
    onSurface = Color(0xFF3F3A35),
    surfaceVariant = Color(0xFFF5EFD9),
    onSurfaceVariant = Color(0xFF7A7264),
    outline = Color(0xFFD3C8A6),          // only for text-field borders etc.
    outlineVariant = Color(0xFFE0D8C0),
    surfaceContainerLowest = Color(0xFFFFFBF0),
    surfaceContainerLow = Color(0xFFFFFBF0),      // M3 Card default → blends in
    surfaceContainer = Color(0xFFFFFBF0),
    surfaceContainerHigh = Color(0xFFFFFBF0),
    surfaceContainerHighest = Color(0xFFF8F1DE),
    surfaceBright = Color(0xFFFFFBF0),
    surfaceDim = Color(0xFFEAE1C8)
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
    outlineVariant = DividerDark,
    // v5.6.0: define explicitly so cards never fall back to baseline scheme.
    // Values mirror the previous effective look (slightly lighter than bg).
    surfaceContainerLowest = Color(0xFF161320),
    surfaceContainerLow = Color(0xFF1F1B29),
    surfaceContainer = Color(0xFF242030),
    surfaceContainerHigh = Color(0xFF2A2538),
    surfaceContainerHighest = Color(0xFF302A40),
    surfaceBright = Color(0xFF2E2940),
    surfaceDim = Color(0xFF171420)
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
