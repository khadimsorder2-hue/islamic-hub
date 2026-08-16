package com.islamichub.app.ui.screens.names

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.NameOfAllah

data class NamesUiState(
    val names: List<NameOfAllah> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true
)

class NamesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(NamesUiState())
    val state: StateFlow<NamesUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val list = container.namesRepository.all()
            _state.value = NamesUiState(names = list, isLoading = false)
        }
    }

    fun onQueryChange(q: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(query = q, isLoading = true)
            val list = container.namesRepository.search(q)
            _state.value = _state.value.copy(names = list, isLoading = false)
        }
    }
}
