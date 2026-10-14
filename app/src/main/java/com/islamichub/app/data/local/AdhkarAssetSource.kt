package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * v5.18.0 — Zikr & Dua hub data, adopted from the Muslimeen app's
 * adhkar database (37 categories → 208 topics → 265 adhkar) with
 * Bangla meanings. Single JSON asset: data/adhkar_full.json.
 */
class AdhkarAssetSource(private val context: Context) {

    private val gson = Gson()

    suspend fun load(): AdhkarData = withContext(Dispatchers.IO) {
        val text = context.assets.open("data/adhkar_full.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        gson.fromJson(text, AdhkarData::class.java)
    }
}

data class AdhkarData(
    @SerializedName("categories") val categories: List<AdhkarCategory> = emptyList(),
    @SerializedName("topics") val topics: List<AdhkarTopic> = emptyList(),
    @SerializedName("adhkar") val adhkar: List<AdhkarItem> = emptyList(),
    @SerializedName("topicAdhkar") val topicAdhkar: List<AdhkarTopicLink> = emptyList(),
    @SerializedName("topicCategories") val topicCategories: List<AdhkarTopicCategory> = emptyList()
) {
    /** Topics that belong to a category (via topicCategories mapping, sorted). */
    fun topicsForCategory(categoryId: Int): List<AdhkarTopic> {
        val topicIds = topicCategories
            .filter { it.categoryId == categoryId }
            .sortedBy { it.sortOrder }
            .map { it.topicId }
            .toSet()
        return topics.filter { it.id in topicIds }
    }

    /** Adhkar items of a topic (via topicAdhkar mapping, sorted). */
    fun adhkarForTopic(topicId: Int): List<AdhkarItem> {
        val links = topicAdhkar.filter { it.topicId == topicId }.sortedBy { it.sortOrder }
        val byId = adhkar.associateBy { it.id }
        return links.mapNotNull { byId[it.adhkarId] }
    }
}

data class AdhkarCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("nameBn") val nameBn: String?,
    @SerializedName("imageName") val imageName: String?,
    @SerializedName("sortOrder") val sortOrder: Int = 0,
    @SerializedName("showTopic") val showTopic: Int = 0
)

data class AdhkarTopic(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("nameBn") val nameBn: String?
)

data class AdhkarItem(
    @SerializedName("id") val id: Int,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("transliteration") val transliteration: String?,
    @SerializedName("meaning") val meaning: String?,
    @SerializedName("meaningBn") val meaningBn: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("remarks") val remarks: String?
) {
    /** Display meaning: Bangla preferred, English fallback. */
    fun displayMeaning(): String = meaningBn?.takeIf { it.isNotBlank() } ?: meaning ?: ""
}

data class AdhkarTopicLink(
    @SerializedName("topicId") val topicId: Int,
    @SerializedName("adhkarId") val adhkarId: Int,
    @SerializedName("sortOrder") val sortOrder: Int = 0,
    @SerializedName("count") val count: Int = 0
)

data class AdhkarTopicCategory(
    @SerializedName("topicId") val topicId: Int,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("sortOrder") val sortOrder: Int = 0
)
