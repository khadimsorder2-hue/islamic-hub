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

    /** Get a single verse by key (e.g. "2:255" = Surah 2, Ayah 255) */
    @GET("verses/by_key/{verse_key}")
    suspend fun getVerseByKey(
        @Path("verse_key") verseKey: String,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translations") translations: String = "163,84",
        @Query("words") words: Boolean = true,
        @Query("word_fields") wordFields: String = "text_uthmani,transliteration"
    ): retrofit2.Response<VerseByKeyEvent>

    /** Get all verses in a surah (chapter) */
    @GET("verses/by_chapter/{chapter_number}")
    suspend fun getVersesByChapter(
        @Path("chapter_number") chapterNumber: Int,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translations") translations: String = "163,84",
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

        // Translation IDs
        const val TRANSLATION_BN_MUJIB = 163      // Sheikh Mujibur Rahman (Bengali)
        const val TRANSLATION_EN_USMANI = 84      // T. Usmani (English)
        const val TRANSLATION_BN_ZAKARIA = 213    // Dr. Abu Bakr Muhammad Zakaria (Bengali)

        // Default translation params
        const val DEFAULT_TRANSLATIONS = "163,84"
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

    /** Get Bangla translation text */
    fun getBanglaTranslation(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_BN_MUJIB }?.text
    }

    /** Get English translation text */
    fun getEnglishTranslation(): String? {
        return translations?.find { it.id == QuranComApi.TRANSLATION_EN_USMANI }?.text
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
        // Simple mapping: transliteration is in English, we return it as-is
        // (Bangla pronunciation would require a proper transliteration library)
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
