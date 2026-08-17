package com.islamichub.app.ui.screens.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.ExtendedDua
import com.islamichub.app.data.local.ExtendedDuaCategory

data class DuaListUiState(
    val categories: List<ExtendedDuaCategory> = emptyList(),
    val duasByCategory: Map<String, List<ExtendedDua>> = emptyMap(),
    val isLoading: Boolean = true
)

class DuaListViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(DuaListUiState())
    val state: StateFlow<DuaListUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadExtendedDuas()
                val grouped = data.getGroupedDuas()
                _state.value = DuaListUiState(
                    categories = data.categories ?: emptyList(),
                    duasByCategory = grouped,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = DuaListUiState(isLoading = false)
            }
        }
    }
}
