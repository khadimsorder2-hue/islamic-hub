package com.islamichub.app.ui.screens.topic_study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.TopicDetailResult
import com.islamichub.app.data.repo.TopicListResult
import com.islamichub.app.data.repo.TopicSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val source: TopicSource? = null,
    val searchQuery: String = "",
    val filteredTopics: List<ThematicTopic> = emptyList()
)

data class TopicListUiState(
    val topics: List<ThematicTopic> = emptyList(),
    val searchQuery: String = "",
    val filteredTopics: List<ThematicTopic> = emptyList(),
    val domains: List<String> = emptyList(),
    val selectedDomain: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val source: TopicSource? = null
)

class TopicListViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicListUiState())
    val uiState: StateFlow<TopicListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var currentRequestId: Int = 0

    init { loadTopics() }

    fun loadTopics() {
        // Stale response protection: cancel previous load, increment request ID
        loadJob?.cancel()
        val requestId = ++currentRequestId
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result: TopicListResult = container.topicStudyRepository.listTopics()
                // Only apply if this is still the latest request
                if (requestId != currentRequestId) return@launch
                val domains = result.topics.map { it.domain }.distinct().filter { it.isNotBlank() }
                _uiState.update {
                    it.copy(
                        topics = result.topics,
                        filteredTopics = result.topics,
                        domains = domains,
                        isLoading = false,
                        source = result.source
                    )
                }
            } catch (e: Exception) {
                if (requestId != currentRequestId) return@launch
                _uiState.update {
                    it.copy(isLoading = false, error = "লোড করতে ব্যর্থ: ${e.message}")
                }
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredTopics = filterTopics(it.topics, query, it.selectedDomain)
            )
        }
    }

    fun selectDomain(domain: String?) {
        _uiState.update {
            it.copy(
                selectedDomain = domain,
                filteredTopics = filterTopics(it.topics, it.searchQuery, domain)
            )
        }
    }

    private fun filterTopics(
        topics: List<ThematicTopic>,
        query: String,
        domain: String?
    ): List<ThematicTopic> {
        var filtered = topics
        if (domain != null) filtered = filtered.filter { it.domain == domain }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.nameBn.contains(query) || it.nameEn.lowercase().contains(q) ||
                    it.nameAr.contains(query) || it.categoryBn.contains(query)
            }
        }
        return filtered
    }
}

class TopicDetailViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicDetailUiState())
    val uiState: StateFlow<TopicDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var currentRequestId: Int = 0

    fun load(slug: String) {
        // Stale response protection
        loadJob?.cancel()
        val requestId = ++currentRequestId
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, expandedAyahRef = null) }
            try {
                val result = container.topicStudyRepository.getTopicDetail(slug)
                if (requestId != currentRequestId) return@launch
                when (result) {
                    is TopicDetailResult.Success -> {
                        _uiState.update {
                            it.copy(
                                topic = result.topic,
                                resolvedKeyAyahs = result.keyAyahs,
                                resolvedAllAyahs = result.resolvedAyahs,
                                isLoading = false,
                                error = null,
                                source = result.source
                            )
                        }
                    }
                    is TopicDetailResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                if (requestId != currentRequestId) return@launch
                _uiState.update {
                    it.copy(isLoading = false, error = "লোড করতে ব্যর্থ: ${e.message}")
                }
            }
        }
    }

    fun toggleAyahExpand(reference: String) {
        _uiState.update {
            it.copy(expandedAyahRef = if (it.expandedAyahRef == reference) null else reference)
        }
    }
}
