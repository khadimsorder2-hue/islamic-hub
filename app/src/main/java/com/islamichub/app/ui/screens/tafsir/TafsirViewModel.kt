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

data class TranslationOption(
    val name: String,
    val language: String, // "bn" or "en"
    val text: String
)

data class TafsirOption(
    val name: String,
    val language: String, // "bn" or "en"
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
    // Quran.com API online data — ALL translations & tafsirs
    val allTranslations: List<TranslationOption> = emptyList(),
    val allTafsirs: List<TafsirOption> = emptyList(),
    val transliteration: String? = null,
    val isOnlineDataLoaded: Boolean = false,
    val selectedTranslationIndex: Int = 0,
    val selectedTafsirIndex: Int = 0,
    // Notes feature
    val noteText: String = "",
    val isNoteSaving: Boolean = false,
    val isNoteSaved: Boolean = false
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
        loadNote()
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
     * Load verse data from Quran.com API — ALL translations + ALL tafsirs
     * Bangla: মুহিউদ্দীন খান, তাইসিরুল কুরআন, ড. যাকারিয়া, রাওয়ায়ে বায়ান
     * English: T. Usmani
     * Bangla Tafsirs: ইবনে কাসীর, আবু বকর যাকারিয়া, আহসানুল বায়ান, ফাতহুল মজীদ
     * English Tafsir: Ibn Kathir (Abridged)
     */
    private fun loadOnlineVerseData() {
        viewModelScope.launch {
            try {
                val verseKey = "$surah:$ayah"
                val response = container.quranComApi.getVerseByKey(
                    verseKey,
                    translations = "163,161,213,162,84", // 4 Bangla + 1 English
                    tafsirs = "164,165,166,381,169"    // 4 Bangla + 1 English
                )
                if (response.isSuccessful) {
                    val verse = response.body()?.verse ?: return@launch
                    val translations = mutableListOf<TranslationOption>()
                    verse.getBanglaTranslationMujib()?.let { translations.add(TranslationOption("মুহিউদ্দীন খান", "bn", it)) }
                    verse.getBanglaTranslationTaisirul()?.let { translations.add(TranslationOption("তাইসিরুল কুরআন", "bn", it)) }
                    verse.getBanglaTranslationZakaria()?.let { translations.add(TranslationOption("ড. যাকারিয়া", "bn", it)) }
                    verse.getBanglaTranslationRawai()?.let { translations.add(TranslationOption("রাওয়ায়ে বায়ান", "bn", it)) }
                    verse.getEnglishTranslation()?.let { translations.add(TranslationOption("T. Usmani (English)", "en", it)) }
                    val tafsirs = mutableListOf<TafsirOption>()
                    verse.getTafsirIbnKathirBn()?.let { tafsirs.add(TafsirOption("তাফসীর ইবনে কাসীর", "bn", it)) }
                    verse.getTafsirZakariaBn()?.let { tafsirs.add(TafsirOption("তাফসীর আবু বকর যাকারিয়া", "bn", it)) }
                    verse.getTafsirAhsanulBn()?.let { tafsirs.add(TafsirOption("তাফসীর আহসানুল বায়ান", "bn", it)) }
                    verse.getTafsirFathulMajidBn()?.let { tafsirs.add(TafsirOption("তাফসীর ফাতহুল মজীদ", "bn", it)) }
                    tafsirs.add(TafsirOption("Ibn Kathir (English)", "en", ""))
                    // Fetch English tafsir separately via Ibn Kathir
                    try {
                        val engResponse = container.quranComApi.getVerseByKey(
                            verseKey,
                            translations = "163",
                            tafsirs = "169"
                        )
                        val engVerse = engResponse.body()?.verse
                        engVerse?.getEnglishTafsirIbnKathir()?.let { engText ->
                            if (tafsirs.any { it.name.contains("English") }) {
                                val idx = tafsirs.indexOfFirst { it.name.contains("English") }
                                tafsirs[idx] = tafsirs[idx].copy(text = engText)
                            }
                        }
                    } catch (_: Exception) {}
                    _state.value = _state.value.copy(
                        allTranslations = translations,
                        allTafsirs = tafsirs,
                        transliteration = verse.getTransliteration(),
                        isOnlineDataLoaded = true
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadTafsir() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val source = container.settingsRepository.tafsirSource.first()
            try {
                val tafsir = container.tafsirRepository.getTafsir(surah, ayah, source.editionId)
                if (tafsir != null) {
                    _state.value = _state.value.copy(isLoading = false, tafsirText = tafsir, source = source, isCached = true)
                } else {
                    _state.value = _state.value.copy(isLoading = false, source = source)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message, source = source)
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
                val prompt = """
সূরা: ${surahData?.nameEnglish ?: ""} (আয়াত $ayah)
আরবি: ${ayahData?.arabic ?: ""}
বাংলা অনুবাদ: ${ayahData?.bengali ?: ""}
এই আয়াতের সম্পূর্ণ তাফসীর দিন। একজন গ্রামের খতিব যেভাবে সাধারণ মানুষকে শূন্য থেকে বোঝান, সেভাবে বোঝান। নাজিলের কারণ উল্লেখ করবেন।""".trimIndent()
                val result = container.aiService.ask(prompt, cacheType = "tafsir")
                if (result.error == null) {
                    _state.value = _state.value.copy(isAILoading = false, aiExplanation = result.answer)
                } else {
                    _state.value = _state.value.copy(isAILoading = false)
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(isAILoading = false)
            }
        }
    }

    private fun loadNote() {
        viewModelScope.launch {
            val existing = container.settingsRepository.getAyahNote(surah, ayah)
            _state.value = _state.value.copy(noteText = existing, isNoteSaved = existing.isNotBlank())
        }
    }

    fun changeSource(source: TafsirSource) {
        viewModelScope.launch {
            container.settingsRepository.setTafsirSource(source)
            loadTafsir()
        }
    }

    fun selectTranslation(index: Int) { _state.value = _state.value.copy(selectedTranslationIndex = index) }
    fun selectTafsir(index: Int) { _state.value = _state.value.copy(selectedTafsirIndex = index) }

    fun updateNote(text: String) {
        _state.value = _state.value.copy(noteText = text, isNoteSaved = false)
    }

    fun saveNote() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isNoteSaving = true)
            container.settingsRepository.setAyahNote(surah, ayah, _state.value.noteText)
            _state.value = _state.value.copy(isNoteSaving = false, isNoteSaved = true)
        }
    }
}