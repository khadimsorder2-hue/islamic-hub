package com.islamichub.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Islamic.app Topics API — public CC BY 4.0 thematic Quran topic dataset.
 *
 * Reference: https://docs.islamic.app/api-reference/topics
 *
 * Coverage (per docs):
 *  - 338 Quran topics
 *  - 18 domains
 *  - 70 categories
 *  - all 6,236 ayahs covered
 *  - average ~2.6 topic tags per ayah
 *  - topic hierarchy (parent/child)
 *  - topic search
 *  - topic → paginated ayahs
 *  - language parameter
 *
 * Native Android is not subject to browser CORS, so we call the API directly.
 * Falls back to bundled [com.islamichub.app.ui.screens.topic_study.TopicStudyData]
 * when offline or on API failure.
 *
 * License: CC BY 4.0 (attribution required — shown in source-attribution UI).
 */
interface IslamicAppApi {

    /** List all topics (paginated). */
    @GET("v1/topics")
    suspend fun getTopics(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("lang") lang: String = "bn"
    ): retrofit2.Response<TopicsListResponse>

    /** Search topics by name (Bangla / English / Arabic). */
    @GET("v1/topics/search")
    suspend fun searchTopics(
        @Query("q") query: String,
        @Query("lang") lang: String = "bn"
    ): retrofit2.Response<TopicsListResponse>

    /** Get a single topic with its ayahs (paginated). */
    @GET("v1/topics/{slug}")
    suspend fun getTopic(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("lang") lang: String = "bn"
    ): retrofit2.Response<TopicDetailApiResponse>

    /** Get all topic domains (categories groups). */
    @GET("v1/topic-domains")
    suspend fun getDomains(
        @Query("lang") lang: String = "bn"
    ): retrofit2.Response<DomainsResponse>

    companion object {
        const val BASE_URL = "https://api.islamic.app/"
    }
}

// ─── Response DTOs ───────────────────────────────────────────────────────────

data class TopicsListResponse(
    @SerializedName("data") val data: List<TopicApi>? = null,
    @SerializedName("topics") val topics: List<TopicApi>? = null,
    @SerializedName("meta") val meta: PaginationMeta? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null
) {
    val items: List<TopicApi> get() = data ?: topics ?: emptyList()
}

data class TopicApi(
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("name_bn") val nameBn: String? = null,
    @SerializedName("name_en") val nameEn: String? = null,
    @SerializedName("name_ar") val nameAr: String? = null,
    @SerializedName("translated_name") val translatedName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("description_bn") val descriptionBn: String? = null,
    @SerializedName("description_en") val descriptionEn: String? = null,
    @SerializedName("domain_id") val domainId: Int? = null,
    @SerializedName("domain_name") val domainName: String? = null,
    @SerializedName("domain_name_bn") val domainNameBn: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("category_name_bn") val categoryNameBn: String? = null,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("ayahs_count") val ayahsCount: Int? = null,
    @SerializedName("total_ayahs") val totalAyahs: Int? = null,
    @SerializedName("children") val children: List<TopicApi>? = null,
    @SerializedName("related_topics") val relatedTopics: List<TopicApi>? = null
)

data class TopicDetailApiResponse(
    @SerializedName("data") val data: TopicDetailApi? = null,
    @SerializedName("topic") val topic: TopicDetailApi? = null,
    @SerializedName("ayahs") val ayahs: List<TopicAyahApi>? = null,
    @SerializedName("verses") val verses: List<TopicAyahApi>? = null,
    @SerializedName("meta") val meta: PaginationMeta? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null
) {
    val topicData: TopicDetailApi? get() = data ?: topic
    val ayahList: List<TopicAyahApi> get() = ayahs ?: verses ?: emptyList()
}

data class TopicDetailApi(
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("name_bn") val nameBn: String? = null,
    @SerializedName("name_en") val nameEn: String? = null,
    @SerializedName("name_ar") val nameAr: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("description_bn") val descriptionBn: String? = null,
    @SerializedName("description_en") val descriptionEn: String? = null,
    @SerializedName("domain_name") val domainName: String? = null,
    @SerializedName("domain_name_bn") val domainNameBn: String? = null,
    @SerializedName("category_name_bn") val categoryNameBn: String? = null,
    @SerializedName("ayahs_count") val ayahsCount: Int? = null,
    @SerializedName("total_ayahs") val totalAyahs: Int? = null
)

data class TopicAyahApi(
    @SerializedName("verse_key") val verseKey: String? = null,
    @SerializedName("verse_number") val verseNumber: Int? = null,
    @SerializedName("surah_number") val surahNumber: Int? = null,
    @SerializedName("ayah_number") val ayahNumber: Int? = null,
    @SerializedName("surah_name") val surahName: String? = null,
    @SerializedName("surah_name_bn") val surahNameBn: String? = null,
    @SerializedName("arabic") val arabic: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("bangla") val bangla: String? = null,
    @SerializedName("translation") val translation: String? = null,
    @SerializedName("english") val english: String? = null,
    @SerializedName("tafsir") val tafsir: String? = null,
    @SerializedName("relevance") val relevance: Int? = null,
    @SerializedName("is_primary") val isPrimary: Boolean? = null
) {
    /** Parse "2:255" → Pair(2, 255). Falls back to surah/ayah fields. */
    fun parseReference(): Pair<Int, Int>? {
        verseKey?.let { key ->
            val parts = key.split(":")
            if (parts.size == 2) {
                val s = parts[0].toIntOrNull() ?: return@let
                val a = parts[1].toIntOrNull() ?: return@let
                return s to a
            }
        }
        val s = surahNumber
        val a = ayahNumber ?: verseNumber
        return if (s != null && a != null) s to a else null
    }
}

data class DomainsResponse(
    @SerializedName("data") val data: List<DomainApi>? = null,
    @SerializedName("domains") val domains: List<DomainApi>? = null
) {
    val items: List<DomainApi> get() = data ?: domains ?: emptyList()
}

data class DomainApi(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("name_bn") val nameBn: String? = null,
    @SerializedName("name_en") val nameEn: String? = null,
    @SerializedName("topics_count") val topicsCount: Int? = null
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("total") val total: Int? = null
)
