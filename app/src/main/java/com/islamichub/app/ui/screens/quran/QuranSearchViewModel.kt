package com.islamichub.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AyahSearchResult

data class QuranSearchUiState(
    val query: String = "",
    val results: List<AyahSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

class QuranSearchViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QuranSearchUiState())
    val state: StateFlow<QuranSearchUiState> = _state.asStateFlow()

    fun onQueryChange(q: String) {
        _state.value = _state.value.copy(query = q)
        if (q.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), hasSearched = false)
            return
        }
        _state.value = _state.value.copy(isSearching = true, hasSearched = true)
        viewModelScope.launch {
            val results = container.quranRepository.searchAyahs(q, limit = 100)
            _state.value = _state.value.copy(results = results, isSearching = false)
        }
    }
}
