package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.RevelationType
import com.islamichub.app.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Loads Quran data from bundled JSON assets (quran.json + meta.json).
 *
 * Asset format (see scripts/download_quran.py):
 *   {
 *     "surahs": [
 *       {
 *         "number": 1,
 *         "nameArabic": "...",
 *         "nameEnglish": "...",
 *         "nameBengali": "...",
 *         "englishMeaning": "...",
 *         "revelationType": "Meccan" | "Medinan",
 *         "ayahCount": 7,
 *         "ayahs": [
 *           {"numberInSurah": 1, "arabic": "...", "english": "...", "bengali": "..."}
 *         ]
 *       }
 *     ]
 *   }
 */
class QuranAssetSource(private val context: Context) {

    private val gson = Gson()

    // Cached parsed payload (loaded once, on demand).
    @Volatile private var fullQuranCache: QuranPayload? = null
    @Volatile private var metaCache: SurahMetaPayload? = null

    suspend fun loadMeta(): List<SurahMeta> = withContext(Dispatchers.IO) {
        metaCache?.let { return@withContext it.surahs }
        val text = readAsset("quran/meta.json")
        val parsed = try {
            gson.fromJson(text, SurahMetaPayload::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } ?: throw IOException("Failed to parse quran/meta.json")
        metaCache = parsed
        parsed.surahs
    }

    suspend fun loadSurah(number: Int): Surah? = withContext(Dispatchers.IO) {
        val cache = fullQuranCache ?: run {
            val text = readAsset("quran/quran.json")
            val parsed = try {
                gson.fromJson(text, QuranPayload::class.java)
            } catch (e: JsonSyntaxException) {
                null
            } ?: throw IOException("Failed to parse quran/quran.json")
            fullQuranCache = parsed
            parsed
        }
        cache.surahs.firstOrNull { it.number == number }?.toDomain()
    }

    suspend fun loadAllSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        val cache = fullQuranCache ?: run {
            val text = readAsset("quran/quran.json")
            val parsed = try {
                gson.fromJson(text, QuranPayload::class.java)
            } catch (e: JsonSyntaxException) {
                null
            } ?: throw IOException("Failed to parse quran/quran.json")
            fullQuranCache = parsed
            parsed
        }
        cache.surahs.map { it.toDomain() }
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

// ─── JSON DTOs ──────────────────────────────────────────────────────────────

data class SurahMetaPayload(
    @SerializedName("surahs") val surahs: List<SurahMeta>
)

data class SurahMeta(
    @SerializedName("number") val number: Int,
    @SerializedName("nameArabic") val nameArabic: String,
    @SerializedName("nameEnglish") val nameEnglish: String,
    @SerializedName("nameBengali") val nameBengali: String,
    @SerializedName("englishMeaning") val englishMeaning: String,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("ayahCount") val ayahCount: Int
) {
    fun toRevelationType(): RevelationType =
        if (revelationType.equals("Medinan", ignoreCase = true)) RevelationType.MEDINAN
        else RevelationType.MECCAN
}

data class QuranPayload(
    @SerializedName("surahs") val surahs: List<SurahJson>
)

data class SurahJson(
    @SerializedName("number") val number: Int,
    @SerializedName("nameArabic") val nameArabic: String,
    @SerializedName("nameEnglish") val nameEnglish: String,
    @SerializedName("nameBengali") val nameBengali: String,
    @SerializedName("englishMeaning") val englishMeaning: String,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("ayahCount") val ayahCount: Int,
    @SerializedName("ayahs") val ayahs: List<AyahJson>
) {
    fun toDomain(): Surah = Surah(
        number = number,
        nameArabic = nameArabic,
        nameEnglish = nameEnglish,
        nameBengali = nameBengali,
        englishMeaning = englishMeaning,
        revelationType = if (revelationType.equals("Medinan", ignoreCase = true))
            RevelationType.MEDINAN else RevelationType.MECCAN,
        ayahCount = ayahCount,
        ayahs = ayahs.map {
            Ayah(
                numberInSurah = it.numberInSurah,
                arabic = it.arabic,
                english = it.english,
                bengali = it.bengali
            )
        }
    )
}

data class AyahJson(
    @SerializedName("numberInSurah") val numberInSurah: Int,
    @SerializedName("arabic") val arabic: String,
    @SerializedName("english") val english: String,
    @SerializedName("bengali") val bengali: String
)
