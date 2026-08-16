package com.islamichub.app.data.repo

import com.islamichub.app.data.local.QuranAssetSource
import com.islamichub.app.data.local.QuranData
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.RevelationType
import com.islamichub.app.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only Quran repository. Loads from bundled JSON assets (all 114 surahs).
 * Falls back to in-memory QuranData only if asset is missing (should never happen).
 */
class QuranRepository(
    private val data: QuranData,
    private val assetSource: QuranAssetSource? = null
) {

    suspend fun listSurahs(): List<SurahSummary> = withContext(Dispatchers.IO) {
        if (assetSource != null) {
            try {
                return@withContext assetSource.loadMeta().map {
                    SurahSummary(
                        number = it.number,
                        nameArabic = it.nameArabic,
                        nameEnglish = it.nameEnglish,
                        nameBengali = it.nameBengali,
                        englishMeaning = it.englishMeaning,
                        revelationType = it.toRevelationType(),
                        ayahCount = it.ayahCount,
                        isFullTextAvailable = true
                    )
                }
            } catch (_: Exception) {
                // fall through to bundled sample
            }
        }
        // Bundled fallback (only used if asset is missing)
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
        if (assetSource != null) {
            try {
                assetSource.loadSurah(number)?.let { return@withContext it }
            } catch (_: Exception) {
                // fall through
            }
        }
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

    /**
     * Full-text search across all ayahs. Returns matching ayahs grouped by surah.
     */
    suspend fun searchAyahs(query: String, limit: Int = 50): List<AyahSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank() || assetSource == null) return@withContext emptyList()
        val q = query.trim().lowercase()
        val results = mutableListOf<AyahSearchResult>()
        try {
            val all = assetSource.loadAllSurahs()
            outer@ for (surah in all) {
                for (ayah in surah.ayahs) {
                    val matchEn = ayah.english.lowercase().contains(q)
                    val matchBn = ayah.bengali.contains(query)
                    val matchAr = ayah.arabic.contains(query)
                    if (matchEn || matchBn || matchAr) {
                        results.add(
                            AyahSearchResult(
                                surahNumber = surah.number,
                                surahName = surah.nameEnglish,
                                surahNameBn = surah.nameBengali,
                                ayahNumber = ayah.numberInSurah,
                                arabic = ayah.arabic,
                                english = ayah.english,
                                bengali = ayah.bengali
                            )
                        )
                        if (results.size >= limit) break@outer
                    }
                }
            }
        } catch (_: Exception) {
            return@withContext emptyList()
        }
        results
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
    val revelationType: RevelationType,
    val ayahCount: Int,
    val isFullTextAvailable: Boolean
)

data class AyahSearchResult(
    val surahNumber: Int,
    val surahName: String,
    val surahNameBn: String,
    val ayahNumber: Int,
    val arabic: String,
    val english: String,
    val bengali: String
)
