package com.islamichub.app.ui.screens.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.FullStoriesData
import com.islamichub.app.data.local.FullStoriesAssetSource
import com.islamichub.app.data.local.FullProphet
import com.islamichub.app.data.local.FullKhalifa
import com.islamichub.app.data.local.FullStoryChapter

data class StoriesUiState(
    val isLoading: Boolean = true,
    val merajChapters: List<FullStoryChapter> = emptyList(),
    val siratChapters: List<FullStoryChapter> = emptyList(),
    val prophets: List<FullProphet> = emptyList(),
    val khalifas: List<FullKhalifa> = emptyList(),
    val merajTitle: String = "",
    val siratTitle: String = ""
)

class StoriesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(StoriesUiState())
    val state: StateFlow<StoriesUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val ctx = (container.javaClass.getDeclaredField("context").apply { isAccessible = true }
                    .get(container) as android.content.Context)
                val source = FullStoriesAssetSource(ctx)
                val data = source.loadFullStories()
                _state.value = StoriesUiState(
                    isLoading = false,
                    merajChapters = data.meraj?.chapters ?: emptyList(),
                    siratChapters = data.sirat?.chapters ?: emptyList(),
                    prophets = data.prophets ?: emptyList(),
                    khalifas = data.khalifas ?: emptyList(),
                    merajTitle = data.meraj?.title ?: "মে'রাজ",
                    siratTitle = data.sirat?.title ?: "সীরাত"
                )
            } catch (e: Exception) {
                _state.value = StoriesUiState(isLoading = false)
            }
        }
    }
}
