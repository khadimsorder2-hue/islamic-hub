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
    val tafsirSource: TafsirSource = TafsirSource.BN_BENGALI,
    val autoPause: AutoPauseOption = AutoPauseOption.OFF,
    val banglaAudioEnabled: Boolean = false,
    val wordByWordAudioEnabled: Boolean = true,
    val showArabic: Boolean = true,
    val showBangla: Boolean = true,
    val showEnglish: Boolean = true,
    val cacheSizeBytes: Long = 0L,
    val aiApiKey: String = "",
    val aiBaseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
    val aiModel: String = "gemini-2.5-flash",
    val aiProvider: String = "gemini",
    val firebaseEnabled: Boolean = false,
    /** Number of cached AI responses */
    val cacheCount: Int = 0
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        load()
        observeAICache()
    }

    private fun observeAICache() {
        viewModelScope.launch {
            container.aiCacheRepository.count.collect { count ->
                _state.value = _state.value.copy(cacheCount = count)
            }
        }
    }

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
            val aiKey = container.settingsRepository.aiApiKey.first()
            val aiUrl = container.settingsRepository.aiBaseUrl.first()
            val aiModel = container.settingsRepository.aiModel.first()
            val aiProvider = container.settingsRepository.aiProvider.first()
            val firebase = container.settingsRepository.firebaseEnabled.first()

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
                cacheSizeBytes = cacheSize,
                aiApiKey = aiKey,
                aiBaseUrl = aiUrl,
                aiModel = aiModel,
                aiProvider = aiProvider,
                firebaseEnabled = firebase
            )
        }
    }

    fun setAiApiKey(key: String) {
        viewModelScope.launch {
            container.settingsRepository.setAiApiKey(key)
            _state.value = _state.value.copy(aiApiKey = key)
            container.aiService.updateConfig(
                com.islamichub.app.data.repo.AIService.Config(
                    apiKey = key,
                    baseUrl = _state.value.aiBaseUrl,
                    model = _state.value.aiModel,
                    provider = _state.value.aiProvider
                )
            )
        }
    }
    fun setAiBaseUrl(url: String) {
        viewModelScope.launch {
            container.settingsRepository.setAiBaseUrl(url)
            _state.value = _state.value.copy(aiBaseUrl = url)
            container.aiService.updateConfig(
                com.islamichub.app.data.repo.AIService.Config(
                    apiKey = _state.value.aiApiKey,
                    baseUrl = url,
                    model = _state.value.aiModel,
                    provider = _state.value.aiProvider
                )
            )
        }
    }
    fun setAiModel(model: String) {
        viewModelScope.launch {
            container.settingsRepository.setAiModel(model)
            _state.value = _state.value.copy(aiModel = model)
            container.aiService.updateConfig(
                com.islamichub.app.data.repo.AIService.Config(
                    apiKey = _state.value.aiApiKey,
                    baseUrl = _state.value.aiBaseUrl,
                    model = model,
                    provider = _state.value.aiProvider
                )
            )
        }
    }
    fun setAiProvider(provider: String) {
        viewModelScope.launch {
            container.settingsRepository.setAiProvider(provider)
            // Auto-set defaults per provider
            val (defaultUrl, defaultModel) = when (provider) {
                "gemini" -> "https://generativelanguage.googleapis.com/v1beta" to "gemini-2.5-flash"
                "openrouter" -> "https://openrouter.ai/api/v1" to "stepfun/step-3.5-flash:free"
                else -> "https://api.openai.com/v1" to "gpt-4o-mini"
            }
            container.settingsRepository.setAiBaseUrl(defaultUrl)
            container.settingsRepository.setAiModel(defaultModel)
            _state.value = _state.value.copy(
                aiProvider = provider,
                aiBaseUrl = defaultUrl,
                aiModel = defaultModel
            )
            container.aiService.updateConfig(
                com.islamichub.app.data.repo.AIService.Config(
                    apiKey = _state.value.aiApiKey,
                    baseUrl = defaultUrl,
                    model = defaultModel,
                    provider = provider
                )
            )
        }
    }
    fun setFirebaseEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.setFirebaseEnabled(enabled)
            _state.value = _state.value.copy(firebaseEnabled = enabled)
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

    fun clearAICache() {
        viewModelScope.launch {
            container.settingsRepository.clearAICache()
        }
    }
}

