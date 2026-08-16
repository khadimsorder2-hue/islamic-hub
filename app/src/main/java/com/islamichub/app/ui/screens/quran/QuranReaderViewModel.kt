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
    val notAvailable: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val isLoadingAudio: Boolean = false,
    val currentPlayingAyah: Int? = null
)

class QuranReaderViewModel(
    private val container: AppContainer,
    private val surahNumber: Int
) : ViewModel() {
    private val _state = MutableStateFlow(QuranReaderUiState())
    val state: StateFlow<QuranReaderUiState> = _state.asStateFlow()

    init {
        load()
        observeAudio()
    }

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

    private fun observeAudio() {
        viewModelScope.launch {
            container.audioController.state.collect { audioState ->
                _state.value = _state.value.copy(
                    isPlayingAudio = audioState.isPlaying,
                    isLoadingAudio = audioState.isLoading,
                    currentPlayingAyah = if (audioState.currentSurah == surahNumber)
                        audioState.currentAyah else null
                )
            }
        }
    }

    fun playSurah() {
        container.audioController.playSurah(surahNumber)
    }

    fun playAyah(ayahNumber: Int) {
        container.audioController.playAyah(surahNumber, ayahNumber)
    }

    fun toggleAudio() {
        if (_state.value.isPlayingAudio) {
            container.audioController.pause()
        } else {
            container.audioController.resume()
        }
    }

    fun stopAudio() {
        container.audioController.stop()
    }

    override fun onCleared() {
        super.onCleared()
        // Stop audio when leaving the reader to release Media3 resources
        // (but only if this VM owns the current playback)
        val audioState = container.audioController.state.value
        if (audioState.currentSurah == surahNumber) {
            container.audioController.stop()
        }
    }
}
