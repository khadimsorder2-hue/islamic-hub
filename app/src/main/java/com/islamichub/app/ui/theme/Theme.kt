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
    background = WarmIvoryBg,
    onBackground = TextPrimaryLight,
    surface = WhiteSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    outlineVariant = DividerLight
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

@Composable
fun IslamicHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = IslamicHubTypography,
        shapes = IslamicHubShapes,
        content = content
    )
}
