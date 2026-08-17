package com.islamichub.app.ui.screens.hadith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.HadithTopic
import com.islamichub.app.data.local.HadithTopicsAssetSource
import com.islamichub.app.data.local.TopicHadith

data class HadithTopicsUiState(
    val topics: List<HadithTopic> = emptyList(),
    val isLoading: Boolean = true
)

class HadithTopicsViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(HadithTopicsUiState())
    val state: StateFlow<HadithTopicsUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val ctx = (container.javaClass.getDeclaredField("context").apply { isAccessible = true }
                    .get(container) as android.content.Context)
                val source = HadithTopicsAssetSource(ctx)
                val data = source.loadHadithTopics()
                _state.value = HadithTopicsUiState(
                    topics = data.hadithTopics ?: emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = HadithTopicsUiState(isLoading = false)
            }
        }
    }
}
