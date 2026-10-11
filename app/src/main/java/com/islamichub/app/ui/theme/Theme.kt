package com.islamichub.app.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import android.view.Window

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
    // v5.17.0 — UNIFIED PREMIUM CANVAS (PrimeTube reference): in dark mode
    // surface used to be #1B1822 while background was #121016, so every
    // Scaffold/TopAppBar screen showed a slightly lighter tone than the
    // no-Scaffold screens (Home/Prayer/Names) — the user saw this as "আলাদা
    // layout". surface now equals background, so the status-bar strip, the
    // top app bar and the page body are ONE continuous canvas on all 43
    // screens; cards still stand out via the unchanged surfaceContainer* ramp.
    surface = DarkBg,
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

    // v5.17.1 — SYSTEM INFO ALWAYS VISIBLE (root fix for "theme dile system
    // info dekha jay nah"). enableEdgeToEdge(SystemBarStyle.auto) follows the
    // SYSTEM dark mode, but the app theme can differ from the system — with
    // the app in Light and the system in Dark the clock/battery were drawn
    // WHITE on the app's white canvas and vanished completely. The status/nav
    // bar icon appearance now follows the APP's canvas luminance instead:
    // light canvas -> dark icons, dark canvas -> light icons, reactively.
    val view = LocalView.current
    val lightCanvas = colors.background.luminance() > 0.5f
    if (!view.isInEditMode) {
        SideEffect {
            activityWindowOf(view.context)?.let { window ->
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = lightCanvas
                controller.isAppearanceLightNavigationBars = lightCanvas
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = IslamicHubTypography,
        shapes = IslamicHubShapes,
        content = content
    )
}

/**
 * v5.17.1 — unwraps ContextWrapper chains (ContextThemeWrapper etc.) to find
 * the hosting Activity's window. Returns null defensively — never crash over
 * system-bar tinting.
 */
private fun activityWindowOf(context: android.content.Context): Window? {
    var ctx = context
    var guard = 0
    while (guard < 8) {
        when (ctx) {
            is Activity -> return ctx.window
            is ContextWrapper -> ctx = ctx.baseContext
            else -> return null
        }
        guard++
    }
    return null
}
