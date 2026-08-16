package com.islamichub.app.data.repo

import com.islamichub.app.data.local.HadithAssetSource
import com.islamichub.app.data.local.HadithCollectionMeta
import com.islamichub.app.data.local.HadithJson
import com.islamichub.app.data.local.HadithCollectionJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only Hadith repository. Loads from bundled JSON assets.
 * Supports cross-collection search, per-collection listing, and per-hadith retrieval.
 */
class HadithRepository(private val assetSource: HadithAssetSource) {

    suspend fun listCollections(): List<HadithCollectionMeta> = withContext(Dispatchers.IO) {
        assetSource.loadIndex()
    }

    suspend fun getCollection(collectionId: String): HadithCollectionJson = withContext(Dispatchers.IO) {
        assetSource.loadCollection(collectionId)
    }

    suspend fun listHadiths(collectionId: String, limit: Int? = null): List<HadithJson> = withContext(Dispatchers.IO) {
        val coll = assetSource.loadCollection(collectionId)
        if (limit != null) coll.hadiths.take(limit) else coll.hadiths
    }

    suspend fun getHadith(collectionId: String, hadithNumber: Int): HadithJson? = withContext(Dispatchers.IO) {
        assetSource.loadCollection(collectionId).hadiths.firstOrNull { it.hadithNumber == hadithNumber }
    }

    suspend fun searchInCollection(collectionId: String, query: String): List<HadithJson> = withContext(Dispatchers.IO) {
        assetSource.searchInCollection(collectionId, query)
    }

    suspend fun searchAll(query: String): List<HadithSearchResult> = withContext(Dispatchers.IO) {
        assetSource.searchAll(query).map { (cid, hadith) ->
            val coll = assetSource.loadCollection(cid)
            HadithSearchResult(
                collectionId = cid,
                collectionName = coll.collectionName,
                collectionNameBn = coll.collectionNameBn,
                hadithNumber = hadith.hadithNumber,
                arabic = hadith.arabic,
                bangla = hadith.bangla,
                chapterId = hadith.chapterId
            )
        }
    }
}

data class HadithSearchResult(
    val collectionId: String,
    val collectionName: String,
    val collectionNameBn: String,
    val hadithNumber: Int,
    val arabic: String,
    val bangla: String,
    val chapterId: Int
)
