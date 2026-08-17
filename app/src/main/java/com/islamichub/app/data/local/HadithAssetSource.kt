package com.islamichub.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Loads hadith collections from bundled JSON assets.
 *
 * Asset format (see scripts/download_hadith.py):
 *   app/src/main/assets/hadith/
 *     index.json        # Collection metadata
 *     bukhari.json      # 7589 hadiths (Arabic + Bangla)
 *     muslim.json       # 7563 hadiths
 *     tirmidhi.json     # 3998 hadiths
 *     abudawud.json     # 5274 hadiths
 *
 * Each collection file:
 * {
 *   "collection": "bukhari",
 *   "collectionName": "Sahih al-Bukhari",
 *   "collectionNameBn": "সহীহ বুখারী",
 *   "totalHadiths": 7589,
 *   "chapters": [{"id": 1, "name": "...", "nameBn": "..."}],
 *   "hadiths": [
 *     {
 *       "hadithNumber": 1,
 *       "chapterId": 1,
 *       "arabic": "...",
 *       "bangla": "...",
 *       "grades": [...],
 *       "reference": {...}
 *     }
 *   ]
 * }
 */
class HadithAssetSource(private val context: Context) {

    private val gson = Gson()

    private var indexCache: HadithIndex? = null
    private val collectionCache = mutableMapOf<String, HadithCollectionJson>()

    suspend fun loadIndex(): List<HadithCollectionMeta> = withContext(Dispatchers.IO) {
        indexCache?.let { return@withContext it.collections }
        val text = readAsset("hadith/index.json")
        val parsed = try {
            gson.fromJson(text, HadithIndex::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } ?: throw IOException("Failed to parse hadith/index.json")
        indexCache = parsed
        parsed.collections
    }

    suspend fun loadCollection(collectionId: String): HadithCollectionJson = withContext(Dispatchers.IO) {
        collectionCache[collectionId]?.let { return@withContext it }
        val text = readAsset("hadith/$collectionId.json")
        val parsed = try {
            gson.fromJson(text, HadithCollectionJson::class.java)
        } catch (e: JsonSyntaxException) {
            null
        } ?: throw IOException("Failed to parse hadith/$collectionId.json")
        collectionCache[collectionId] = parsed
        parsed
    }

    /**
     * Search across a specific collection. Returns matching hadiths.
     */
    suspend fun searchInCollection(
        collectionId: String,
        query: String,
        limit: Int = 100
    ): List<HadithJson> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val q = query.trim()
        val qLower = q.lowercase()
        val collection = loadCollection(collectionId)
        val results = mutableListOf<HadithJson>()
        for (h in collection.hadiths) {
            if (h.bangla.contains(q, ignoreCase = true) ||
                h.arabic.contains(q) ||
                h.hadithNumber.toString() == q
            ) {
                results.add(h)
                if (results.size >= limit) break
            }
        }
        results
    }

    /**
     * Cross-collection search. Returns (collectionId, hadith) pairs.
     */
    suspend fun searchAll(
        query: String,
        limit: Int = 200
    ): List<Pair<String, HadithJson>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val idx = loadIndex()
        val results = mutableListOf<Pair<String, HadithJson>>()
        for (coll in idx) {
            try {
                val matches = searchInCollection(coll.id, query, limit = limit / idx.size + 10)
                matches.forEach { results.add(coll.id to it) }
                if (results.size >= limit) break
            } catch (_: Exception) {
                // skip failed collection
            }
        }
        results.take(limit)
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

// ─── JSON DTOs ──────────────────────────────────────────────────────────────

data class HadithIndex(
    @SerializedName("collections") val collections: List<HadithCollectionMeta>
)

data class HadithCollectionMeta(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("nameBn") val nameBn: String,
    @SerializedName("totalHadiths") val totalHadiths: Int,
    @SerializedName("totalChapters") val totalChapters: Int
)

data class HadithCollectionJson(
    @SerializedName("collection") val collection: String,
    @SerializedName("collectionName") val collectionName: String,
    @SerializedName("collectionNameBn") val collectionNameBn: String,
    @SerializedName("totalHadiths") val totalHadiths: Int,
    @SerializedName("chapters") val chapters: List<HadithChapterJson>,
    @SerializedName("hadiths") val hadiths: List<HadithJson>
)

data class HadithChapterJson(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("nameBn") val nameBn: String
)

data class HadithJson(
    @SerializedName("hadithNumber") val hadithNumber: Int = 0,
    @SerializedName("chapterId") val chapterId: Int = 0,
    @SerializedName("arabic") val arabic: String = "",
    @SerializedName("bangla") val bangla: String = "",
    @SerializedName("grades") val grades: List<Map<String, String>>? = null,
    @SerializedName("reference") val reference: Map<String, Any>? = null
)
