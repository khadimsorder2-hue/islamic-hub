package com.islamichub.app.ui.screens.misconceptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.MisconceptionCategory

data class MisconceptionsUiState(
    val categories: List<MisconceptionCategory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MisconceptionsViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(MisconceptionsUiState())
    val state: StateFlow<MisconceptionsUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadMisconceptions()
                _state.value = MisconceptionsUiState(
                    categories = data.categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = MisconceptionsUiState(isLoading = false, error = e.message)
            }
        }
    }
}
