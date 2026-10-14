package com.islamichub.app.ui.screens.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.DayTracker

data class TrackerUiState(
    val today: DayTracker = DayTracker(date = ""),
    val streak: Int = 0,
    val totalZikr: Int = 0,
    val totalAyahs: Int = 0,
    val totalHadiths: Int = 0,
    /** v5.18.0 — last 7 days (oldest → today) for the weekly bar chart. */
    val week: List<DayTracker> = emptyList(),
    val isLoading: Boolean = true
)

class TrackerViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val today = container.trackerRepository.today.first()
            val streak = container.trackerRepository.prayerStreak.first()
            val totalZikr = container.trackerRepository.totalZikr.first()
            val totalAyahs = container.trackerRepository.totalAyahsRead.first()
            val totalHadiths = container.trackerRepository.totalHadithsRead.first()
            // v5.18.0 — build the last-7-days list (oldest → today) for the chart
            val allDays = container.trackerRepository.days.first()
            val byDate = allDays.associateBy { it.date }
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val week = (6 downTo 0).map { back ->
                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -back) }
                val date = fmt.format(cal.time)
                byDate[date] ?: DayTracker(date = date)
            }
            _state.value = TrackerUiState(
                today = today,
                streak = streak,
                totalZikr = totalZikr,
                totalAyahs = totalAyahs,
                totalHadiths = totalHadiths,
                week = week,
                isLoading = false
            )
        }
    }

    fun togglePrayer(prayer: String) {
        viewModelScope.launch {
            container.trackerRepository.togglePrayer(prayer)
            load()  // refresh
        }
    }
}

