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
        // v5.15.1 — serve repeat opens from the parsed per-surah cache. This
        // was THE "Quran reloads from scratch every time you enter" bug:
        // perSurahCache existed but loadSurah never used it, so every reader
        // open re-read + re-parsed the JSON asset from disk.
        perSurahCache[number]?.let { return@withContext it }
        if (number in 1..114) {
            try {
                // v5.14.1 — CRITICAL BUGFIX: "%03d".format() uses the device's
                // DEFAULT LOCALE. On Bengali/Arabic-locale phones %03d produced
                // localised digits ("০০১"/"٠٠١") so assets.open() failed for
                // EVERY per-surah file and every open fell back to the 4.7 MB
                // full-Quran parse. The first surah opened (always Al-Fatiha,
                // list item #1) hit the slow path, timed out and showed
                // "Surah #1 not available" — every later surah loaded fine
                // from the warm cache, which looked like "only Fatiha is
                // broken". padStart() is locale-INDEPENDENT and always
                // produces ASCII digits.
                val text = readAsset("quran/surah/surah_" + number.toString().padStart(3, '0') + ".json")
                val parsed = try {
                    gson.fromJson(text, SurahJson::class.java)
                } catch (e: JsonSyntaxException) {
                    null
                }
                // v5.14.0 — validate the parsed surah actually has ayahs; a
                // malformed/empty per-surah file now falls back to the full
                // Quran parse instead of surfacing an empty surah.
                if (parsed != null && parsed.ayahs.isNotEmpty()) {
                    val domain = parsed.toDomain()
                    perSurahCache[number] = domain
                    return@withContext domain
                }
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
        cache.surahs.firstOrNull { it.number == number }?.toDomain()?.also {
            // v5.15.1 — cache the fallback result too so the next open is instant.
            perSurahCache[number] = it
        }
    }

    /**
     * v5.15.1 — synchronous peek at an already-parsed surah (no IO). ViewModels
     * use this to seed their UI instantly on re-entry, killing the flash of a
     * fresh loading spinner every time the user opens the Quran.
     */
    fun peekCachedSurah(number: Int): Surah? = perSurahCache[number]

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
                        readAsset("quran/surah/surah_" + n.toString().padStart(3, '0') + ".json"),
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

    /**
     * v5.15.0 — Andi-style Bangla uccharon (quran/andi/uccharon.json).
     * Ported from the decoded Al Quran Bangla reference app — a higher
     * quality Bangla phonetic rendering than the legacy v5.5 dataset
     * (e.g. "বিছমিল্লাহির রাহমানির রাহিম।"). Loaded once, cached forever;
     * an empty map on any failure keeps the reader unaffected.
     */
    suspend fun loadAndiUccharon(): Map<Int, Map<Int, String>> = withContext(Dispatchers.IO) {
        andiUccharonCache?.let { return@withContext it }
        val parsed: Map<String, Map<String, String>>? = try {
            gson.fromJson(readAsset("quran/andi/uccharon.json"), uccaronType)
        } catch (_: Exception) {
            null
        }
        val mapped = parsed?.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
            ?.mapValues { (_, v) -> v.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 } }
            ?: emptyMap()
        andiUccharonCache = mapped
        mapped
    }

    /**
     * v5.15.0 — Andi Bangla meaning (quran/andi/meaning.json), the classic
     * মুহিউদ্দীন খান-style rendering from the reference app. Shown as the
     * optional "বিকল্প অর্থ" line and inside the word-by-word sheet.
     */
    suspend fun loadAndiMeaning(): Map<Int, Map<Int, String>> = withContext(Dispatchers.IO) {
        andiMeaningCache?.let { return@withContext it }
        val parsed: Map<String, Map<String, String>>? = try {
            gson.fromJson(readAsset("quran/andi/meaning.json"), uccaronType)
        } catch (_: Exception) {
            null
        }
        val mapped = parsed?.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
            ?.mapValues { (_, v) -> v.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 } }
            ?: emptyMap()
        andiMeaningCache = mapped
        mapped
    }

    /**
     * v5.18.0 — Latin (English) transliteration for every ayah, adopted
     * from the Muslimeen reference app's QuranDto.ayahTransliteration
     * (quran/quran_en_transliteration.json). Empty map on any failure.
     */
    suspend fun loadLatinTranslit(): Map<Int, Map<Int, String>> = withContext(Dispatchers.IO) {
        latinTranslitCache?.let { return@withContext it }
        val parsed: Map<String, Map<String, String>>? = try {
            gson.fromJson(readAsset("quran/quran_en_transliteration.json"), uccaronType)
        } catch (_: Exception) {
            null
        }
        val mapped = parsed?.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
            ?.mapValues { (_, v) -> v.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 } }
            ?: emptyMap()
        latinTranslitCache = mapped
        mapped
    }

    /**
     * v5.15.0 — REAL Quranic word-by-word data (quran/wbw/surah_NNN.json),
     * ported from the reference app's 77,425-entry wbw database. Each ayah
     * maps to an ordered list of (arabic word, Bangla meaning) pairs taken
     * from the same segmentation as the printed mushaf — no more guessing
     * meanings by splitting raw text. Per-surah files keep the parse small;
     * results are cached per surah for instant re-opens.
     */
    suspend fun loadWordByWord(surah: Int): Map<Int, List<WbwWord>> = withContext(Dispatchers.IO) {
        wbwCache[surah]?.let { return@withContext it }
        if (surah !in 1..114) return@withContext emptyMap()
        val parsed: WbwPayload? = try {
            gson.fromJson(
                readAsset("quran/wbw/surah_" + surah.toString().padStart(3, '0') + ".json"),
                WbwPayload::class.java
            )
        } catch (_: Exception) {
            null
        }
        val mapped: Map<Int, List<WbwWord>> = parsed?.ayahs
            ?.mapKeys { (k, _) -> k.toIntOrNull() ?: 0 }
            ?.mapValues { (_, words) -> words.filter { it.ar.isNotBlank() } }
            ?: emptyMap()
        wbwCache[surah] = mapped
        mapped
    }

    private fun readAsset(path: String): String {
        // v5.14.1 — trim a leading U+FEFF (BOM/zero-width) defensively so a
        // stray marker can never confuse Gson at the very first token.
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use {
            it.readText().trimStart('\uFEFF')
        }
    }

    private val uccaronType by lazy {
        object : com.google.gson.reflect.TypeToken<Map<String, Map<String, String>>>() {}.type
    }

    @Volatile private var bnUccaronCache: Map<Int, Map<Int, String>>? = null

    @Volatile private var andiUccharonCache: Map<Int, Map<Int, String>>? = null
    @Volatile private var andiMeaningCache: Map<Int, Map<Int, String>>? = null
    /** v5.18.0 — LATIN transliteration (adopted from Muslimeen QuranDto.ayahTransliteration). */
    @Volatile private var latinTranslitCache: Map<Int, Map<Int, String>>? = null
    private val wbwCache = java.util.concurrent.ConcurrentHashMap<Int, Map<Int, List<WbwWord>>>()

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

/** v5.15.0 — one word of the word-by-word dataset: arabic word + Bangla meaning. */
data class WbwWord(
    @SerializedName("ar") val ar: String,
    @SerializedName("tr") val tr: String
)

/** v5.15.0 — DTO for quran/wbw/surah_NNN.json ({"surah":N,"ayahs":{"1":[{ar,tr}…]}}). */
data class WbwPayload(
    @SerializedName("surah") val surah: Int,
    @SerializedName("ayahs") val ayahs: Map<String, List<WbwWord>>
)
