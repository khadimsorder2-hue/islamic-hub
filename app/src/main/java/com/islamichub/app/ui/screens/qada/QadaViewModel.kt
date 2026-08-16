package com.islamichub.app.ui.screens.qada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.QadaSummary

data class QadaUiState(
    val summary: QadaSummary = QadaSummary(),
    val isLoading: Boolean = true
)

class QadaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QadaUiState())
    val state: StateFlow<QadaUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            container.qadaRepository.summary.collect { summary ->
                _state.value = QadaUiState(summary = summary, isLoading = false)
            }
        }
    }

    fun addMissed(prayer: String, count: Int = 1) {
        viewModelScope.launch { container.qadaRepository.addMissed(prayer, count) }
    }

    fun markCompleted(prayer: String, count: Int = 1) {
        viewModelScope.launch { container.qadaRepository.markCompleted(prayer, count) }
    }

    fun reset() {
        viewModelScope.launch { container.qadaRepository.reset() }
    }
}
