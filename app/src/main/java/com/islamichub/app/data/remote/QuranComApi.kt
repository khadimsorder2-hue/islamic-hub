package com.islamichub.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Quran.com API v4 — free, no Cloudflare protection, no API key required.
 *
 * Base URL: https://api.quran.com/api/v4
 *
 * Endpoints used:
 *  - GET /verses/by_key/{verse_key} — single verse with Arabic + translations + transliteration
 *  - GET /verses/by_juz/{juz_number} — all verses in a para/juz
 *  - GET /verses/by_chapter/{chapter_number} — all verses in a surah
 *  - GET /chapters — list of all 114 surahs
 *  - GET /search?keyword=... — search verses by keyword
 *
 * Translations:
 *  - ID 163: Sheikh Mujibur Rahman (Bengali)
 *  - ID 84: T. Usmani (English)
 *  - ID 213: Dr. Abu Bakr Muhammad Zakaria (Bengali)
 *
 * Word fields: text_uthmani, transliteration (for Bangla pronunciation)
 */
interface QuranComApi {

    /** Get a single verse by key with translations + tafsirs + transliteration */
    @GET("verses/by_key/{verse_key}")
    suspend fun getVerseByKey(
        @Path("verse_key") verseKey: String,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translations") translations: String = "163,213,84",
        @Query("tafsirs") tafsirs: String = "164,166",
        @Query("words") words: Boolean = true,
        @Query("word_fields") wordFields: String = "text_uthmani,transliteration"
    ): retrofit2.Response<VerseByKeyEvent>

    /** Get all verses in a surah (chapter) */
    @GET("verses/by_chapter/{chapter_number}")
    suspend fun getVersesByChapter(
        @Path("chapter_number") chapterNumber: Int,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translations") translations: String = "163,213,84",
        @Query("per_page") perPage: Int = 300,
        @Query("words") words: Boolean = false
    ): retrofit2.Response<VersesByChapterEvent>

    /** Get all verses in a juz (para) */
    @GET("verses/by_juz/{juz_number}")
    suspend fun getVersesByJuz(
        @Path("juz_number") juzNumber: Int,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translations") translations: String = "163,84",
        @Query("per_page") perPage: Int = 300,
        @Query("page") page: Int = 1
    ): retrofit2.Response<VersesByJuzEvent>

    /** Search verses by keyword */
    @GET("search")
    suspend fun searchVerses(
        @Query("keyword") keyword: String,
        @Query("size") size: Int = 50,
        @Query("page") page: Int = 1,
        @Query("translations") translations: String = "163,84"
    ): retrofit2.Response<SearchEvent>

    /** Get all 114 chapters (surahs) */
    @GET("chapters")
    suspend fun getChapters(
        @Query("language") language: String = "en"
    ): retrofit2.Response<ChaptersEvent>

    companion object {
        const val BASE_URL = "https://api.quran.com/api/v4/"

        // Bangla translations
        const val TRANSLATION_BN_MUJIB = 163      // Sheikh Mujibur Rahman (Darussalaam)
        const val TRANSLATION_BN_TAISIRUL = 161   // Taisirul Quran (Tawheed Publication)
        const val TRANSLATION_BN_RAWAI = 162      // Rawai Al-bayan (Bayaan Foundation)
        const val TRANSLATION_BN_ZAKARIA = 213    // Dr. Abu Bakr Muhammad Zakaria

        // English translation
        const val TRANSLATION_EN_USMANI = 84      // T. Usmani

        // Bangla tafsirs
        const val TAFSIR_BN_IBN_KATHIR = 164     // Tafseer Ibn Kathir (Bangla)
        const val TAFSIR_BN_AHSANUL = 165        // Tafsir Ahsanul Bayaan
        const val TAFSIR_BN_ZAKARIA = 166         // Tafsir Abu Bakr Zakaria
        const val TAFSIR_BN_FATHUL_MAJID = 381   // Tafsir Fathul Majid

        // English tafsirs
        const val TAFSIR_EN_IBN_KATHIR = 169     // Ibn Kathir (Abridged, English)

        // Default params — all Bangla translations + Bangla tafsirs
        const val DEFAULT_TRANSLATIONS = "163,161,213,84"
        const val DEFAULT_TAFSIRS = "164,166"
    }
}

// ─── Response DTOs ───────────────────────────────────────────────────────────

data class VerseByKeyEvent(
    @SerializedName("verse") val verse: VerseApi?
)

data class VersesByChapterEvent(
    @SerializedName("verses") val verses: List<VerseApi>?,
    @SerializedName("pagination") val pagination: PaginationApi?
)

data class VersesByJuzEvent(
    @SerializedName("verses") val verses: List<VerseApi>?,
    @SerializedName("pagination") val pagination: PaginationApi?
)

data class SearchEvent(
    @SerializedName("search") val search: SearchDataApi?
)

data class SearchDataApi(
    @SerializedName("query") val query: String?,
    @SerializedName("total_results") val totalResults: Int?,
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("total_pages") val totalPages: Int?,
    @SerializedName("results") val results: List<SearchResultApi>?
)

data class SearchResultApi(
    @SerializedName("verse_key") val verseKey: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("translations") val translations: List<TranslationApi>?
)

data class VerseApi(
    @SerializedName("id") val id: Int?,
    @SerializedName("verse_number") val verseNumber: Int?,
    @SerializedName("verse_key") val verseKey: String?,
    @SerializedName("text_uthmani") val textUthmani: String?,
    @SerializedName("juz_number") val juzNumber: Int?,
    @SerializedName("page_number") val pageNumber: Int?,
    @SerializedName("translations") val translations: List<TranslationApi>?,
    @SerializedName("tafsirs") val tafsirs: List<TafsirApi>?,
    @SerializedName("words") val words: List<WordApi>?
) {
    /** Parse verse_key "2:255" → Pair(2, 255) */
    fun parseSurahAyah(): Pair<Int, Int>? {
        verseKey?.let { key ->
            val parts = key.split(":")
            if (parts.size == 2) {
                val s = parts[0].toIntOrNull() ?: return@let
                val a = parts[1].toIntOrNull() ?: return@let
                return s to a
            }
        }
        return null
    }

    /** Get Bangla translation — Sheikh Mujibur Rahman (Darussalaam) */
    fun getBanglaTranslationMujib(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_BN_MUJIB }?.text
    }

    /** Get Bangla translation — Taisirul Quran (Tawheed Publication) */
    fun getBanglaTranslationTaisirul(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_BN_TAISIRUL }?.text
    }

    /** Get Bangla translation — Dr. Abu Bakr Muhammad Zakaria */
    fun getBanglaTranslationZakaria(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_BN_ZAKARIA }?.text
    }

    /** Get Bangla translation — Rawai Al-bayan */
    fun getBanglaTranslationRawai(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_BN_RAWAI }?.text
    }

    /** Get default Bangla translation (Mujibur Rahman) */
    fun getBanglaTranslation(): String? = getBanglaTranslationMujib()

    /** Get English translation text */
    fun getEnglishTranslation(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_EN_USMANI }?.text
    }

    /** Get all available Bangla translations as list of (name, text) pairs */
    fun getAllBanglaTranslations(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        getBanglaTranslationMujib()?.let { result.add("মুহিউদ্দীন খান" to it) }
        getBanglaTranslationTaisirul()?.let { result.add("তাইসিরুল কুরআন" to it) }
        getBanglaTranslationZakaria()?.let { result.add("ড. আবু বকর মুহাম্মদ যাকারিয়া" to it) }
        getBanglaTranslationRawai()?.let { result.add("রাওয়ায়ে বায়ান" to it) }
        return result
    }

    /** Get Bangla tafsir — Ibn Kathir */
    fun getTafsirIbnKathirBn(): String? {
        return tafsirs?.find { it.id == QuranComApi.TAFSIR_BN_IBN_KATHIR }?.text
    }

    /** Get Bangla tafsir — Abu Bakr Zakaria */
    fun getTafsirZakariaBn(): String? {
        return tafsirs?.find { it.id == QuranComApi.TAFSIR_BN_ZAKARIA }?.text
    }

    /** Get Bangla tafsir — Ahsanul Bayaan */
    fun getTafsirAhsanulBn(): String? {
        return tafsirs?.find { it.id == QuranComApi.TAFSIR_BN_AHSANUL }?.text
    }

    /** Get Bangla tafsir — Fathul Majid */
    fun getTafsirFathulMajidBn(): String? {
        return tafsirs?.find { it.id == QuranComApi.TAFSIR_BN_FATHUL_MAJID }?.text
    }

    /** Get all available Bangla tafsirs as list of (name, text) pairs */
    fun getAllBanglaTafsirs(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        getTafsirIbnKathirBn()?.let { result.add("তাফসীর ইবনে কাসীর" to it) }
        getTafsirZakariaBn()?.let { result.add("তাফসীর আবু বকর যাকারিয়া" to it) }
        getTafsirAhsanulBn()?.let { result.add("তাফসীর আহসানুল বায়ান" to it) }
        getTafsirFathulMajidBn()?.let { result.add("তাফসীর ফাতহুল মজীদ" to it) }
        return result
    }

    /** Build Bangla transliteration from word-level data */
    fun getTransliteration(): String? {
        val words = words ?: return null
        val parts = words.filter { it.transliteration?.text != null }
            .map { it.transliteration?.text ?: "" }
        return if (parts.isNotEmpty()) parts.joinToString(" ") else null
    }

    /** Build Bangla pronunciation from transliteration (simplified) */
    fun getBanglaPronunciation(): String? {
        val translit = getTransliteration() ?: return null
        return translit
    }
}

data class TranslationApi(
    @SerializedName("id") val id: Int?,
    @SerializedName("text") val text: String?,
    @SerializedName("language_name") val languageName: String?
)

data class WordApi(
    @SerializedName("id") val id: Int?,
    @SerializedName("position") val position: Int?,
    @SerializedName("text_uthmani") val textUthmani: String?,
    @SerializedName("transliteration") val transliteration: TransliterationApi?,
    @SerializedName("translation") val translation: String?
)

data class TransliterationApi(
    @SerializedName("text") val text: String?,
    @SerializedName("language_name") val languageName: String?
)

data class PaginationApi(
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("next_page") val nextPage: Int?,
    @SerializedName("prev_page") val prevPage: Int?,
    @SerializedName("total_pages") val totalPages: Int?,
    @SerializedName("total_records") val totalRecords: Int?,
    @SerializedName("per_page") val perPage: Int?
)

data class ChaptersEvent(
    @SerializedName("chapters") val chapters: List<ChapterApi>?
)

data class ChapterApi(
    @SerializedName("id") val id: Int,
    @SerializedName("name_simple") val nameSimple: String?,
    @SerializedName("name_arabic") val nameArabic: String?,
    @SerializedName("translated_name") val translatedName: TranslatedNameApi?,
    @SerializedName("verses_count") val versesCount: Int?,
    @SerializedName("revelation_place") val revelationPlace: String?
)

data class TranslatedNameApi(
    @SerializedName("name") val name: String?
)

data class TafsirApi(
    @SerializedName("id") val id: Int?,
    @SerializedName("text") val text: String?,
    @SerializedName("language_name") val languageName: String?,
    @SerializedName("resource_name") val resourceName: String?
)
