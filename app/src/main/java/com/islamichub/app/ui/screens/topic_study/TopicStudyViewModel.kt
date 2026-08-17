package com.islamichub.app.ui.screens.topic_study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A single ayah resolved with full Quran text + topic context */
data class ResolvedAyah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameBn: String,
    val surahNameEn: String,
    val arabic: String,
    val bengali: String,
    val english: String,
    val tafsirBn: String,
    val relation: AyahTopicRelation,
    val reference: String                  // "3:133"
)

data class TopicDetailUiState(
    val topic: ThematicTopic? = null,
    val resolvedKeyAyahs: List<ResolvedAyah> = emptyList(),
    val resolvedAllAyahs: List<ResolvedAyah> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val expandedAyahRef: String? = null,    // "3:133"
    val searchQuery: String = "",
    val filteredTopics: List<ThematicTopic> = emptyList()
)

data class TopicListUiState(
    val topics: List<ThematicTopic> = emptyList(),
    val searchQuery: String = "",
    val filteredTopics: List<ThematicTopic> = emptyList(),
    val domains: List<String> = emptyList(),
    val selectedDomain: String? = null
)

class TopicListViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicListUiState())
    val uiState: StateFlow<TopicListUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                topics = TopicStudyData.topics,
                filteredTopics = TopicStudyData.topics,
                domains = TopicStudyData.domains()
            )
        }
    }

    fun updateSearch(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredTopics = if (query.isBlank()) {
                    if (it.selectedDomain == null) it.topics
                    else it.topics.filter { t -> t.domain == it.selectedDomain }
                } else {
                    TopicStudyData.search(query).filter { t ->
                        it.selectedDomain == null || t.domain == it.selectedDomain
                    }
                }
            )
        }
    }

    fun selectDomain(domain: String?) {
        _uiState.update {
            it.copy(
                selectedDomain = domain,
                filteredTopics = if (domain == null) {
                    if (it.searchQuery.isBlank()) it.topics
                    else TopicStudyData.search(it.searchQuery)
                } else {
                    TopicStudyData.byDomain(domain).filter { t ->
                        it.searchQuery.isBlank() ||
                            t.nameBn.contains(it.searchQuery) ||
                            t.nameEn.lowercase().contains(it.searchQuery.lowercase())
                    }
                }
            )
        }
    }
}

class TopicDetailViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicDetailUiState())
    val uiState: StateFlow<TopicDetailUiState> = _uiState.asStateFlow()

    fun load(slug: String) {
        viewModelScope.launch {
            val topic = TopicStudyData.getTopic(slug)
            if (topic == null) {
                _uiState.update { it.copy(isLoading = false, error = "Topic not found") }
                return@launch
            }
            try {
                val resolvedKey = resolveAyahs(topic.keyAyahs)
                val resolvedAll = resolveAyahs(topic.allAyahs)
                _uiState.update {
                    it.copy(
                        topic = topic,
                        resolvedKeyAyahs = resolvedKey,
                        resolvedAllAyahs = resolvedAll,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        topic = topic,
                        isLoading = false,
                        error = "Failed to load ayahs: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleAyahExpand(reference: String) {
        _uiState.update {
            it.copy(expandedAyahRef = if (it.expandedAyahRef == reference) null else reference)
        }
    }

    /**
     * Resolve ayah references by fetching actual Surah text from QuranRepository.
     * Groups by surah to minimize asset loads.
     */
    private suspend fun resolveAyahs(refs: List<TopicAyahRef>): List<ResolvedAyah> = withContext(Dispatchers.IO) {
        if (refs.isEmpty()) return@withContext emptyList()
        // group by surah
        val bySurah = refs.groupBy { it.surahNumber }
        val resolved = mutableListOf<ResolvedAyah>()
        for ((surahNum, refsForSurah) in bySurah) {
            val surah: Surah? = container.quranRepository.getSurah(surahNum)
            if (surah == null) {
                // fallback — use tafsir as Bengali, leave Arabic empty
                for (ref in refsForSurah) {
                    resolved.add(
                        ResolvedAyah(
                            surahNumber = surahNum,
                            ayahNumber = ref.ayahNumber,
                            surahNameBn = "",
                            surahNameEn = "",
                            arabic = "",
                            bengali = ref.tafsirBn,
                            english = "",
                            tafsirBn = ref.tafsirBn,
                            relation = ref.relation,
                            reference = "${ref.surahNumber}:${ref.ayahNumber}"
                        )
                    )
                }
                continue
            }
            for (ref in refsForSurah) {
                val ayah: Ayah? = surah.ayahs.find { it.numberInSurah == ref.ayahNumber }
                resolved.add(
                    ResolvedAyah(
                        surahNumber = surahNum,
                        ayahNumber = ref.ayahNumber,
                        surahNameBn = surah.nameBengali,
                        surahNameEn = surah.nameEnglish,
                        arabic = ayah?.arabic ?: "",
                        bengali = ayah?.bengali ?: ref.tafsirBn,
                        english = ayah?.english ?: "",
                        tafsirBn = ref.tafsirBn,
                        relation = ref.relation,
                        reference = "${ref.surahNumber}:${ref.ayahNumber}"
                    )
                )
            }
        }
        // preserve original order (sorted by surah, then ayah)
        resolved.sortBy { it.surahNumber * 10000 + it.ayahNumber }
        resolved
    }
}
