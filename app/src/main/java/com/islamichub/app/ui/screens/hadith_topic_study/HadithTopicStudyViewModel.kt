package com.islamichub.app.ui.screens.hadith_topic_study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.HadithTopic
import com.islamichub.app.data.repo.HadithTopicCatalog
import com.islamichub.app.data.repo.HadithTopicDetailResult
import com.islamichub.app.data.repo.HadithTopicEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HadithTopicListUiState(
    val topics: List<HadithTopic> = HadithTopicCatalog.topics,
    val searchQuery: String = "",
    val filteredTopics: List<HadithTopic> = HadithTopicCatalog.topics,
    val domains: List<String> = HadithTopicCatalog.topics.map { it.domain }.distinct(),
    val selectedDomain: String? = null
)

class HadithTopicListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HadithTopicListUiState())
    val uiState: StateFlow<HadithTopicListUiState> = _uiState.asStateFlow()

    fun updateSearch(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredTopics = filter(it.topics, query, it.selectedDomain)
            )
        }
    }

    fun selectDomain(domain: String?) {
        _uiState.update {
            it.copy(
                selectedDomain = domain,
                filteredTopics = filter(it.topics, it.searchQuery, domain)
            )
        }
    }

    private fun filter(topics: List<HadithTopic>, query: String, domain: String?): List<HadithTopic> {
        var filtered = topics
        if (domain != null) filtered = filtered.filter { it.domain == domain }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.nameBn.contains(query) || it.nameEn.lowercase().contains(query.lowercase()) ||
                    it.nameAr.contains(query) || it.categoryBn.contains(query) || it.domain.contains(query)
            }
        }
        return filtered
    }
}

data class HadithTopicDetailUiState(
    val topic: HadithTopic? = null,
    val hadiths: List<HadithTopicEntry> = emptyList(),
    val totalCount: Int = 0,
    val byCollection: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val expandedHadithRef: String? = null
)

class HadithTopicDetailViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(HadithTopicDetailUiState())
    val uiState: StateFlow<HadithTopicDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var currentRequestId: Int = 0

    fun load(slug: String) {
        loadJob?.cancel()
        val requestId = ++currentRequestId
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, expandedHadithRef = null) }
            try {
                val topic = HadithTopicCatalog.get(slug)
                if (topic == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Topic not found") }
                    return@launch
                }
                val result: HadithTopicDetailResult = container.hadithTopicRepository.getTopicHadiths(topic)
                if (requestId != currentRequestId) return@launch
                _uiState.update {
                    it.copy(
                        topic = result.topic,
                        hadiths = result.hadiths,
                        totalCount = result.totalCount,
                        byCollection = result.byCollection,
                        isLoading = false,
                        error = null
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

    fun toggleHadithExpand(ref: String) {
        _uiState.update {
            it.copy(expandedHadithRef = if (it.expandedHadithRef == ref) null else ref)
        }
    }
}
