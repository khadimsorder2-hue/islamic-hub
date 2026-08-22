package com.islamichub.app.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AyahSearchResult

data class QuranSearchUiState(
    val query: String = "",
    val results: List<AyahSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val searchSource: SearchSource = SearchSource.OFFLINE,
    val totalOnlineResults: Int = 0
)

enum class SearchSource(val labelBn: String) {
    OFFLINE("অফলাইন"),
    ONLINE_API("Quran.com API")
}

class QuranSearchViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QuranSearchUiState())
    val state: StateFlow<QuranSearchUiState> = _state.asStateFlow()

    fun onQueryChange(q: String) {
        _state.value = _state.value.copy(query = q)
        if (q.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), hasSearched = false)
            return
        }
        search(q)
    }

    fun switchSource(source: SearchSource) {
        val q = _state.value.query
        if (q.isBlank()) return
        _state.value = _state.value.copy(searchSource = source)
        search(q)
    }

    private fun search(q: String) {
        _state.value = _state.value.copy(isSearching = true, hasSearched = true)
        viewModelScope.launch {
            val results = when (_state.value.searchSource) {
                SearchSource.ONLINE_API -> searchOnline(q)
                SearchSource.OFFLINE -> container.quranRepository.searchAyahs(q, limit = 100)
            }
            _state.value = _state.value.copy(results = results, isSearching = false)
        }
    }

    private suspend fun searchOnline(q: String): List<AyahSearchResult> {
        return try {
            val response = container.quranComApi.searchVerses(q, size = 50)
            if (response.isSuccessful) {
                val search = response.body()?.search
                _state.value = _state.value.copy(totalOnlineResults = search?.totalResults ?: 0)
                search?.results?.mapNotNull { r ->
                    val key = r.verseKey ?: return@mapNotNull null
                    val parts = key.split(":")
                    if (parts.size != 2) return@mapNotNull null
                    val surahNum = parts[0].toIntOrNull() ?: return@mapNotNull null
                    val ayahNum = parts[1].toIntOrNull() ?: return@mapNotNull null
                    val bnText = r.translations?.firstOrNull {
                        it.id == 163 || it.id == 213 || it.id == 161
                    }?.text?.replace(Regex("<[^>]*>"), "") ?: ""
                    val enText = r.translations?.firstOrNull { it.id == 84 }?.text?.replace(Regex("<[^>]*>"), "") ?: ""
                    val surahData = container.quranRepository.getSurah(surahNum)
                    AyahSearchResult(
                        surahNumber = surahNum,
                        surahName = surahData?.nameEnglish ?: "Surah $surahNum",
                        surahNameBn = surahData?.nameBengali ?: "",
                        ayahNumber = ayahNum,
                        arabic = r.text ?: "",
                        english = enText,
                        bengali = bnText
                    )
                } ?: emptyList()
            } else emptyList()
        } catch (_: Exception) {
            // Fallback to offline
            container.quranRepository.searchAyahs(q, limit = 100)
        }
    }
}
