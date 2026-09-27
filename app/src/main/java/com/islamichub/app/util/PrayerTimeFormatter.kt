package com.islamichub.app.util

/**
 * v5.14.0 — Prayer-time display formatting.
 *
 * The user asked to drop the 16/18-style 24-hour display ("16:30", "18:45")
 * and show times in the familiar Bangla 12-hour style everywhere:
 *   "04:45" → "ভোর ৪:৪৫", "12:30" → "দুপুর ১২:৩০", "18:10" → "সন্ধ্যা ৬:১০".
 *
 * Only the DISPLAY layer converts — data storage ("HH:mm") and alarm
 * scheduling still use the raw 24-hour values, so nothing breaks.
 */
object PrayerTimeFormatter {

    private val BN_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBanglaDigits(value: String): String = buildString {
        for (ch in value) {
            if (ch.isDigit() && ch - '0' in 0..9) append(BN_DIGITS[ch - '0']) else append(ch)
        }
    }

    /** Bangla day-part label for a 24-hour value, the way prayer times are
     *  conventionally spoken in Bangladesh (ভোর / সকাল / দুপুর / বিকাল / সন্ধ্যা / রাত). */
    fun periodLabel(hour24: Int): String = when (hour24) {
        in 4..5 -> "ভোর"
        in 6..11 -> "সকাল"
        in 12..15 -> "দুপুর"
        in 16..17 -> "বিকাল"
        in 18..19 -> "সন্ধ্যা"
        else -> "রাত"
    }

    /**
     * "HH:mm" → "ভোর ৪:৪৫" style 12-hour Bangla display.
     * Any unparseable input is returned unchanged (defensive).
     */
    fun to12HourBangla(hhmm: String): String {
        val match = Regex("(\\d{1,2}):(\\d{2})").find(hhmm.trim()) ?: return hhmm
        val h = match.groupValues[1].toIntOrNull() ?: return hhmm
        val minute = match.groupValues[2]
        if (h !in 0..23) return hhmm
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return "${periodLabel(h)} ${toBanglaDigits(h12.toString())}:${toBanglaDigits(minute)}"
    }
}
