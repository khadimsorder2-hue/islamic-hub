package com.islamichub.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Surah

data class QuranReaderUiState(
    val surah: Surah? = null,
    val isLoading: Boolean = true,
    val notAvailable: Boolean = false
)

class QuranReaderViewModel(
    private val container: AppContainer,
    private val surahNumber: Int
) : ViewModel() {
    private val _state = MutableStateFlow(QuranReaderUiState())
    val state: StateFlow<QuranReaderUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val surah = container.quranRepository.getSurah(surahNumber)
            _state.value = QuranReaderUiState(
                surah = surah,
                isLoading = false,
                notAvailable = surah == null
            )
        }
    }
}
