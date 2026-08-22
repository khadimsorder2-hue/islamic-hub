package com.islamichub.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * IslamicHub Design System
 *
 * Centralized design tokens for consistent UI across all screens.
 * Per GLM 5.2 MAX-EFFORT skill §6: "Avoid scattered magic values."
 *
 * All screens should reference these tokens instead of hardcoding values.
 */

// ─── Color Tokens (Semantic) ───────────────────────────────────────────────

object AppColors {
    // Brand
    val brandPrimary = Color(0xFF6D45C7)       // Islamic Violet
    val brandPrimaryDark = Color(0xFF4F3295)
    val brandSecondary = Color(0xFFC9A34E)     // Muted Gold

    // Surfaces
    val surfaceLight = Color(0xFFFFFFFF)
    val surfaceVariantLight = Color(0xFFF7F4F8)
    val surfaceDark = Color(0xFF1B1822)
    val surfaceVariantDark = Color(0xFF262030)

    // Content
    val contentPrimary = Color(0xFF24212B)
    val contentSecondary = Color(0xFF77727D)
    val contentPrimaryDark = Color(0xFFEDEAF1)
    val contentSecondaryDark = Color(0xFFA39DAE)

    // Functional
    val success = Color(0xFF2E7D32)
    val warning = Color(0xFFEF6C00)
    val error = Color(0xFFC62828)
    val info = Color(0xFF1565C0)

    // Dividers
    val dividerLight = Color(0xFFECE8EF)
    val dividerDark = Color(0xFF2A2632)

    // Prayer accent colors
    val fajrAccent = Color(0xFF7E8CE0)
    val sunriseAccent = Color(0xFFE8A86A)
    val dhuhrAccent = Color(0xFFE0B53D)
    val asrAccent = Color(0xFFC97862)
    val maghribAccent = Color(0xFFB36283)
    val ishaAccent = Color(0xFF6B6E91)

    // Topic accent palette (for thematic study cards)
    val topicAccents = listOf(
        Color(0xFF6D45C7), Color(0xFF1B5E20), Color(0xFFC9A34E),
        Color(0xFF1565C0), Color(0xFFD84315), Color(0xFF00ACC1),
        Color(0xFF8D6E63), Color(0xFFEF6C00), Color(0xFF00897B),
        Color(0xFF3949AB), Color(0xFFFF6B35), Color(0xFF7E57C2)
    )

    fun topicColor(index: Int): Color = topicAccents[index % topicAccents.size]
}

// ─── Spacing Scale ──────────────────────────────────────────────────────────

object AppSpacing {
    val xs = 4.dp       // tight internal padding
    val sm = 8.dp       // chip gaps, small margins
    val md = 12.dp      // card internal padding
    val lg = 16.dp      // standard padding
    val xl = 20.dp      // card padding
    val xxl = 24.dp     // premium card padding
    val xxxl = 32.dp    // section spacing

    // Screen-level
    val screenPadding = 16.dp
    val cardPadding = 16.dp
    val cardPaddingPremium = 20.dp
    val sectionGap = 16.dp
}

// ─── Corner Radius Scale ───────────────────────────────────────────────────

object AppRadius {
    val xs = 8.dp       // badges, chips
    val sm = 12.dp      // small cards
    val md = 16.dp      // standard cards
    val lg = 20.dp      // premium cards
    val xl = 24.dp      // hero cards
    val xxl = 28.dp     // large hero
    val pill = 50.dp    // pill-shaped elements
}

// ─── Elevation Scale ───────────────────────────────────────────────────────

object AppElevation {
    val none = 0.dp
    val low = 1.dp      // flat cards
    val medium = 2.dp   // standard cards
    val high = 4.dp     // premium cards, dialogs
    val higher = 6.dp   // hero cards
    val max = 8.dp      // bottom nav, floating player
}

// ─── Touch Target Sizes ────────────────────────────────────────────────────

object AppTouchTargets {
    val minimum = 48.dp  // Material Design minimum
    val comfortable = 56.dp
    val iconButton = 36.dp
}

// ─── Animation Durations ──────────────────────────────────────────────────

object AppDurations {
    const val instant = 50      // tap feedback
    const val fast = 150        // scale, color change
    const val medium = 300      // expand/collapse
    const val slow = 500        // screen transitions
}

// ─── Icon Sizes ────────────────────────────────────────────────────────────

object AppIconSizes {
    val small = 16.dp
    val medium = 20.dp
    val large = 24.dp
    val xlarge = 28.dp
    val badge = 44.dp    // circular badge
}

// ─── Font Sizes ────────────────────────────────────────────────────────────

object AppFontSizes {
    val caption = 11.sp
    val label = 12.sp
    val body = 14.sp
    val title = 16.sp
    val headline = 20.sp
    val display = 28.sp
    val displayLarge = 36.sp
}
