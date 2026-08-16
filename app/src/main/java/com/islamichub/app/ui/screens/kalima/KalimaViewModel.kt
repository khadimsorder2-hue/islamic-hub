package com.islamichub.app.ui.screens.kalima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.KalimaData

data class KalimaUiState(
    val data: KalimaData? = null,
    val isLoading: Boolean = true
)

class KalimaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(KalimaUiState())
    val state: StateFlow<KalimaUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadKalima()
                _state.value = KalimaUiState(data = data, isLoading = false)
            } catch (e: Exception) {
                _state.value = KalimaUiState(isLoading = false)
            }
        }
    }
}
