package com.islamichub.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Surah
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.data.repo.Bookmark
import com.islamichub.app.data.repo.LastRead

data class QuranReaderUiState(
    val surah: Surah? = null,
    val isLoading: Boolean = true,
    val notAvailable: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val isLoadingAudio: Boolean = false,
    val currentPlayingAyah: Int? = null,
    val bookmarkedAyahs: Set<Int> = emptySet(),
    val quranFontScale: Float = 1.0f,
    val showArabic: Boolean = true,
    val showBangla: Boolean = true,
    val showEnglish: Boolean = true,
    val selectedReciterId: String = "ar.alafasy"
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
        observeSettings()
        observeBookmarks()
    }

    private fun load() {
        viewModelScope.launch {
            val surah = container.quranRepository.getSurah(surahNumber)
            _state.value = _state.value.copy(
                surah = surah,
                isLoading = false,
                notAvailable = surah == null
            )
            // Mark khatam progress and last read
            if (surah != null) {
                container.khatamRepository.markSurahCompleted(surahNumber, surah.ayahCount)
                container.lastReadRepository.set(
                    LastRead(
                        surahNumber = surahNumber,
                        ayahNumber = surah.ayahs.lastOrNull()?.numberInSurah ?: 1,
                        surahName = surah.nameEnglish,
                        surahNameBn = surah.nameBengali
                    )
                )
                container.trackerRepository.recordSurahRead()
            }
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

    private fun observeSettings() {
        viewModelScope.launch {
            container.settingsRepository.quranFontScale.collect { scale ->
                _state.value = _state.value.copy(quranFontScale = scale)
            }
        }
        viewModelScope.launch {
            container.settingsRepository.showArabic.collect { show ->
                _state.value = _state.value.copy(showArabic = show)
            }
        }
        viewModelScope.launch {
            container.settingsRepository.showBangla.collect { show ->
                _state.value = _state.value.copy(showBangla = show)
            }
        }
        viewModelScope.launch {
            container.settingsRepository.showEnglish.collect { show ->
                _state.value = _state.value.copy(showEnglish = show)
            }
        }
        viewModelScope.launch {
            container.settingsRepository.selectedReciter.collect { id ->
                _state.value = _state.value.copy(selectedReciterId = id)
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            container.bookmarkRepository.bookmarks.collect { list ->
                _state.value = _state.value.copy(
                    bookmarkedAyahs = list.filter { it.surahNumber == surahNumber }
                        .map { it.ayahNumber }.toSet()
                )
            }
        }
    }

    fun playSurah() {
        val reciter = AudioController.availableRecitersStatic.firstOrNull {
            it.editionId == _state.value.selectedReciterId
        } ?: AudioController.availableRecitersStatic.first()
        container.audioController.playSurah(surahNumber, reciter)
    }

    fun playAyah(ayahNumber: Int) {
        val reciter = AudioController.availableRecitersStatic.firstOrNull {
            it.editionId == _state.value.selectedReciterId
        } ?: AudioController.availableRecitersStatic.first()
        container.audioController.playAyah(surahNumber, ayahNumber, reciter)
        viewModelScope.launch {
            container.trackerRepository.recordAyahRead()
        }
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

    fun toggleBookmark(ayahNumber: Int) {
        val surah = _state.value.surah ?: return
        val ayah = surah.ayahs.firstOrNull { it.numberInSurah == ayahNumber } ?: return
        viewModelScope.launch {
            container.bookmarkRepository.toggle(
                Bookmark(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surah.nameEnglish,
                    surahNameBn = surah.nameBengali,
                    arabicSnippet = ayah.arabic.take(120)
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        val audioState = container.audioController.state.value
        if (audioState.currentSurah == surahNumber) {
            container.audioController.stop()
        }
    }
}
