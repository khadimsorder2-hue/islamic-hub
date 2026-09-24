package com.islamichub.app.data.repo

import com.islamichub.app.data.local.QuranAssetSource
import com.islamichub.app.data.model.Ayah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * ThematicQuranEngine (v5.3.1) — keyword-scoring engine over the bundled
 * full Quran (114 surahs / 6,236 ayahs, fully offline).
 *
 * This is the Quran-side twin of HadithTopicRepository.scoreHadithForTopic():
 *   +3 relevance points per Bangla keyword match   (bengali translation field)
 *   +2 relevance points per Arabic keyword match   (uthmani arabic field)
 *
 * Before this engine the app shipped only 7 hand-curated topics with static
 * ayah lists (TopicStudyData.kt). Now every catalog topic resolves its full
 * matching ayah set from the whole Quran — "fully loaded" thematic study.
 *
 * Performance notes:
 *  - The Quran payload is parsed once by QuranAssetSource (fullQuranCache).
 *  - Per-topic scoring results and counts are memoized in-memory.
 *  - computeAllCounts() scans the Quran in a SINGLE pass for the whole
 *    catalog so the topic list can show real ayah counts cheaply.
 */
class ThematicQuranEngine(private val assetSource: QuranAssetSource?) {

    /** One scored ayah for a topic */
    data class ScoredAyah(
        val surahNumber: Int,
        val ayahNumber: Int,
        val arabic: String,
        val bengali: String,
        val english: String,
        val relevanceScore: Int,
        val matchedKeywords: List<String>
    )

    private val scoreMutex = Mutex()
    private val countMutex = Mutex()
    private val scoreCache = HashMap<String, List<ScoredAyah>>()
    private val countCache = HashMap<String, Int>()

    /**
     * Score every ayah of the Quran against [topic].
     * Results sorted by relevance (desc), then canonical mushaf order.
     */
    suspend fun scoreTopic(
        topic: QuranKeywordTopic,
        minScore: Int = QuranTopicCatalog.MIN_MATCH_SCORE,
        limit: Int = QuranTopicCatalog.MAX_SCORED_AYAHS
    ): List<ScoredAyah> = withContext(Dispatchers.IO) {
        scoreMutex.withLock {
            scoreCache[topic.slug]?.let { return@withContext it.take(limit) }
        }
        val all = try {
            assetSource?.loadAllSurahs()
        } catch (_: Exception) {
            null
        } ?: return@withContext emptyList()

        val scored = mutableListOf<ScoredAyah>()
        for (surah in all) {
            for (ayah in surah.ayahs) {
                val (score, matched) = scoreAyah(ayah, topic)
                if (score >= minScore) {
                    scored.add(
                        ScoredAyah(
                            surahNumber = surah.number,
                            ayahNumber = ayah.numberInSurah,
                            arabic = ayah.arabic,
                            bengali = ayah.bengali,
                            english = ayah.english,
                            relevanceScore = score,
                            matchedKeywords = matched
                        )
                    )
                }
            }
        }
        scored.sortWith(
            compareByDescending<ScoredAyah> { it.relevanceScore }
                .thenBy { it.surahNumber }
                .thenBy { it.ayahNumber }
        )
        scoreMutex.withLock {
            scoreCache[topic.slug] = scored
            countCache[topic.slug] = scored.size
        }
        scored.take(limit)
    }

    /**
     * Count matching ayahs for one topic (uses/populates the score cache).
     */
    suspend fun countFor(topic: QuranKeywordTopic): Int = withContext(Dispatchers.IO) {
        countMutex.withLock {
            countCache[topic.slug]?.let { return@withContext it }
        }
        val scored = scoreTopic(topic, limit = Int.MAX_VALUE)
        scored.size
    }

    /**
     * Single-pass count for the WHOLE catalog (list screen badges).
     * One Quran scan instead of N scans; memoized afterwards.
     */
    suspend fun computeAllCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        val catalog = QuranTopicCatalog.topics
        countMutex.withLock {
            if (countCache.size >= catalog.size) {
                return@withContext catalog.associate { it.slug to (countCache[it.slug] ?: 0) }
            }
        }
        val all = try {
            assetSource?.loadAllSurahs()
        } catch (_: Exception) {
            null
        } ?: return@withContext emptyMap()

        val local = HashMap<String, Int>(catalog.size)
        catalog.forEach { local[it.slug] = 0 }
        for (surah in all) {
            for (ayah in surah.ayahs) {
                for (topic in catalog) {
                    val (score, _) = scoreAyah(ayah, topic)
                    if (score >= QuranTopicCatalog.MIN_MATCH_SCORE) {
                        local[topic.slug] = (local[topic.slug] ?: 0) + 1
                    }
                }
            }
        }
        countMutex.withLock {
            countCache.putAll(local)
        }
        local
    }

    /** Memoized wrapper for the single-pass count map. */
    @Volatile
    private var allCountsMemo: Map<String, Int>? = null

    suspend fun allCounts(): Map<String, Int> {
        allCountsMemo?.let { return it }
        val counts = computeAllCounts()
        if (counts.isNotEmpty()) allCountsMemo = counts
        return counts
    }

    /** Invalidate caches (used by pull-to-refresh / after asset reload). */
    fun clearCache() {
        if (scoreMutex.tryLock()) {
            try { scoreCache.clear() } finally { scoreMutex.unlock() }
        }
        if (countMutex.tryLock()) {
            try {
                countCache.clear()
                allCountsMemo = null
            } finally { countMutex.unlock() }
        }
    }

    // ─── Scoring core ────────────────────────────────────────────────────

    /**
     * Score one ayah: +3 per Bangla keyword hit, +2 per Arabic keyword hit.
     * Mirrors HadithTopicRepository.scoreHadithForTopic() exactly.
     */
    private fun scoreAyah(ayah: Ayah, topic: QuranKeywordTopic): Pair<Int, List<String>> {
        var score = 0
        val matched = mutableListOf<String>()
        for (kw in topic.keywordsBn) {
            if (ayah.bengali.contains(kw)) {
                score += 3
                matched.add(kw)
            }
        }
        for (kw in topic.keywordsAr) {
            if (ayah.arabic.contains(kw)) {
                score += 2
                matched.add(kw)
            }
        }
        return score to matched
    }
}
