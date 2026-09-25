package com.islamichub.app.ui.screens.notepad

import androidx.compose.ui.graphics.Color

/**
 * v5.12.0 — Shared catalog for the Notepad feature (list + editor screens).
 *
 * 8 categories (user asked for Quran / Hadith / Thematic + as many as make
 * sense) and 8 ColorNote-style pastel colors. Every color ships in a light
 * and a dark variant so note cards stay readable in both themes.
 */
data class NoteCategory(
    val id: String,
    val label: String,
    val emoji: String,
    val color: Color
)

object NoteCategories {
    val ALL: List<NoteCategory> = listOf(
        NoteCategory("quran", "কুরআন", "📖", Color(0xFF2E7D32)),
        NoteCategory("hadith", "হাদিস", "🕌", Color(0xFF00695C)),
        NoteCategory("thematic", "বিষয়ভিত্তিক", "🧭", Color(0xFF6A1B9A)),
        NoteCategory("dua", "দুআ", "🤲", Color(0xFF3949AB)),
        NoteCategory("todo", "করণীয়", "✅", Color(0xFFAD1457)),
        NoteCategory("question", "প্রশ্ন", "❓", Color(0xFFEF6C00)),
        NoteCategory("personal", "ব্যক্তিগত", "📝", Color(0xFF1565C0)),
        NoteCategory("inspiration", "অনুপ্রেরণা", "✨", Color(0xFFB08500))
    )

    fun byId(id: String): NoteCategory = ALL.find { it.id == id } ?: ALL.first { it.id == "personal" }
}

object NoteColors {
    /** ColorNote-like pastels — light theme. */
    val lightPalette: List<Color> = listOf(
        Color(0xFFFFF59D), // yellow
        Color(0xFFC8E6C9), // green
        Color(0xFFBBDEFB), // blue
        Color(0xFFF8BBD0), // pink
        Color(0xFFE1BEE7), // purple
        Color(0xFFFFE0B2), // orange
        Color(0xFFB2DFDB), // teal
        Color(0xFFFFCCBC)  // salmon
    )

    /** Deep muted variants — dark theme (keeps the colorful look, readable text). */
    val darkPalette: List<Color> = listOf(
        Color(0xFF565021),
        Color(0xFF2C4A31),
        Color(0xFF2A4258),
        Color(0xFF563546),
        Color(0xFF483958),
        Color(0xFF584730),
        Color(0xFF244E48),
        Color(0xFF573A31)
    )

    /** Text colors on the pastel cards. */
    val lightOnCard: Color = Color(0xFF263238)
    val darkOnCard: Color = Color(0xFFECEFF1)

    fun palette(isDark: Boolean): List<Color> = if (isDark) darkPalette else lightPalette

    fun onCard(isDark: Boolean): Color = if (isDark) darkOnCard else lightOnCard

    fun colorAt(index: Int, isDark: Boolean): Color =
        palette(isDark)[index.coerceIn(0, palette().size - 1)]
}
