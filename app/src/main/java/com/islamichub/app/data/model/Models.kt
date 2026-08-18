package com.islamichub.app.data.model

/** A surah (chapter) of the Quran. */
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameBengali: String,
    val englishMeaning: String,
    val revelationType: RevelationType,
    val ayahCount: Int,
    val ayahs: List<Ayah>
)

enum class RevelationType(val label: String) {
    MECCAN("Meccan"),
    MEDINAN("Medinan")
}

/** A single ayah (verse). */
data class Ayah(
    val numberInSurah: Int,
    val arabic: String,
    val english: String,
    val bengali: String,
    val reference: String? = null
)

/** One of the 99 names of Allah (Asma ul Husna). */
data class NameOfAllah(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val englishMeaning: String,
    val bengaliMeaning: String
)

/** A daily dua / adhkar entry. */
data class Dua(
    val id: String,
    val titleEnglish: String,
    val titleBengali: String,
    val arabic: String,
    val transliteration: String,
    val translationEnglish: String,
    val translationBengali: String,
    val reference: String
)

/** Prayer times for a single day. */
data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val date: String,
    val hijriDate: String,
    val locationName: String
) {
    fun asList(): List<Pair<String, String>> = listOf(
        "Fajr" to fajr,
        "Sunrise" to sunrise,
        "Dhuhr" to dhuhr,
        "Asr" to asr,
        "Maghrib" to maghrib,
        "Isha" to isha
    )
}

/** Dhikr option for tasbih. */
data class DhikrOption(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val defaultTarget: Int,
    val banglaPronunciation: String = "",
    val banglaTranslation: String = "",
    val banglaMeaning: String = "",
    val whyRecite: String = "",
    val reference: String = "",
    val reward: String = ""
)

/** Hijri calendar day info. */
data class HijriDay(
    val day: Int,
    val monthNumber: Int,
    val monthName: String,
    val year: Int,
    val weekday: String,
    val isToday: Boolean = false
)
