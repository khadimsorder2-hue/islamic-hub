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

    // ─── v5.14.1 — Home top time/date panel (always available, NO network) ───

    private val BN_MONTHS = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    private val BN_WEEKDAYS = arrayOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
    private val BN_HIJRI_MONTHS = arrayOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদাল আউয়াল", "জমাদাস সানি",
        "রজব", "শা'বান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ্জ"
    )

    /**
     * Current device time as "বিকাল ৫:৪২" — the clock pill on the Home hero.
     * Computed from the device clock, so it works fully offline.
     */
    fun currentClock12hBangla(): String {
        val cal = java.util.Calendar.getInstance()
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val m = cal.get(java.util.Calendar.MINUTE)
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return "${periodLabel(h)} ${toBanglaDigits(h12.toString())}:${toBanglaDigits(m.toString().padStart(2, '0'))}"
    }

    /**
     * Today's Gregorian date in Bangla: "শুক্রবার, ২৭ সেপ্টেম্বর ২০২৬".
     * Device-local, no network needed.
     */
    fun gregorianBanglaDate(): String {
        val cal = java.util.Calendar.getInstance()
        val weekday = cal.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sunday..7=Saturday
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month = cal.get(java.util.Calendar.MONTH) // 0-based
        val year = cal.get(java.util.Calendar.YEAR)
        return "${BN_WEEKDAYS[(weekday - 1).coerceIn(0, 6)]}, " +
            "${toBanglaDigits(day.toString())} ${BN_MONTHS[month.coerceIn(0, 11)]} ${toBanglaDigits(year.toString())}"
    }

    /**
     * Offline Hijri date in Bangla: "৫ রবিউল আউয়াল ১৪৪৮ হিজরি".
     *
     * The home hero used to show the Hijri date ONLY when the prayer-times
     * API responded — with no network the whole time/date panel vanished
     * ("—"). This arithmetic conversion (classic Kuwaiti/Julian algorithm,
     * integer math only) always works. Local moon-sighting may shift it ±1
     * day; the API value takes precedence when it is available.
     */
    fun hijriDateBanglaOffline(): String {
        return try {
            val cal = java.util.Calendar.getInstance()
            val h = gregorianToHijri(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            )
            "${toBanglaDigits(h.third.toString())} ${BN_HIJRI_MONTHS[(h.second - 1).coerceIn(0, 11)]} " +
                "${toBanglaDigits(h.first.toString())} হিজরি"
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Gregorian → Hijri (tabular/Kuwaiti algorithm, all-integer math).
     * Returns Triple(hijriYear, hijriMonth 1..12, hijriDay).
     */
    private fun gregorianToHijri(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        var jd = if (year > 1582 || (year == 1582 && (month > 10 || (month == 10 && day > 14)))) {
            val k = (year + 4800 + (month - 14) / 12)
            (1461 * k) / 4 +
                (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12 -
                (3 * ((k + 100) / 100)) / 4 + day - 32075
        } else {
            367 * year - (7 * (year + 5001 + (month - 9) / 7)) / 4 +
                (275 * month) / 9 + day + 1729777
        }
        // "-1" calibration: the raw tabular conversion runs ~2 days ahead of
        // the widely used local/Umm-al-Qura values; -1 day keeps it within
        // the documented ±1-day accuracy of any arithmetic Hijri calendar.
        jd = jd - 1948440 + 10632 - 1
        val n = (jd - 1) / 10631
        jd = jd - 10631 * n + 354
        var j = ((10985 - jd) / 5316) * ((50 * jd) / 17719) + (jd / 5670) * ((43 * jd) / 15238)
        jd = jd - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val m = (24 * jd) / 709
        val d = jd - (709 * m) / 24
        val y = 30 * n + j - 30
        return Triple(y, m, d)
    }
}
