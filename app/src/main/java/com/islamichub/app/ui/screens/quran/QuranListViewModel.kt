package com.islamichub.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.data.repo.SurahSummary

data class QuranListUiState(
    val surahs: List<SurahSummary> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val progressMap: Map<Int, Float> = emptyMap()
)

class QuranListViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QuranListUiState())
    val state: StateFlow<QuranListUiState> = _state.asStateFlow()

    init {
        load()
        loadProgress()
    }

    private fun load() {
        viewModelScope.launch {
            val list = container.quranRepository.listSurahs()
            _state.value = _state.value.copy(surahs = list, isLoading = false)
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            container.khatamRepository.currentKhatam.collect { khatam ->
                if (khatam == null) return@collect
                val map = mutableMapOf<Int, Float>()
                _state.value.surahs.forEach { surah ->
                    val completed = khatam.completedAyahs[surah.number]
                    val ayahsRead = if (completed != null) (completed.last - completed.first + 1) else 0
                    map[surah.number] = if (surah.ayahCount > 0) ayahsRead.toFloat() / surah.ayahCount else 0f
                }
                _state.value = _state.value.copy(progressMap = map)
            }
        }
    }

    fun onQueryChange(q: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(query = q, isLoading = true)
            val list = container.quranRepository.searchSurahs(q)
            _state.value = _state.value.copy(surahs = list, isLoading = false)
        }
    }

    fun playSurah(surahNumber: Int) {
        val reciter = AudioController.availableRecitersStatic.firstOrNull {
            it.editionId == _state.value.surahs.firstOrNull()?.let { null } // use default
        } ?: AudioController.availableRecitersStatic.first()
        container.audioController.playSurah(surahNumber, reciter)
    }
}
