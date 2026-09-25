package com.islamichub.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * IslamicHub Premium Animation Toolkit (v5.3.1)
 *
 * Shared reusable animation components so premium motion is consistent
 * across all 36+ screens without each screen hand-rolling its own logic:
 *
 *  1. [Modifier.staggerEntrance]  — list/grid item fade + slide-in stagger
 *  2. [Modifier.premiumTap]       — tap scale-bounce + optional haptic feedback
 *  3. [PremiumCountUpText]        — animated number count-up (stats, zakat, streak)
 *  4. [Modifier.premiumShimmer]   — skeleton shimmer loading placeholder
 *  5. [Modifier.premiumPulseHighlight] — pulsing tint background (playing ayah)
 *  6. [PremiumProgressBar]        — animated fill progress bar
 *  7. [Modifier.premiumGlow]      — soft glow behind avatars while active
 *
 * All components respect the design tokens in DesignSystem.kt.
 */

// ─── 1. Entrance stagger (lists & grids) ─────────────────────────────────────

/**
 * Staggered entrance: item fades in and slides up, delayed by its index.
 *
 * Usage inside LazyColumn:
 * ```
 * itemsIndexed(list) { index, item ->
 *     Card(modifier = Modifier.staggerEntrance(index)) { ... }
 * }
 * ```
 *
 * Items beyond [maxAnimatedItems] appear instantly so very long lists
 * (24,000 hadiths) don't feel sluggish when scrolled fast.
 */
fun Modifier.staggerEntrance(
    index: Int,
    delayPerItemMs: Int = 40,
    maxAnimatedItems: Int = 20,
    slideDp: Float = 26f,
    durationMs: Int = 420,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    val fraction = remember { Animatable(if (index < maxAnimatedItems) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (index < maxAnimatedItems) {
            delay(index.toLong() * delayPerItemMs)
            fraction.animateTo(1f, tween(durationMs, easing = EaseOutCubic))
        }
    }
    val p = fraction.value
    this.graphicsLayer {
        alpha = p
        translationY = (1f - p) * slideDp * density
    }
}

// ─── 2. Premium tap (scale bounce + haptic) ──────────────────────────────────

/**
 * Tap feedback: card/row scales down slightly while pressed (springy bounce
 * back on release) with optional light haptic tick. Replaces bare
 * `Modifier.clickable` where a premium feel is wanted (Tasbih counter,
 * checkboxes, buttons, list rows).
 */
fun Modifier.premiumTap(
    enabled: Boolean = true,
    haptic: Boolean = true,
    pressedScale: Float = 0.96f,
    hapticType: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "premiumTapScale"
    )
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(pressed) {
        if (pressed && haptic) haptics.performHapticFeedback(hapticType)
    }
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = rememberRipple(),
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * v5.11.0 — one-shot haptic performer for events that are NOT taps:
 * tasbih round-complete, quiz answer reveal, milestone reached, etc.
 *
 * ```
 * val haptic = rememberPremiumHaptic()
 * LaunchedEffect(state.justCompletedRound) { if (state.justCompletedRound) haptic(HapticFeedbackType.LongPress) }
 * ```
 */
@Composable
fun rememberPremiumHaptic(): (HapticFeedbackType) -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(haptics) { { type -> haptics.performHapticFeedback(type) } }
}

// ─── 3. Count-up number text ─────────────────────────────────────────────────

/**
 * Number that counts up from 0 (or previous value) to [targetValue] with an
 * ease-out curve. Use for Zakat totals, profile stats, khatam counts,
 * streak numbers — anywhere a static number deserves emphasis.
 */
@Composable
fun PremiumCountUpText(
    targetValue: Float,
    modifier: Modifier = Modifier,
    durationMs: Int = 900,
    format: (Float) -> String = { it.toInt().toString() },
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(targetValue) {
        animated.animateTo(targetValue, tween(durationMs, easing = EaseOutCubic))
    }
    Text(
        text = format(animated.value),
        style = style,
        color = color,
        modifier = modifier
    )
}

// ─── 4. Shimmer skeleton ─────────────────────────────────────────────────────

/**
 * Skeleton loading shimmer — a moving light band across a rounded block.
 * Apply to placeholder boxes/surah cards while data loads.
 *
 * ```
 * Box(Modifier.fillMaxWidth().height(64.dp).premiumShimmer())
 * ```
 */
fun Modifier.premiumShimmer(
    shape: Shape = RoundedCornerShape(12.dp),
    baseAlpha: Float = 0.6f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "premiumShimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "premiumShimmerProgress"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val baseColor = base.copy(alpha = baseAlpha)
    this
        .clip(shape)
        .drawBehind {
            drawRect(baseColor)
            val bandWidth = size.width.coerceAtLeast(1f) * 0.65f
            val startX = progress * (size.width + bandWidth) - bandWidth
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        highlight.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + bandWidth, size.height)
                )
            )
        }
}

// ─── 5. Pulse highlight (playing ayah, live states) ──────────────────────────

/**
 * Pulsing rounded-rect tint behind content — used to highlight the ayah
 * currently being recited, active tabs, or milestone moments. The tint
 * breathes between [minAlpha] and [maxAlpha] so the eye is drawn gently,
 * never harshly.
 */
fun Modifier.premiumPulseHighlight(
    active: Boolean,
    color: Color,
    cornerRadius: Dp = 12.dp,
    minAlpha: Float = 0.10f,
    maxAlpha: Float = 0.30f,
    periodMs: Int = 950
): Modifier = composed {
    if (!active) return@composed this
    val transition = rememberInfiniteTransition(label = "premiumPulse")
    val glow by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "premiumPulseGlow"
    )
    this
        .clip(RoundedCornerShape(cornerRadius))
        .background(color.copy(alpha = glow))
}

// ─── 6. Animated progress bar ────────────────────────────────────────────────

/**
 * Rounded progress bar whose fill animates smoothly to [progress] (0f..1f)
 * with a spring — used for khatam progress, qada outstanding, quiz scores.
 */
@Composable
fun PremiumProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    fillColor: Color = MaterialTheme.colorScheme.primary,
    barHeight: Dp = 10.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "premiumProgressFill"
    )
    BoxWithConstraints(modifier) {
        val widthPx = LocalDensity.current.run { (maxWidth * animated).toPx() }
        val fill = fillColor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .background(trackColor)
                .drawBehind {
                    drawRect(
                        color = fill,
                        size = Size(widthPx.coerceAtLeast(0f), size.height)
                    )
                }
        )
    }
}

// ─── 7. Soft glow (active reciter avatar, floating elements) ────────────────

/**
 * Soft radial glow behind an element while [active] — reciter avatar glows
 * while their recitation plays, achievement badges glow when unlocked.
 */
fun Modifier.premiumGlow(
    active: Boolean,
    color: Color,
    radius: Dp = 20.dp,
    minAlpha: Float = 0.18f,
    maxAlpha: Float = 0.45f
): Modifier = composed {
    if (!active) return@composed this
    val transition = rememberInfiniteTransition(label = "premiumGlow")
    val alpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "premiumGlowAlpha"
    )
    val glowColor = color
    this.drawBehind {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.maxDimension / 2f + radius.toPx()
            ),
            radius = size.maxDimension / 2f + radius.toPx()
        )
    }
}
