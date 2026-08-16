package com.islamichub.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.SurahSummary

data class QuranListUiState(
    val surahs: List<SurahSummary> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true
)

class QuranListViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QuranListUiState())
    val state: StateFlow<QuranListUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val list = container.quranRepository.listSurahs()
            _state.value = QuranListUiState(surahs = list, isLoading = false)
        }
    }

    fun onQueryChange(q: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(query = q, isLoading = true)
            val list = container.quranRepository.searchSurahs(q)
            _state.value = _state.value.copy(surahs = list, isLoading = false)
        }
    }
}
