package com.islamichub.app.data.repo

import com.islamichub.app.data.local.QuranData
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Read-only Quran repository backed by bundled data. */
class QuranRepository(private val data: QuranData) {

    suspend fun listSurahs(): List<SurahSummary> = withContext(Dispatchers.IO) {
        data.allSurahMetadata.map {
            SurahSummary(
                number = it.number,
                nameArabic = it.nameArabic,
                nameEnglish = it.nameEnglish,
                nameBengali = it.nameBengali,
                englishMeaning = it.englishMeaning,
                revelationType = it.revelationType,
                ayahCount = it.ayahCount,
                isFullTextAvailable = data.fullSurahs.any { s -> s.number == it.number }
            )
        }
    }

    suspend fun getSurah(number: Int): Surah? = withContext(Dispatchers.IO) {
        data.fullSurahs.firstOrNull { it.number == number }
    }

    suspend fun searchSurahs(query: String): List<SurahSummary> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext listSurahs()
        val q = query.trim().lowercase()
        listSurahs().filter {
            it.nameEnglish.lowercase().contains(q) ||
                it.nameBengali.contains(query) ||
                it.englishMeaning.lowercase().contains(q) ||
                it.number.toString() == q
        }
    }

    suspend fun ayahOfDay(): Ayah {
        val pool = data.ayahOfDayPool
        val idx = (System.currentTimeMillis() / 86_400_000L).toInt().mod(pool.size)
        val (ar, en, bn) = pool[idx]
        return Ayah(numberInSurah = 0, arabic = ar, english = en, bengali = bn)
    }

    suspend fun hadithOfDay(): Pair<String, String> {
        val pool = data.hadithOfDayPool
        val idx = (System.currentTimeMillis() / 86_400_000L).toInt().mod(pool.size)
        return pool[idx]
    }
}

data class SurahSummary(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameBengali: String,
    val englishMeaning: String,
    val revelationType: com.islamichub.app.data.model.RevelationType,
    val ayahCount: Int,
    val isFullTextAvailable: Boolean
)
