package com.islamichub.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit

/**
 * v5.5 — Global per-script reading text sizes.
 *
 * MainActivity collects the three user settings (0.7×–1.8×) from
 * SettingsRepository and provides them here, so EVERY screen can scale its
 * Arabic / Bangla / English text with a single helper call:
 *
 *     fontSize = arabicSp(28.sp)                       // literal base size
 *     fontSize = arabicSp(MaterialTheme.typography.displaySmall.fontSize)
 *     fontSize = banglaSp(base.fontSize * fontScale)   // chained with reader scale
 *
 * Defaults are 1f, so any screen not touched still renders at 100%.
 */
val LocalArabicFontScale = staticCompositionLocalOf { 1f }
val LocalBanglaFontScale = staticCompositionLocalOf { 1f }
val LocalEnglishFontScale = staticCompositionLocalOf { 1f }

/** Scale a base Arabic text size by the user's Arabic reading-size setting. */
@Composable
fun arabicSp(base: TextUnit): TextUnit = base * LocalArabicFontScale.current

/** Scale a base Bangla text size by the user's Bangla reading-size setting. */
@Composable
fun banglaSp(base: TextUnit): TextUnit = base * LocalBanglaFontScale.current

/** Scale a base English text size by the user's English reading-size setting. */
@Composable
fun englishSp(base: TextUnit): TextUnit = base * LocalEnglishFontScale.current
