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
    // v5.13.1 — hang-proof: never leave the list stuck on an empty spinner
    val loadFailed: Boolean = false,
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
            // v5.13.1 — HARDENED: timeout + full exception guard. Whatever
            // happens, the spinner always resolves to content or a retry state.
            try {
                val list = kotlinx.coroutines.withTimeoutOrNull(15_000L) {
                    container.quranRepository.listSurahs()
                } ?: emptyList()
                _state.value = _state.value.copy(
                    surahs = list,
                    isLoading = false,
                    loadFailed = list.isEmpty()
                )
            } catch (_: Exception) {
                _state.value = _state.value.copy(isLoading = false, loadFailed = true)
            }
        }
    }

    /** v5.13.1 — user-visible retry after a failed/stalled load. */
    fun retryLoad() {
        _state.value = _state.value.copy(isLoading = true, loadFailed = false)
        load()
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
