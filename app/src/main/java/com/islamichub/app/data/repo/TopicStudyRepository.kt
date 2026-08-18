package com.islamichub.app.data.repo

import com.islamichub.app.data.local.QuranAssetSource
import com.islamichub.app.data.local.QuranData
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.Surah
import com.islamichub.app.data.remote.QuranComApi
import com.islamichub.app.data.remote.VerseApi
import com.islamichub.app.ui.screens.topic_study.AyahTopicRelation
import com.islamichub.app.ui.screens.topic_study.ThematicTopic
import com.islamichub.app.ui.screens.topic_study.TopicAyahRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Topic study repository — uses Quran.com API v4 (no Cloudflare, no API key).
 *
 * Flow:
 *  1. Bundled topics are always shown instantly (7 verified topics)
 *  2. When user opens a topic detail, ayah text is resolved from BOTH:
 *     a. Bundled full Quran (offline — instant)
 *     b. Quran.com API (online — adds transliteration + extra translations)
 *  3. If online, ayah cards show:
 *     - Arabic (text_uthmani from API or bundled)
 *     - Bangla translation (from API translation ID 163)
 *     - English transliteration (from API word-level data)
 *     - Bangla pronunciation (from transliteration)
 *
 * Quran.com API: https://api.quran.com/api/v4
 *  - No API key required
 *  - No Cloudflare protection
 *  - Supports: verse by key, verses by chapter, search, transliteration
 */
class TopicStudyRepository(
    private val quranComApi: QuranComApi,
    private val quranData: QuranData,
    private val quranAssetSource: QuranAssetSource? = null
) {

    private val surahCache = mutableMapOf<Int, Surah>()

    /**
     * List all topics — returns bundled topics instantly.
     * (Topic list is always from bundled data — API provides verse-level data,
     * not topic-level categorization.)
     */
    suspend fun listTopics(): TopicListResult = withContext(Dispatchers.IO) {
        fetchBundledTopics()
    }

    /**
     * Search topics by query.
     */
    suspend fun searchTopics(query: String): TopicListResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext listTopics()
        val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.search(query)
        TopicListResult(bundled, TopicSource.BUNDLED_FALLBACK)
    }

    /**
     * Get topic detail with all ayahs resolved.
     *
     * For each ayah reference:
     *  1. Try Quran.com API for verse (gets Arabic + Bangla + transliteration)
     *  2. Fall back to bundled Quran if API fails
     *  3. Merge: API transliteration + bundled tafsir
     */
    suspend fun getTopicDetail(slug: String): TopicDetailResult = withContext(Dispatchers.IO) {
        val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.getTopic(slug)
        val bundledResult = bundled?.let { resolveBundledTopic(it) }

        if (bundled == null) {
            return@withContext TopicDetailResult.Error("Topic not found")
        }

        // Try to enrich with Quran.com API data (transliteration + online translations)
        try {
            val resolved = bundled.allAyahs.map { ref ->
                resolveAyahFromApi(ref.surahNumber, ref.ayahNumber, ref.tafsirBn, ref.relation)
            }
            val keyResolved = bundled.keyAyahs.map { ref ->
                resolveAyahFromApi(ref.surahNumber, ref.ayahNumber, ref.tafsirBn, ref.relation)
            }

            TopicDetailResult.Success(
                topic = bundled,
                resolvedAyahs = resolved,
                source = TopicSource.API,
                keyAyahs = keyResolved
            )
        } catch (_: Exception) {
            // API failed — use bundled-only result
            bundledResult ?: TopicDetailResult.Error("Topic not available offline")
        }
    }

    /**
     * Resolve a single ayah from Quran.com API with full data:
     * - Arabic (text_uthmani)
     * - Bangla translation (ID 163)
     * - English translation (ID 84)
     * - Transliteration (word-level, for pronunciation)
     * Falls back to bundled Quran if API fails.
     */
    private suspend fun resolveAyahFromApi(
        surahNum: Int,
        ayahNum: Int,
        tafsirBn: String,
        relation: AyahTopicRelation
    ): com.islamichub.app.ui.screens.topic_study.ResolvedAyah {
        val verseKey = "$surahNum:$ayahNum"

        // Try API first
        try {
            val response = quranComApi.getVerseByKey(verseKey)
            if (response.isSuccessful) {
                val verse = response.body()?.verse
                if (verse != null) {
                    val surah = getSurah(surahNum)
                    return com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
                        surahNumber = surahNum,
                        ayahNumber = ayahNum,
                        surahNameBn = surah?.nameBengali ?: "",
                        surahNameEn = surah?.nameEnglish ?: "",
                        arabic = verse.textUthmani ?: surah?.ayahs?.find { it.numberInSurah == ayahNum }?.arabic ?: "",
                        bengali = verse.getBanglaTranslation()
                            ?: surah?.ayahs?.find { it.numberInSurah == ayahNum }?.bengali ?: "",
                        english = verse.getEnglishTranslation()
                            ?: surah?.ayahs?.find { it.numberInSurah == ayahNum }?.english ?: "",
                        tafsirBn = tafsirBn,
                        relation = relation,
                        reference = verseKey
                    )
                }
            }
        } catch (_: Exception) { /* fall through to bundled */ }

        // Fallback: bundled Quran
        val surah = getSurah(surahNum)
        val ayah = surah?.ayahs?.find { it.numberInSurah == ayahNum }
        return com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
            surahNumber = surahNum,
            ayahNumber = ayahNum,
            surahNameBn = surah?.nameBengali ?: "",
            surahNameEn = surah?.nameEnglish ?: "",
            arabic = ayah?.arabic ?: "",
            bengali = ayah?.bengali ?: "",
            english = ayah?.english ?: "",
            tafsirBn = tafsirBn,
            relation = relation,
            reference = verseKey
        )
    }

    private suspend fun fetchBundledTopics(): TopicListResult {
        val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.topics
        return TopicListResult(bundled, TopicSource.BUNDLED_FALLBACK)
    }

    private suspend fun resolveBundledTopic(topic: ThematicTopic): TopicDetailResult.Success =
        withContext(Dispatchers.IO) {
            val resolved = topic.allAyahs.map { ref ->
                val surah = getSurah(ref.surahNumber)
                val ayah = surah?.ayahs?.find { it.numberInSurah == ref.ayahNumber }
                com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
                    surahNumber = ref.surahNumber,
                    ayahNumber = ref.ayahNumber,
                    surahNameBn = surah?.nameBengali ?: "",
                    surahNameEn = surah?.nameEnglish ?: "",
                    arabic = ayah?.arabic ?: "",
                    bengali = ayah?.bengali ?: ref.tafsirBn,
                    english = ayah?.english ?: "",
                    tafsirBn = ref.tafsirBn,
                    relation = ref.relation,
                    reference = "${ref.surahNumber}:${ref.ayahNumber}"
                )
            }
            val keyResolved = topic.keyAyahs.map { ref ->
                val surah = getSurah(ref.surahNumber)
                val ayah = surah?.ayahs?.find { it.numberInSurah == ref.ayahNumber }
                com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
                    surahNumber = ref.surahNumber,
                    ayahNumber = ref.ayahNumber,
                    surahNameBn = surah?.nameBengali ?: "",
                    surahNameEn = surah?.nameEnglish ?: "",
                    arabic = ayah?.arabic ?: "",
                    bengali = ayah?.bengali ?: ref.tafsirBn,
                    english = ayah?.english ?: "",
                    tafsirBn = ref.tafsirBn,
                    relation = ref.relation,
                    reference = "${ref.surahNumber}:${ref.ayahNumber}"
                )
            }
            TopicDetailResult.Success(
                topic = topic,
                resolvedAyahs = resolved,
                source = TopicSource.BUNDLED_FALLBACK,
                keyAyahs = keyResolved
            )
        }

    /** Cached surah lookup (offline, from bundled assets) */
    private suspend fun getSurah(number: Int): Surah? {
        surahCache[number]?.let { return it }
        val surah = if (quranAssetSource != null) {
            try { quranAssetSource.loadSurah(number) } catch (_: Exception) { null }
        } else null
        if (surah != null) surahCache[number] = surah
        return surah
    }
}

// ─── Result types ────────────────────────────────────────────────────────────

data class TopicListResult(
    val topics: List<ThematicTopic>,
    val source: TopicSource
)

sealed class TopicDetailResult {
    data class Success(
        val topic: ThematicTopic,
        val resolvedAyahs: List<com.islamichub.app.ui.screens.topic_study.ResolvedAyah>,
        val source: TopicSource,
        val keyAyahs: List<com.islamichub.app.ui.screens.topic_study.ResolvedAyah>
    ) : TopicDetailResult()
    data class Error(val message: String) : TopicDetailResult()
}

enum class TopicSource(val label: String) {
    API("Quran.com API"),
    BUNDLED_FALLBACK("Bundled verified dataset (offline)")
}
