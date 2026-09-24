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

    /**
     * v5.7.0 — per-surah fast path: each surah lives in its own small JSON file
     * (quran/surah/surah_001.json …), so opening a surah parses ~40 KB instead
     * of the whole 4.7 MB Quran — fixes the endless "loading" spinner on
     * first open. Falls back to the bundled full-Quran file if missing.
     */
    suspend fun loadSurah(number: Int): Surah? = withContext(Dispatchers.IO) {
        if (number in 1..114) {
            try {
                val text = readAsset("quran/surah/surah_%03d.json".format(number))
                val parsed = try {
                    gson.fromJson(text, SurahJson::class.java)
                } catch (e: JsonSyntaxException) {
                    null
                }
                if (parsed != null) return@withContext parsed.toDomain()
            } catch (_: Exception) {
                // fall through to full-Quran parse
            }
        }
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
        // v5.7.0 — stream per-surah files when available (lower memory peak).
        val cached: List<Surah>? = if (perSurahCache.size == 114) {
            perSurahCache.values.sortedBy { it.number }
        } else null
        if (cached != null) return@withContext cached
        val individual: List<Surah>? = runCatching {
            (1..114).map { n: Int ->
                perSurahCache.getOrPut(n) {
                    gson.fromJson<SurahJson>(
                        readAsset("quran/surah/surah_%03d.json".format(n)),
                        SurahJson::class.java
                    ).toDomain()
                }
            }
        }.getOrNull()
        if (individual != null) return@withContext individual
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

    /**
     * Bangla transliteration (uccaron) for every ayah — loaded once from
     * quran/quran_bn_transliteration.json (surah → ayah → text).
     * Returns an empty map if the asset is missing (feature silently off).
     */
    suspend fun loadBanglaUccaron(): Map<Int, Map<Int, String>> = withContext(Dispatchers.IO) {
        bnUccaronCache?.let { return@withContext it }
        val parsed: Map<String, Map<String, String>>? = try {
            gson.fromJson(readAsset("quran/quran_bn_transliteration.json"), uccaronType)
        } catch (_: Exception) {
            null
        }
        val mapped = parsed?.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
            ?.mapValues { (_, v) ->
                v.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
            } ?: emptyMap()
        bnUccaronCache = mapped
        mapped
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private val uccaronType by lazy {
        object : com.google.gson.reflect.TypeToken<Map<String, Map<String, String>>>() {}.type
    }

    @Volatile private var bnUccaronCache: Map<Int, Map<Int, String>>? = null

    /** v5.7.0 — parsed per-surah domain cache (surah number → Surah). */
    private val perSurahCache = java.util.concurrent.ConcurrentHashMap<Int, Surah>()
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
