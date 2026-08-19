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

data class BanglaTranslationOption(
    val name: String,
    val text: String
)

data class BanglaTafsirOption(
    val name: String,
    val text: String
)

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
    val surahNumber: Int = 0,
    // Quran.com API online data
    val onlineBanglaTranslations: List<BanglaTranslationOption> = emptyList(),
    val onlineBanglaTafsirs: List<BanglaTafsirOption> = emptyList(),
    val transliteration: String? = null,
    val isOnlineDataLoaded: Boolean = false,
    // Currently selected translation index
    val selectedTranslationIndex: Int = 0,
    val selectedTafsirIndex: Int = 0
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
        loadOnlineVerseData()
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

    /**
     * Load verse data from Quran.com API — includes:
     * - Multiple Bangla translations (Mujibur Rahman, Taisirul, Zakaria)
     * - Multiple Bangla tafsirs (Ibn Kathir, Abu Bakr Zakaria)
     * - Transliteration for pronunciation
     */
    private fun loadOnlineVerseData() {
        viewModelScope.launch {
            try {
                val verseKey = "$surah:$ayah"
                val response = container.quranComApi.getVerseByKey(verseKey)
                if (response.isSuccessful) {
                    val verse = response.body()?.verse
                    if (verse != null) {
                        // Get all Bangla translations
                        val translations = verse.getAllBanglaTranslations().map { (name, text) ->
                            BanglaTranslationOption(name, text)
                        }
                        // Get all Bangla tafsirs
                        val tafsirs = verse.getAllBanglaTafsirs().map { (name, text) ->
                            BanglaTafsirOption(name, text)
                        }
                        // Get transliteration
                        val translit = verse.getTransliteration()

                        _state.value = _state.value.copy(
                            onlineBanglaTranslations = translations,
                            onlineBanglaTafsirs = tafsirs,
                            transliteration = translit,
                            isOnlineDataLoaded = true
                        )
                    }
                }
            } catch (_: Exception) {
                // Offline — bundled data still works
            }
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

    fun selectTranslation(index: Int) {
        _state.value = _state.value.copy(selectedTranslationIndex = index)
    }

    fun selectTafsir(index: Int) {
        _state.value = _state.value.copy(selectedTafsirIndex = index)
    }
}
