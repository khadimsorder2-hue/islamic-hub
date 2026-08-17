package com.islamichub.app.data.repo

import com.islamichub.app.data.local.QuranAssetSource
import com.islamichub.app.data.local.QuranData
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.Surah
import com.islamichub.app.data.remote.IslamicAppApi
import com.islamichub.app.data.remote.TopicApi
import com.islamichub.app.data.remote.TopicAyahApi
import com.islamichub.app.data.remote.TopicDetailApi
import com.islamichub.app.ui.screens.topic_study.AyahTopicRelation
import com.islamichub.app.ui.screens.topic_study.ThematicTopic
import com.islamichub.app.ui.screens.topic_study.TopicAyahRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Topic study repository — API-first, bundled fallback.
 *
 * Flow:
 *  1. Try Islamic.app Topics API (338 topics, full Quran coverage)
 *  2. If offline / API fails → fall back to bundled verified topics
 *  3. Ayah text is ALWAYS resolved from bundled full Quran (offline-first,
 *     no extra API call needed, no client_secret required)
 *
 * Architecture follows the master plan:
 *  - Single-writer pattern via suspend functions
 *  - Stale response protection handled by ViewModel (requestId)
 *  - No race conditions: each suspend call is atomic
 *
 * License registry:
 *  - Islamic.app topics: CC BY 4.0 (attribution required)
 *  - Ayah text: bundled Quran (in-app)
 *  - Tafsir summaries: IslamicHub original Bangla (where bundled fallback used)
 */
class TopicStudyRepository(
    private val api: IslamicAppApi,
    private val quranData: QuranData,
    private val quranAssetSource: QuranAssetSource? = null
) {

    private val surahCache = mutableMapOf<Int, Surah>()

    /**
     * List all topics — tries API first, falls back to bundled.
     * Returns (topics, source) so UI can show attribution.
     */
    suspend fun listTopics(): TopicListResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getTopics(page = 1, perPage = 100, lang = "bn")
            if (response.isSuccessful) {
                val body = response.body()
                val apiTopics = body?.items ?: emptyList()
                // Fetch remaining pages if paginated
                val allTopics = if (apiTopics.isNotEmpty() && (body?.meta?.lastPage ?: 1) > 1) {
                    val mutable = apiTopics.toMutableList()
                    val lastPage = body?.meta?.lastPage ?: 1
                    for (p in 2..lastPage) {
                        try {
                            val r = api.getTopics(page = p, perPage = 100, lang = "bn")
                            if (r.isSuccessful) {
                                r.body()?.items?.let { mutable.addAll(it) }
                            }
                        } catch (_: Exception) { /* continue */ }
                    }
                    mutable
                } else apiTopics

                if (allTopics.isNotEmpty()) {
                    return@withContext TopicListResult(
                        topics = allTopics.map { it.toThematicTopic() },
                        source = TopicSource.API
                    )
                }
            }
            // API succeeded but empty → fall back
            fetchBundledTopics()
        } catch (_: Exception) {
            // Network failure → fall back to bundled
            fetchBundledTopics()
        }
    }

    /**
     * Search topics by query — tries API search first.
     */
    suspend fun searchTopics(query: String): TopicListResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext listTopics()
        try {
            val response = api.searchTopics(query, lang = "bn")
            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                if (items.isNotEmpty()) {
                    return@withContext TopicListResult(
                        topics = items.map { it.toThematicTopic() },
                        source = TopicSource.API
                    )
                }
            }
            // Fall back to bundled search
            val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.search(query)
            TopicListResult(bundled, TopicSource.BUNDLED_FALLBACK)
        } catch (_: Exception) {
            val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.search(query)
            TopicListResult(bundled, TopicSource.BUNDLED_FALLBACK)
        }
    }

    /**
     * Get topic detail with all ayahs resolved with full Quran text.
     *
     * Flow:
     *  1. Try API: get topic + ayah references (verse_keys)
     *  2. Resolve ayah text from bundled full Quran
     *  3. If API fails, use bundled verified topic data
     */
    suspend fun getTopicDetail(slug: String): TopicDetailResult = withContext(Dispatchers.IO) {
        // First try bundled (instant, always works, has rich Bangla tafsir)
        val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.getTopic(slug)
        val bundledResult = bundled?.let { resolveBundledTopic(it) }

        try {
            // Fetch all ayah pages
            val allAyahRefs = mutableListOf<TopicAyahApi>()
            var currentPage = 1
            var lastPage = 1
            var topicMeta: com.islamichub.app.data.remote.TopicDetailApi? = null
            do {
                val response = api.getTopic(slug, page = currentPage, perPage = 100, lang = "bn")
                if (!response.isSuccessful) break
                val body = response.body() ?: break
                if (topicMeta == null) topicMeta = body.topicData
                body.ayahList.let { allAyahRefs.addAll(it) }
                lastPage = body.meta?.lastPage ?: body.lastPage ?: 1
                currentPage++
            } while (currentPage <= lastPage && allAyahRefs.size < 1000) // safety cap

            if (allAyahRefs.isEmpty()) {
                return@withContext bundledResult ?: TopicDetailResult.Error("Topic not found")
            }

            // Resolve each ayah ref against bundled Quran
            val resolved = allAyahRefs.mapNotNull { apiAyah ->
                val ref = apiAyah.parseReference() ?: return@mapNotNull null
                val (surahNum, ayahNum) = ref
                val surah = getSurah(surahNum) ?: return@mapNotNull null
                val ayah: Ayah? = surah.ayahs.find { it.numberInSurah == ayahNum }

                com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
                    surahNumber = surahNum,
                    ayahNumber = ayahNum,
                    surahNameBn = surah.nameBengali,
                    surahNameEn = surah.nameEnglish,
                    arabic = ayah?.arabic ?: apiAyah.arabic ?: "",
                    bengali = ayah?.bengali ?: apiAyah.bangla ?: apiAyah.translation ?: "",
                    english = ayah?.english ?: apiAyah.english ?: "",
                    tafsirBn = apiAyah.tafsir ?: bundledTafsirFor(surahNum, ayahNum),
                    relation = if (apiAyah.isPrimary == true) AyahTopicRelation.PRIMARY
                               else AyahTopicRelation.THEMATIC,
                    reference = "$surahNum:$ayahNum"
                )
            }

            val thematic = ThematicTopic(
                slug = slug,
                nameBn = topicMeta?.nameBn ?: topicMeta?.name ?: bundled?.nameBn ?: slug,
                nameEn = topicMeta?.nameEn ?: topicMeta?.name ?: bundled?.nameEn ?: slug,
                nameAr = topicMeta?.nameAr ?: bundled?.nameAr ?: "",
                domain = topicMeta?.domainNameBn ?: topicMeta?.domainName ?: bundled?.domain ?: "",
                categoryBn = topicMeta?.categoryNameBn ?: bundled?.categoryBn ?: "",
                overviewBn = topicMeta?.descriptionBn ?: topicMeta?.description
                    ?: bundled?.overviewBn ?: "",
                keyAyahs = emptyList(),
                allAyahs = resolved.map {
                    TopicAyahRef(it.surahNumber, it.ayahNumber, it.tafsirBn, it.relation)
                },
                relatedTopics = emptyList(),
                relatedStories = bundled?.relatedStories ?: emptyList(),
                relatedConcepts = bundled?.relatedConcepts ?: emptyList(),
                accentColor = bundled?.accentColor ?: 0xFF6D45C7
            )

            TopicDetailResult.Success(
                topic = thematic,
                resolvedAyahs = resolved,
                source = TopicSource.API,
                keyAyahs = bundled?.keyAyahs?.map { ref ->
                    val surah = getSurah(ref.surahNumber)
                    com.islamichub.app.ui.screens.topic_study.ResolvedAyah(
                        surahNumber = ref.surahNumber,
                        ayahNumber = ref.ayahNumber,
                        surahNameBn = surah?.nameBengali ?: "",
                        surahNameEn = surah?.nameEnglish ?: "",
                        arabic = surah?.ayahs?.find { it.numberInSurah == ref.ayahNumber }?.arabic ?: "",
                        bengali = surah?.ayahs?.find { it.numberInSurah == ref.ayahNumber }?.bengali
                            ?: ref.tafsirBn,
                        english = surah?.ayahs?.find { it.numberInSurah == ref.ayahNumber }?.english ?: "",
                        tafsirBn = ref.tafsirBn,
                        relation = ref.relation,
                        reference = "${ref.surahNumber}:${ref.ayahNumber}"
                    )
                } ?: resolved.take(5)
            )
        } catch (_: Exception) {
            bundledResult ?: TopicDetailResult.Error("Topic not available offline")
        }
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

    /** Default tafsir when API doesn't provide one (Bangla) */
    private fun bundledTafsirFor(surah: Int, ayah: Int): String {
        val bundled = com.islamichub.app.ui.screens.topic_study.TopicStudyData.topics
        for (topic in bundled) {
            val match = topic.allAyahs.find { it.surahNumber == surah && it.ayahNumber == ayah }
            if (match != null) return match.tafsirBn
        }
        return "এই আয়াতের বিস্তারিত তাফসির এই বিষয়ের সাথে সম্পর্কিত। আরও জানতে সম্পূর্ণ তাফসির গ্রন্থ দেখুন।"
    }

    /** Convert API topic → ThematicTopic (without ayahs — loaded on demand) */
    private fun TopicApi.toThematicTopic(): ThematicTopic {
        val accentPalette = listOf(
            0xFF6D45C7, 0xFF1B5E20, 0xFFC9A34E, 0xFFD84315, 0xFF00ACC1,
            0xFFEF6C00, 0xFF8E24AA, 0xFF2E7D32, 0xFF3949AB, 0xFFFF6B35,
            0xFF00897B, 0xFF5C6BC0, 0xFF7E57C2, 0xFF8D6E63, 0xFF558B2F
        )
        val accent = accentPalette[(slug?.hashCode() ?: 0).let { if (it < 0) -it else it } % accentPalette.size]
        return ThematicTopic(
            slug = slug ?: "",
            nameBn = nameBn ?: name ?: nameEn ?: slug ?: "",
            nameEn = nameEn ?: name ?: slug ?: "",
            nameAr = nameAr ?: "",
            domain = domainNameBn ?: domainName ?: domainNameEn ?: "",
            categoryBn = categoryNameBn ?: "",
            overviewBn = descriptionBn ?: description ?: descriptionEn ?: "",
            keyAyahs = emptyList(),
            allAyahs = emptyList(),
            relatedTopics = emptyList(),
            relatedStories = emptyList(),
            relatedConcepts = emptyList(),
            accentColor = accent
        )
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
    API("Islamic.app Topics API"),
    BUNDLED_FALLBACK("Bundled verified dataset (offline)")
}
