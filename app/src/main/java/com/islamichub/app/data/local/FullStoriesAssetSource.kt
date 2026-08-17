package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads FULL stories data from web source converted JSON.
 * Source: islamic-stories-data.js → stories_full.json (42KB)
 * 9 prophets + 4 khalifas + Meraj (4 chapters) + Sirat (34 chapters)
 */
class FullStoriesAssetSource(private val context: Context) {

    private val gson = Gson()

    suspend fun loadFullStories(): FullStoriesData = withContext(Dispatchers.IO) {
        val text = context.assets.open("data/stories_full.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        gson.fromJson(text, FullStoriesData::class.java)
    }
}

data class FullStoriesData(
    @SerializedName("meraj") val meraj: FullStorySection?,
    @SerializedName("sirat") val sirat: FullStorySection?,
    @SerializedName("prophets") val prophets: List<FullProphet>?,
    @SerializedName("khalifas") val List<FullKhalifa>?
)

data class FullStorySection(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("emoji") val emoji: String?,
    @SerializedName("chapters") val chapters: List<FullStoryChapter>?
)

data class FullStoryChapter(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("highlight") val highlight: String?
)

data class FullProphet(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("emoji") val emoji: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("summary") val summary: String?,
    @SerializedName("details") val details: String?,
    @SerializedName("ref") val ref: String?
)

data class FullKhalifa(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("emoji") val emoji: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("khilafat") val khilafat: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("summary") val summary: String?,
    @SerializedName("details") val details: String?
)
