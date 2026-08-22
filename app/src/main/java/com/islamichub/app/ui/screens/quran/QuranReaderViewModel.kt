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
import com.islamichub.app.data.repo.TranslationCacheService

data class QuranReaderUiState(
    val surah: Surah? = null,
    val isLoading: Boolean = true,
    val notAvailable: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val isLoadingAudio: Boolean = false,
    val currentPlayingAyah: Int? = null,
    val totalAyahsInSurah: Int = 0,
    val bookmarkedAyahs: Set<Int> = emptySet(),
    val quranFontScale: Float = 1.0f,
    val showArabic: Boolean = true,
    val showBangla: Boolean = true,
    val showEnglish: Boolean = true,
    val selectedReciterId: String = "ar.alafasy",
    val selectedReciterName: String = "Mishary Rashid Alafasy",
    val banglaAudioEnabled: Boolean = false,
    val isPlayingBanglaAudio: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isRepeatMode: Boolean = false,
    // v5.1+ — Multi Bangla Translation from Quran.com API
    val availableTranslations: List<String> = emptyList(),
    val selectedTranslationIndex: Int = 0,
    val onlineTranslationsMap: Map<Int, List<String>> = emptyMap(), // ayahNumber → [text1, text2, ...]
    val isLoadingOnlineTranslations: Boolean = false,
    val onlineTranslationsLoaded: Boolean = false
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
        loadOnlineTranslations()
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
                        audioState.currentAyah else null,
                    totalAyahsInSurah = audioState.totalAyahsInSurah,
                    isPlayingBanglaAudio = audioState.isPlayingBanglaAudio,
                    banglaAudioEnabled = audioState.banglaAudioEnabled,
                    playbackSpeed = audioState.playbackSpeed,
                    isRepeatMode = audioState.isRepeatMode
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
                val reciter = AudioController.availableRecitersStatic.firstOrNull {
                    it.editionId == id
                } ?: AudioController.availableRecitersStatic.first()
                _state.value = _state.value.copy(
                    selectedReciterId = id,
                    selectedReciterName = reciter.displayName
                )
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

    fun toggleBanglaAudio() {
        val current = _state.value.banglaAudioEnabled
        val newValue = !current
        container.audioController.setBanglaAudioEnabled(newValue)
        viewModelScope.launch {
            container.settingsRepository.setBanglaAudioEnabled(newValue)
        }
    }

    fun increaseFontSize() {
        val newScale = (_state.value.quranFontScale + 0.1f).coerceAtMost(2.0f)
        viewModelScope.launch {
            container.settingsRepository.setQuranFontScale(newScale)
        }
    }

    fun decreaseFontSize() {
        val newScale = (_state.value.quranFontScale - 0.1f).coerceAtLeast(0.7f)
        viewModelScope.launch {
            container.settingsRepository.setQuranFontScale(newScale)
        }
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

    /**
     * Fetch all Bangla translations for this surah from Quran.com API.
     * Stores results in onlineTranslationsMap: ayahNumber → [mujib, taisirul, zakaria, rawai]
     */
    private fun loadOnlineTranslations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingOnlineTranslations = true)
            try {
                val response = container.quranComApi.getVersesByChapter(
                    chapterNumber = surahNumber,
                    translations = "163,161,213,162",
                    perPage = 300
                )
                if (response.isSuccessful) {
                    val verses = response.body()?.verses ?: emptyList()
                    val transMap = mutableMapOf<Int, List<String>>()
                    val names = mutableListOf<String>()
                    for (verse in verses) {
                        val ayahNum = verse.verseNumber ?: continue
                        val texts = mutableListOf<String>()
                        verse.getBanglaTranslationMujib()?.let { texts.add(it) }
                        verse.getBanglaTranslationTaisirul()?.let { texts.add(it) }
                        verse.getBanglaTranslationZakaria()?.let { texts.add(it) }
                        verse.getBanglaTranslationRawai()?.let { texts.add(it) }
                        if (texts.isNotEmpty()) transMap[ayahNum] = texts
                    }
                    // Build translation names from first verse that has all
                    val first = verses.firstOrNull()
                    if (first != null) {
                        first.getBanglaTranslationMujib()?.let { names.add("মুহিউদ্দীন খান") }
                        first.getBanglaTranslationTaisirul()?.let { names.add("তাইসিরুল কুরআন") }
                        first.getBanglaTranslationZakaria()?.let { names.add("ড. যাকারিয়া") }
                        first.getBanglaTranslationRawai()?.let { names.add("রাওয়ায়ে বায়ান") }
                    }
                    _state.value = _state.value.copy(
                        availableTranslations = names,
                        onlineTranslationsMap = transMap,
                        isLoadingOnlineTranslations = false,
                        onlineTranslationsLoaded = true
                    )
                } else {
                    _state.value = _state.value.copy(isLoadingOnlineTranslations = false)
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(isLoadingOnlineTranslations = false)
            }
        }
    }

    fun selectTranslation(index: Int) {
        _state.value = _state.value.copy(selectedTranslationIndex = index)
    }

    /** Get the Bangla text for an ayah considering selected translation */
    fun getBanglaTextForAyah(ayahNumber: Int, fallback: String): String {
        val state = _state.value
        if (!state.onlineTranslationsLoaded) return fallback
        val translations = state.onlineTranslationsMap[ayahNumber]
        if (translations == null || translations.isEmpty()) return fallback
        val idx = state.selectedTranslationIndex.coerceIn(0, translations.size - 1)
        return translations[idx].replace(Regex("<[^>]*>"), "")
    }

    /** Download surah translations for offline use */
    fun downloadForOffline() {
        viewModelScope.launch {
            val surah = _state.value.surah ?: return@launch
            try {
                val response = container.quranComApi.getVersesByChapter(
                    chapterNumber = surahNumber,
                    translations = "163,161,213,162,84",
                    perPage = 300
                )
                if (response.isSuccessful) {
                    val verses = response.body()?.verses ?: return@launch
n                    val cached = verses.map { v ->
                        TranslationCacheService.CachedVerse(
                            surah = surahNumber,
                            ayah = v.verseNumber ?: return@map null,
                            translations = v.getAllBanglaTranslations().associate { (name, text) -> name to text },
                            tafsirs = v.getAllBanglaTafsirs().associate { (name, text) -> name to text },
                            transliteration = v.getTransliteration()
                        )
                    }.filterNotNull()
                    container.translationCache.cacheSurah(surahNumber, cached)
                }
            } catch (_: Exception) {}
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
