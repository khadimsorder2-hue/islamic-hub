package com.islamichub.app.ui.screens.quran

import androidx.compose.ui.graphics.Color

/**
 * v5.16.0 — Quran reader themes, adopted from the decoded Al Quran Bangla
 * (Andi) reference app: its reader exposes day/night page looks (classic
 * paper, sepia, dark mushaf) plus its signature teal-dark home palette
 * (bgHomeGradient*). We extend that idea into six full reader themes —
 * the DEFAULT one keeps the app's Material colors (identical to v5.15.1),
 * the other five repaint the whole reader page: background, ayah cards,
 * Arabic text, Bangla/English lines and the uccharon teal.
 */
enum class QuranReaderTheme(
    val id: String,
    val labelBn: String,
    val bg: Color,
    val cardBg: Color,
    val playingBg: Color,
    val arabicColor: Color,
    val banglaColor: Color,
    val englishColor: Color,
    val uccharonColor: Color,
    val isDark: Boolean
) {
    DEFAULT(
        "default", "অ্যাপ থিম",
        bg = Color.Unspecified, cardBg = Color.Unspecified,
        playingBg = Color.Unspecified, arabicColor = Color.Unspecified,
        banglaColor = Color.Unspecified, englishColor = Color.Unspecified,
        uccharonColor = Color.Unspecified, isDark = false
    ),
    // Andi's list pages: warm cream paper (#FAF8EC bgListAlternate) + dark ink
    CREAM_PAPER(
        "cream", "ক্লাসিক ক্রিম",
        bg = Color(0xFFFAF6EA), cardBg = Color(0xFFFFFCF2),
        playingBg = Color(0xFFF3E9C8), arabicColor = Color(0xFF23201A),
        banglaColor = Color(0xFF3D3A32), englishColor = Color(0xFF55503F),
        uccharonColor = Color(0xFF1F8E84), isDark = false
    ),
    // Pure white daylight reading
    WHITE(
        "white", "সাদা",
        bg = Color(0xFFF6F7F5), cardBg = Color(0xFFFFFFFF),
        playingBg = Color(0xFFE3EFE8), arabicColor = Color(0xFF111827),
        banglaColor = Color(0xFF1F2937), englishColor = Color(0xFF374151),
        uccharonColor = Color(0xFF1F8E84), isDark = false
    ),
    // Old mushaf sepia page
    SEPIA(
        "sepia", "সেপিয়া",
        bg = Color(0xFFF0E2C4), cardBg = Color(0xFFF7ECD6),
        playingBg = Color(0xFFE8D3A4), arabicColor = Color(0xFF3E2E14),
        banglaColor = Color(0xFF4A3A1E), englishColor = Color(0xFF5C4A28),
        uccharonColor = Color(0xFF14655C), isDark = false
    ),
    // Andi premium header teal-dark (bgHomeGradient #273c50 → #112838 family)
    DEEP_BLUE(
        "deepblue", "নীলাভ রাত",
        bg = Color(0xFF152B3D), cardBg = Color(0xFF1C3648),
        playingBg = Color(0xFF274760), arabicColor = Color(0xFFEFE3B8),
        banglaColor = Color(0xFFD8E2EA), englishColor = Color(0xFFB9C8D4),
        uccharonColor = Color(0xFF7BD8CD), isDark = true
    ),
    // Classic night mushaf: near-black page + gold Arabic
    NIGHT_GOLD(
        "nightgold", "মুসহাফ রাত (গোল্ড)",
        bg = Color(0xFF0D1210), cardBg = Color(0xFF141B18),
        playingBg = Color(0xFF1E2A24), arabicColor = Color(0xFFE7C86A),
        banglaColor = Color(0xFFCBD5CE), englishColor = Color(0xFFA9B5AD),
        uccharonColor = Color(0xFF66C7BA), isDark = true
    );

    companion object {
        fun fromId(id: String?): QuranReaderTheme =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
