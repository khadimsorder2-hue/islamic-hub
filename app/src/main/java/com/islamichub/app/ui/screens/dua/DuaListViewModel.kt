package com.islamichub.app.ui.screens.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
    val isLoading: Boolean = true,
    val error: String? = null
)

class DuaListViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(DuaListUiState())
    val state: StateFlow<DuaListUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init { load() }

    fun load() {
        // Cancel any in-flight load to prevent stale state
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val data = container.contentRepository.loadExtendedDuas()
                val grouped = data.getGroupedDuas()
                _state.value = DuaListUiState(
                    categories = data.categories ?: emptyList(),
                    duasByCategory = grouped,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = DuaListUiState(
                    isLoading = false,
                    error = e.message ?: "দোয়া লোড করা যায়নি"
                )
            }
        }
    }
}

