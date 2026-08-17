package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads hadith topics from extended-hadith-data.js → hadith_extended.json
 * 31 topics with categorized hadiths (each with Arabic, Bangla, reference, explanation, grade)
 */
class HadithTopicsAssetSource(private val context: Context) {
    private val gson = Gson()

    suspend fun loadHadithTopics(): HadithTopicsData = withContext(Dispatchers.IO) {
        val text = context.assets.open("data/hadith_extended.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        gson.fromJson(text, HadithTopicsData::class.java)
    }
}

data class HadithTopicsData(
    @SerializedName("hadith_topics") val hadithTopics: List<HadithTopic>?
)

data class HadithTopic(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("arabic") val arabicName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("subcategory") val subcategory: String?,
    @SerializedName("hadiths") val hadiths: List<TopicHadith>?
)

data class TopicHadith(
    @SerializedName("id") val id: Int,
    @SerializedName("arabic") val arabic: String?,
    @SerializedName("bangla") val bangla: String?,
    @SerializedName("reference") val reference: String?,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("grade") val grade: String?,
    @SerializedName("audio") val audio: String?
)
