package com.islamichub.app.ui.screens.hadith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.HadithCollectionMeta
import com.islamichub.app.data.local.HadithJson

data class HadithListUiState(
    val collections: List<HadithCollectionMeta> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HadithListViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(HadithListUiState())
    val state: StateFlow<HadithListUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val list = container.hadithRepository.listCollections()
                _state.value = HadithListUiState(collections = list, isLoading = false)
            } catch (e: Exception) {
                _state.value = HadithListUiState(
                    isLoading = false,
                    error = e.message ?: "সংগ্রহ লোড করতে ব্যর্থ"
                )
            }
        }
    }
}

data class HadithCollectionUiState(
    val hadiths: List<HadithJson> = emptyList(),
    val isLoading: Boolean = true,
    val collectionName: String = "",
    val error: String? = null
)

class HadithCollectionViewModel(
    private val container: AppContainer,
    private val collectionId: String
) : ViewModel() {
    private val _state = MutableStateFlow(HadithCollectionUiState())
    val state: StateFlow<HadithCollectionUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val coll = container.hadithRepository.getCollection(collectionId)
                _state.value = HadithCollectionUiState(
                    hadiths = coll.hadiths,
                    isLoading = false,
                    collectionName = coll.collectionNameBn.ifBlank { coll.collectionName }
                )
            } catch (e: Exception) {
                _state.value = HadithCollectionUiState(
                    isLoading = false,
                    error = e.message ?: "হাদিস লোড করতে ব্যর্থ"
                )
            }
        }
    }
}

data class HadithSearchUiState(
    val query: String = "",
    val results: List<com.islamichub.app.data.repo.HadithSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

class HadithSearchViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(HadithSearchUiState())
    val state: StateFlow<HadithSearchUiState> = _state.asStateFlow()

    fun onQueryChange(q: String) {
        _state.value = _state.value.copy(query = q)
        if (q.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), hasSearched = false)
            return
        }
        _state.value = _state.value.copy(isSearching = true, hasSearched = true)
        viewModelScope.launch {
            try {
                val results = container.hadithRepository.searchAll(q)
                _state.value = _state.value.copy(results = results, isSearching = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSearching = false)
            }
        }
    }
}
