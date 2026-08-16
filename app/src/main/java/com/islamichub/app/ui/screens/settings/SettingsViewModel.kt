package com.islamichub.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AutoPauseOption
import com.islamichub.app.data.repo.BackgroundMode
import com.islamichub.app.data.repo.TafsirSource

data class SettingsUiState(
    val quranFontScale: Float = 1.0f,
    val backgroundMode: BackgroundMode = BackgroundMode.CREAM,
    val selectedReciter: String = "ar.alafasy",
    val tafsirSource: TafsirSource = TafsirSource.BN_MUKHTASAR,
    val autoPause: AutoPauseOption = AutoPauseOption.OFF,
    val banglaAudioEnabled: Boolean = false,
    val wordByWordAudioEnabled: Boolean = true,
    val showArabic: Boolean = true,
    val showBangla: Boolean = true,
    val showEnglish: Boolean = true,
    val cacheSizeBytes: Long = 0L
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val fontScale = container.settingsRepository.quranFontScale.first()
            val bgMode = container.settingsRepository.backgroundMode.first()
            val reciter = container.settingsRepository.selectedReciter.first()
            val tafsirSource = container.settingsRepository.tafsirSource.first()
            val autoPause = container.settingsRepository.autoPauseMinutes.first()
            val bnAudio = container.settingsRepository.banglaAudioEnabled.first()
            val wordAudio = container.settingsRepository.wordByWordAudioEnabled.first()
            val showAr = container.settingsRepository.showArabic.first()
            val showBn = container.settingsRepository.showBangla.first()
            val showEn = container.settingsRepository.showEnglish.first()
            val cacheSize = container.tafsirRepository.cacheSizeBytes()

            _state.value = SettingsUiState(
                quranFontScale = fontScale,
                backgroundMode = bgMode,
                selectedReciter = reciter,
                tafsirSource = tafsirSource,
                autoPause = autoPause,
                banglaAudioEnabled = bnAudio,
                wordByWordAudioEnabled = wordAudio,
                showArabic = showAr,
                showBangla = showBn,
                showEnglish = showEn,
                cacheSizeBytes = cacheSize
            )
        }
    }

    fun setQuranFontScale(scale: Float) {
        viewModelScope.launch {
            container.settingsRepository.setQuranFontScale(scale)
            _state.value = _state.value.copy(quranFontScale = scale)
        }
    }
    fun setBackgroundMode(mode: BackgroundMode) {
        viewModelScope.launch {
            container.settingsRepository.setBackgroundMode(mode)
            _state.value = _state.value.copy(backgroundMode = mode)
        }
    }
    fun setSelectedReciter(id: String) {
        viewModelScope.launch {
            container.settingsRepository.setSelectedReciter(id)
            _state.value = _state.value.copy(selectedReciter = id)
        }
    }
    fun setTafsirSource(source: TafsirSource) {
        viewModelScope.launch {
            container.settingsRepository.setTafsirSource(source)
            _state.value = _state.value.copy(tafsirSource = source)
        }
    }
    fun setAutoPause(option: AutoPauseOption) {
        viewModelScope.launch {
            container.settingsRepository.setAutoPause(option)
            container.audioController.setAutoPause(option)
            _state.value = _state.value.copy(autoPause = option)
        }
    }
    fun setBanglaAudioEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.setBanglaAudioEnabled(enabled)
            container.audioController.setBanglaAudioEnabled(enabled)
            _state.value = _state.value.copy(banglaAudioEnabled = enabled)
        }
    }
    fun setWordByWordAudioEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.setWordByWordAudioEnabled(enabled)
            _state.value = _state.value.copy(wordByWordAudioEnabled = enabled)
        }
    }
    fun setShowArabic(show: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.setShowArabic(show)
            _state.value = _state.value.copy(showArabic = show)
        }
    }
    fun setShowBangla(show: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.setShowBangla(show)
            _state.value = _state.value.copy(showBangla = show)
        }
    }
    fun setShowEnglish(show: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.setShowEnglish(show)
            _state.value = _state.value.copy(showEnglish = show)
        }
    }
    fun clearCache() {
        viewModelScope.launch {
            container.tafsirRepository.clearCache()
            container.settingsRepository.clearCache()
            val newSize = container.tafsirRepository.cacheSizeBytes()
            _state.value = _state.value.copy(cacheSizeBytes = newSize)
        }
    }
}

