package com.islamichub.app.ui.screens.namaz_extras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.NamazExtrasData

data class NamazExtrasUiState(
    val data: NamazExtrasData? = null,
    val isLoading: Boolean = true
)

class NamazExtrasViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(NamazExtrasUiState())
    val state: StateFlow<NamazExtrasUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadNamazExtras()
                _state.value = NamazExtrasUiState(data = data, isLoading = false)
            } catch (e: Exception) {
                _state.value = NamazExtrasUiState(isLoading = false)
            }
        }
    }
}
