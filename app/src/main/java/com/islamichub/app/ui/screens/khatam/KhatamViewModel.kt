package com.islamichub.app.ui.screens.khatam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.KhatamHistoryEntry
import com.islamichub.app.data.repo.KhatamPeriod
import com.islamichub.app.data.repo.KhatamPeriodStat
import com.islamichub.app.data.repo.KhatamProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class KhatamUiState(
    val currentKhatam: KhatamProgress? = null,
    val progressPercent: Float = 0f,
    val completedSurahCount: Int = 0,
    // Permanent history log — most recent completion first.
    val history: List<KhatamHistoryEntry> = emptyList(),
    val totalKhatamsCompleted: Int = 0,
    // Mean of history daysTaken; null when history is empty (UI shows "—").
    val averageDaysTaken: Double? = null,
    // Repository-side year buckets (statsFor(YEAR)) for the year-in-review bars.
    val yearStats: List<KhatamPeriodStat> = emptyList()
)

/**
 * ViewModel for the Khatam tracker screen.
 *
 * Follows the app's manual-DI pattern: repositories come from [AppContainer]
 * (see QadaViewModel / FastingViewModel). KhatamRepository exposes cold
 * DataStore-backed Flows (no StateFlow), so this VM collects them and
 * publishes a single immutable [KhatamUiState].
 */
class KhatamViewModel(private val container: AppContainer) : ViewModel() {

    private val repo = container.khatamRepository

    private val _state = MutableStateFlow(KhatamUiState())
    val state: StateFlow<KhatamUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.currentKhatam,
                repo.progressPercent,
                repo.completedSurahCount,
                repo.history,
                repo.totalKhatamsCompleted
            ) { current, percent, surahCount, history, total ->
                KhatamUiState(
                    currentKhatam = current,
                    progressPercent = percent,
                    completedSurahCount = surahCount,
                    history = history.sortedByDescending { it.completionDate },
                    totalKhatamsCompleted = total,
                    averageDaysTaken =
                        if (history.isEmpty()) null
                        else history.map { it.daysTaken }.average()
                )
            }.collect { newState ->
                _state.value = newState
            }
        }

        // Year-in-review buckets straight from the repository's own stats API.
        viewModelScope.launch {
            try {
                repo.statsFor(KhatamPeriod.YEAR).collect { stats ->
                    _state.value = _state.value.copy(yearStats = stats)
                }
            } catch (_: Exception) {
                // History dashboard is optional — never crash the tracker.
            }
        }
    }

    /** Start a fresh in-progress khatam. History is untouched. */
    fun startNew() {
        viewModelScope.launch { repo.startNew() }
    }

    /**
     * Reset only the in-progress khatam. KhatamRepository.reset() preserves
     * the permanent history by design — only clearHistory() erases that.
     */
    fun reset() {
        viewModelScope.launch { repo.reset() }
    }

    /** Permanent, explicit erase of the completed-khatam history log. */
    fun clearHistory() {
        viewModelScope.launch { repo.clearHistory() }
    }
}
