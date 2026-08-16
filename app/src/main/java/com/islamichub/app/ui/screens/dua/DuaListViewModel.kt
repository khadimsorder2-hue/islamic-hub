package com.islamichub.app.ui.screens.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Dua

data class DuaListUiState(
    val duas: List<Dua> = emptyList(),
    val isLoading: Boolean = true
)

class DuaListViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(DuaListUiState())
    val state: StateFlow<DuaListUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val list = container.duaRepository.allDiary()
            _state.value = DuaListUiState(duas = list, isLoading = false)
        }
    }
}

private suspend fun com.islamichub.app.data.repo.DuaRepository.allDiary(): List<Dua> = allDuas()
