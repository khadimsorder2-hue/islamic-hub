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
            _state.value = TrackerUiState(
                today = today,
                streak = streak,
                totalZikr = totalZikr,
                totalAyahs = totalAyahs,
                totalHadiths = totalHadiths,
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

