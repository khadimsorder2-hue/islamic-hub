package com.islamichub.app.ui.screens.qada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.QadaPeriod
import com.islamichub.app.data.repo.QadaPeriodStat
import com.islamichub.app.data.repo.QadaSummary
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QadaUiState(
    val summary: QadaSummary = QadaSummary(),
    val isLoading: Boolean = true,
    // Date-selection state for logging a missed prayer against a specific
    // day (instead of always defaulting silently to "today").
    val selectedLogDate: String = today(),
    // History dashboard state.
    val selectedPeriod: QadaPeriod = QadaPeriod.MONTH,
    val selectedPrayerFilter: String? = null, // null = all prayers
    val periodStats: List<QadaPeriodStat> = emptyList()
) {
    companion object {
        fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}

class QadaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QadaUiState())
    val state: StateFlow<QadaUiState> = _state.asStateFlow()

    private var statsJob: Job? = null

    init {
        load()
        loadStats()
    }

    private fun load() {
        viewModelScope.launch {
            container.qadaRepository.summary.collect { summary ->
                _state.value = _state.value.copy(summary = summary, isLoading = false)
            }
        }
    }

    private fun loadStats() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            val s = _state.value
            container.qadaRepository.statsFor(s.selectedPeriod, s.selectedPrayerFilter).collect { stats ->
                _state.value = _state.value.copy(periodStats = stats)
            }
        }
    }

    /** Change the history dashboard's grouping (day/week/month/year). */
    fun selectPeriod(period: QadaPeriod) {
        _state.value = _state.value.copy(selectedPeriod = period)
        loadStats()
    }

    /** Filter the history dashboard to one prayer, or null for all. */
    fun selectPrayerFilter(prayer: String?) {
        _state.value = _state.value.copy(selectedPrayerFilter = prayer)
        loadStats()
    }

    /** Set the date to log a missed prayer against, instead of always "today". */
    fun selectLogDate(date: String) {
        _state.value = _state.value.copy(selectedLogDate = date)
    }

    fun addMissed(prayer: String, count: Int = 1, date: String? = null) {
        viewModelScope.launch {
            container.qadaRepository.addMissed(prayer, count, date ?: _state.value.selectedLogDate)
        }
    }

    fun markCompleted(prayer: String, count: Int = 1) {
        viewModelScope.launch { container.qadaRepository.markCompleted(prayer, count) }
    }

    fun reset() {
        viewModelScope.launch { container.qadaRepository.reset() }
    }
}
