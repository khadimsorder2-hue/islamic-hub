package com.islamichub.app.ui.screens.tafsir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AIService
import com.islamichub.app.data.repo.TafsirSource

data class TafsirUiState(
    val isLoading: Boolean = true,
    val tafsirText: String? = null,
    val aiExplanation: String? = null,
    val isAILoading: Boolean = false,
    val source: TafsirSource = TafsirSource.BN_BENGALI,
    val isCached: Boolean = false,
    val error: String? = null,
    val arabicText: String = "",
    val banglaText: String = "",
    val englishText: String = "",
    val surahName: String = "",
    val ayahNumber: Int = 0,
    val surahNumber: Int = 0
)

class TafsirViewModel(
    private val container: AppContainer,
    private val surah: Int,
    private val ayah: Int
) : ViewModel() {
    private val _state = MutableStateFlow(TafsirUiState())
    val state: StateFlow<TafsirUiState> = _state.asStateFlow()

    init {
        loadAyahInfo()
        loadTafsir()
        loadAIExplanation()
    }

    private fun loadAyahInfo() {
        viewModelScope.launch {
            try {
                val surahData = container.quranRepository.getSurah(surah)
                if (surahData != null) {
                    val ayahData = surahData.ayahs.firstOrNull { it.numberInSurah == ayah }
                    if (ayahData != null) {
                        _state.value = _state.value.copy(
                            arabicText = ayahData.arabic,
                            banglaText = ayahData.bengali,
                            englishText = ayahData.english,
                            surahName = surahData.nameEnglish,
                            surahNumber = surah,
                            ayahNumber = ayah
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadTafsir() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val source = container.settingsRepository.tafsirSource.first()
            try {
                val tafsir = container.tafsirRepository.getTafsir(surah, ayah, source.editionId)
                if (tafsir != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        tafsirText = tafsir,
                        source = source,
                        isCached = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        source = source
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message,
                    source = source
                )
            }
        }
    }

    /**
     * AI explanation — like a village khotib explaining the ayah from zero.
     * Uses AYAH_TAFSIR_PROMPT which instructs AI to:
     * - Explain in simple village language
     * - Include revelation reason (asbab al-nuzul)
     * - Give real-life examples
     * - Include Bangla pronunciation
     */
    private fun loadAIExplanation() {
        viewModelScope.launch {
            val apiKey = container.settingsRepository.aiApiKey.first()
            if (apiKey.isBlank()) return@launch

            _state.value = _state.value.copy(isAILoading = true)
            try {
                val surahData = container.quranRepository.getSurah(surah)
                val ayahData = surahData?.ayahs?.firstOrNull { it.numberInSurah == ayah }
                val arabic = ayahData?.arabic ?: ""
                val bangla = ayahData?.bengali ?: ""

                val prompt = """
সূরা: ${surahData?.nameEnglish ?: ""} (আয়াত $ayah)

আরবি: $arabic

বাংলা অনুবাদ: $bangla

এই আয়াতের সম্পূর্ণ তাফসীর দিন। একজন গ্রামের খতিব যেভাবে সাধারণ মানুষকে শূন্য থেকে বোঝান, সেভাবে বোঝান। উদাহরণ দিয়ে প্রতিটি বিষয় সহজ করে ব্যাখ্যা করুন। নাজিলের কারণ অবশ্যই উল্লেখ করবেন।
""".trimIndent()

                val result = container.aiService.ask(prompt, cacheType = "tafsir")
                if (result.error == null) {
                    _state.value = _state.value.copy(
                        isAILoading = false,
                        aiExplanation = result.answer
                    )
                } else {
                    _state.value = _state.value.copy(isAILoading = false)
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(isAILoading = false)
            }
        }
    }

    fun changeSource(source: TafsirSource) {
        viewModelScope.launch {
            container.settingsRepository.setTafsirSource(source)
            loadTafsir()
        }
    }
}
