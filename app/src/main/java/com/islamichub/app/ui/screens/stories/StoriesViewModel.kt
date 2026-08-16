package com.islamichub.app.ui.screens.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.StoriesData

data class StoriesUiState(
    val data: StoriesData? = null,
    val isLoading: Boolean = true
)

class StoriesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(StoriesUiState())
    val state: StateFlow<StoriesUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadStories()
                _state.value = StoriesUiState(data = data, isLoading = false)
            } catch (e: Exception) {
                _state.value = StoriesUiState(isLoading = false)
            }
        }
    }
}
