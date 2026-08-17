package com.islamichub.app.data.repo

import com.islamichub.app.data.local.AnsCategory
import com.islamichub.app.data.local.AnsData
import com.islamichub.app.data.local.AnsQA
import com.islamichub.app.data.local.ContentAssetSource
import com.islamichub.app.data.local.ExtendedDua
import com.islamichub.app.data.local.ExtendedDuaCategory
import com.islamichub.app.data.local.ExtendedDuasData
import com.islamichub.app.data.local.KalimaData
import com.islamichub.app.data.local.MisconceptionsData
import com.islamichub.app.data.local.NamazExtrasData
import com.islamichub.app.data.local.NamazShikkhaData
import com.islamichub.app.data.local.StoriesData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for extended content from bundled JSON assets.
 * Fixed to match actual JSON structure from web source.
 */
class ContentRepository(private val source: ContentAssetSource) {

    suspend fun loadMisconceptions(): MisconceptionsData = withContext(Dispatchers.IO) {
        source.loadAsset("misconceptions.json", MisconceptionsData::class.java)
    }

    suspend fun loadNamazShikkha(): NamazShikkhaData = withContext(Dispatchers.IO) {
        source.loadAsset("namaz_shikkha_full.json", NamazShikkhaData::class.java)
    }

    suspend fun loadNamazExtras(): NamazExtrasData = withContext(Dispatchers.IO) {
        source.loadAsset("namaz_extras.json", NamazExtrasData::class.java)
    }

    suspend fun loadStories(): StoriesData = withContext(Dispatchers.IO) {
        source.loadAsset("stories_full.json", StoriesData::class.java)
    }

    suspend fun loadKalima(): KalimaData = withContext(Dispatchers.IO) {
        source.loadAsset("kalima.json", KalimaData::class.java)
    }

    /**
     * Load extended duas. The JSON has duas in a separate array with a `category` field.
     * We group them by category for display.
     */
    suspend fun loadExtendedDuas(): ExtendedDuasData = withContext(Dispatchers.IO) {
        source.loadAsset("duas_extended.json", ExtendedDuasData::class.java)
    }

    /**
     * Load Q&A data. The JSON has categories as top-level keys (not a list).
     * Returns the raw AnsData which can be converted to a list.
     */
    suspend fun loadAnsData(): AnsData = withContext(Dispatchers.IO) {
        source.loadAsset("ans.json", AnsData::class.java)
    }

    suspend fun searchMisconceptions(query: String): List<MisconceptionSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val data = loadMisconceptions()
        val q = query.trim().lowercase()
        val results = mutableListOf<MisconceptionSearchResult>()
        for (cat in data.categories) {
            for (item in cat.questions) {
                if (item.question.contains(query, ignoreCase = true) ||
                    item.answer?.contains(query, ignoreCase = true) == true
                ) {
                    results.add(
                        MisconceptionSearchResult(
                            categoryId = cat.id,
                            categoryName = cat.name,
                            itemId = item.id,
                            question = item.question,
                            answer = item.answer ?: ""
                        )
                    )
                    if (results.size >= 50) return@withContext results
                }
            }
        }
        results
    }
}

data class MisconceptionSearchResult(
    val categoryId: String,
    val categoryName: String,
    val itemId: String,
    val question: String,
    val answer: String
)
