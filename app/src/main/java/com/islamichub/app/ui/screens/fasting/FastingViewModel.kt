package com.islamichub.app.ui.screens.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FastingUiState(
    val stats: FastingStats = FastingStats(),
    val todayFast: FastType? = null,
    val recentEntries: List<FastEntry> = emptyList(),
    val showAddSheet: Boolean = false,
    val selectedType: FastType = FastType.NAFL
)

class FastingViewModel(private val container: AppContainer) : ViewModel() {

    private val repo = container.fastingRepository

    private val _uiState = MutableStateFlow(FastingUiState())
    val uiState: StateFlow<FastingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.stats.collect { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
        }
        viewModelScope.launch {
            repo.entries.collect { entries ->
                val today = todayStr()
                val todayFast = entries.find { it.date == today && it.completed }?.type
                _uiState.update {
                    it.copy(
                        recentEntries = entries.sortedByDescending { e -> e.date }.take(10),
                        todayFast = todayFast
                    )
                }
            }
        }
    }

    fun showAddSheet() {
        _uiState.update { it.copy(showAddSheet = true) }
    }

    fun hideAddSheet() {
        _uiState.update { it.copy(showAddSheet = false) }
    }

    fun selectType(type: FastType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun addTodayFast() {
        val type = _uiState.value.selectedType
        viewModelScope.launch {
            repo.addFast(type = type, date = todayStr())
            _uiState.update { it.copy(showAddSheet = false) }
        }
    }

    fun addFastToday(type: FastType) {
        viewModelScope.launch {
            repo.addFast(type = type, date = todayStr())
        }
    }

    fun removeFast(date: String, type: FastType) {
        viewModelScope.launch {
            repo.removeFast(date = date, type = type)
        }
    }

    fun resetAll() {
        viewModelScope.launch { repo.resetAll() }
    }

    private fun todayStr(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
