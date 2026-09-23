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

    // v5.3.1 — keyword-driven thematic engine over the bundled full Quran.
    // Turns 7 curated topics into 45 topics, each fully loaded from all
    // 6,236 ayahs (offline) via +3 Bangla / +2 Arabic keyword scoring.
    private val thematicEngine = ThematicQuranEngine(quranAssetSource)

    /**
     * List all topics — curated (hand-written tafsir) first, then the
     * keyword-driven catalog (ayaths resolved on demand in getTopicDetail).
     */
    suspend fun listTopics(): TopicListResult = withContext(Dispatchers.IO) {
        val curated = com.islamichub.app.ui.screens.topic_study.TopicStudyData.topics
        val dynamic = QuranTopicCatalog.topics.map { it.toShellThematicTopic() }
        TopicListResult(curated + dynamic, TopicSource.ENGINE)
    }

    /** Ayah counts for every dynamic topic (single Quran scan, memoized). */
    suspend fun dynamicTopicCounts(): Map<String, Int> =
        thematicEngine.allCounts()

    /** True when the slug belongs to the keyword-driven catalog. */
    fun isDynamicTopic(slug: String): Boolean =
        QuranTopicCatalog.get(slug) != null

    /**
     * Find a topic anywhere (curated dataset OR dynamic catalog shell).
     * Shells have empty ayah lists — use getTopicDetail to resolve them.
     */
    fun findTopic(slug: String): com.islamichub.app.ui.screens.topic_study.ThematicTopic? {
        val curated = com.islamichub.app.ui.screens.topic_study.TopicStudyData.getTopic(slug)
        if (curated != null) return curated
        return QuranTopicCatalog.get(slug)?.toShellThematicTopic()
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
     * Curated topics (7): ayah refs are static, text resolved from
     * bundled Quran + enriched with Quran.com API when online.
     *
     * Dynamic catalog topics (v5.3.1): ThematicQuranEngine scores the
     * whole bundled Quran against the topic keywords and every matching
     * ayah is returned offline (key ayahs = top 5 by relevance; online
     * API enrichment is applied to the key ayahs only, best effort).
     */
    suspend fun getTopicDetail(slug: String): TopicDetailResult = withContext(Dispatchers.IO) {
        val curated = com.islamichub.app.ui.screens.topic_study.TopicStudyData.getTopic(slug)
        if (curated == null) {
            val dynamicTopic = QuranTopicCatalog.get(slug)
            if (dynamicTopic != null) {
                return@withContext getDynamicTopicDetail(dynamicTopic)
            }
            return@withContext TopicDetailResult.Error("Topic not found")
        }
        val bundledResult = resolveBundledTopic(curated)

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
     * Keyword-engine topic detail: score the full bundled Quran, build the
     * ThematicTopic (key = top-5 relevant), resolve text offline, then
     * best-effort enrich the key ayahs via Quran.com API.
     */
    private suspend fun getDynamicTopicDetail(
        topic: QuranKeywordTopic
    ): TopicDetailResult {
        val scored = thematicEngine.scoreTopic(topic)
        if (scored.isEmpty()) {
            return TopicDetailResult.Error(
                "এই বিষয়ের আয়াত অফলাইন কুরআন ডেটাসেট থেকে মেলানো যায়নি"
            )
        }

        val surahNames = surahNameMap()
        val thematic = com.islamichub.app.ui.screens.topic_study.ThematicTopic(
            slug = topic.slug,
            nameBn = topic.nameBn,
            nameEn = topic.nameEn,
            nameAr = topic.nameAr,
            domain = topic.domain,
            categoryBn = topic.categoryBn,
            overviewBn = topic.overviewBn,
            keyAyahs = scored.take(KEY_AYAH_COUNT).map { it.toTopicAyahRef() },
            allAyahs = scored.map { it.toTopicAyahRef() },
            relatedTopics = emptyList(),
            relatedStories = emptyList(),
            relatedConcepts = topic.relatedConcepts,
            accentColor = topic.accentColor
        )

        val resolvedAll = scored.map { s ->
            com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
                surahNumber = s.surahNumber,
                ayahNumber = s.ayahNumber,
                surahNameBn = surahNames[s.surahNumber]?.first ?: "",
                surahNameEn = surahNames[s.surahNumber]?.second ?: "",
                arabic = s.arabic,
                bengali = s.bengali,
                english = s.english,
                tafsirBn = s.matchedKeywordsNote(),
                relation = com.islamichub.app.ui.screens.topic_study.AyahTopicRelation.THEMATIC,
                reference = "${s.surahNumber}:${s.ayahNumber}"
            )
        }
        val resolvedKey = resolvedAll.take(KEY_AYAH_COUNT).map { it }

        // Best-effort: refresh the key ayahs from the online API
        val enrichedKey = try {
            resolvedKey.map { r ->
                val parts = r.reference.split(":")
                val s = parts.getOrNull(0)?.toIntOrNull()
                val a = parts.getOrNull(1)?.toIntOrNull()
                if (s != null && a != null) {
                    resolveAyahFromApi(s, a, r.tafsirBn, r.relation)
                } else r
            }
        } catch (_: Exception) {
            resolvedKey
        }

        return TopicDetailResult.Success(
            topic = thematic,
            resolvedAyahs = resolvedAll,
            source = TopicSource.ENGINE,
            keyAyahs = enrichedKey
        )
    }

    private fun ThematicQuranEngine.ScoredAyah.toTopicAyahRef():
        com.islamichub.app.ui.screens.topic_study.TopicAyahRef =
        com.islamichub.app.ui.screens.topic_study.TopicAyahRef(
            surahNumber = surahNumber,
            ayahNumber = ayahNumber,
            tafsirBn = matchedKeywordsNote(),
            relation = com.islamichub.app.ui.screens.topic_study.AyahTopicRelation.THEMATIC
        )

    /** Human-readable note of which keywords matched + relevance score. */
    private fun ThematicQuranEngine.ScoredAyah.matchedKeywordsNote(): String {
        val kws = matchedKeywords.take(4).joinToString(" • ")
        return "কীওয়ার্ড মিল: $kws  (গুরুত্ব স্কোর $relevanceScore)\n" +
            "থিম ইঞ্জিন সম্পূর্ণ কুরআনের ৬,২৩৬ আয়াতের মধ্যে কীওয়ার্ড মিলিয়ে এই আয়াতটি বেছে নিয়েছে।"
    }

    /** surahNumber → (Bangla name, English name) map, built once. */
    private var surahNameMemo: Map<Int, Pair<String, String>>? = null
    private suspend fun surahNameMap(): Map<Int, Pair<String, String>> {
        surahNameMemo?.let { return it }
        val meta = try { quranAssetSource?.loadMeta() } catch (_: Exception) { null }
        val map = meta?.associate { it.number to (it.nameBengali to it.nameEnglish) } ?: emptyMap()
        surahNameMemo = map
        return map
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

    companion object {
        /** How many top-relevant ayahs are highlighted as "key" in dynamic topics */
        const val KEY_AYAH_COUNT = 5
    }
}

// ─── Dynamic-topic shell conversion ────────────────────────────────────────

/**
 * Catalog topic → ThematicTopic shell (empty ayah lists; text is resolved
 * on demand by ThematicQuranEngine when the user opens the topic).
 */
fun QuranKeywordTopic.toShellThematicTopic():
    com.islamichub.app.ui.screens.topic_study.ThematicTopic =
    com.islamichub.app.ui.screens.topic_study.ThematicTopic(
        slug = slug,
        nameBn = nameBn,
        nameEn = nameEn,
        nameAr = nameAr,
        domain = domain,
        categoryBn = categoryBn,
        overviewBn = overviewBn,
        keyAyahs = emptyList(),
        allAyahs = emptyList(),
        relatedTopics = emptyList(),
        relatedStories = emptyList(),
        relatedConcepts = relatedConcepts,
        accentColor = accentColor
    )

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
    BUNDLED_FALLBACK("Bundled verified dataset (offline)"),
    ENGINE("Keyword Engine — full Quran, offline")
}
