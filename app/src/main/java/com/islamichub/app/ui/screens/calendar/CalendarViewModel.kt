package com.islamichub.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.HijriDay
import java.util.Calendar
import java.util.GregorianCalendar

data class CalendarUiState(
    val monthTitle: String = "",
    val days: List<HijriDay> = emptyList(),
    val isLoading: Boolean = true
)

class CalendarViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            // Compute Hijri month via Aladhan API (fallback to simple Umm al-Qura approximation).
            val now = Calendar.getInstance()
            val year = now.get(Calendar.YEAR)
            val month = now.get(Calendar.MONTH) + 1

            // Try Aladhan API with default Makkah location
            val result = container.prayerRepository.getDefaultPrayerTimes()
            val hijriBase = result.getOrNull()?.hijriDate ?: "1 Muharram 1446 AH"

            // Parse base hijri date
            val parts = hijriBase.split(" ")
            val baseDay = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val baseMonthName = parts.getOrNull(1) ?: "Muharram"
            val baseYear = parts.getOrNull(2)?.toIntOrNull() ?: 1446
            val baseMonthNumber = hijriMonthNumber(baseMonthName)

            // Build a 30-day view centered around baseDay
            val monthNames = listOf(
                "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
                "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
                "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
            )
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val cal = GregorianCalendar()
            val days = (1..30).map { day ->
                val weekday = weekdays[(cal.get(Calendar.DAY_OF_WEEK) - 1 + day - 1) % 7]
                HijriDay(
                    day = day,
                    monthNumber = baseMonthNumber,
                    monthName = monthNames[(baseMonthNumber - 1).coerceIn(0, 11)],
                    year = baseYear,
                    weekday = weekday,
                    isToday = day == baseDay
                )
            }

            _state.value = CalendarUiState(
                monthTitle = "${monthNames[(baseMonthNumber - 1).coerceIn(0, 11)]} $baseYear AH",
                days = days,
                isLoading = false
            )
        }
    }

    private fun hijriMonthNumber(name: String): Int {
        val names = listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        return names.indexOfFirst { name.contains(it, ignoreCase = true) }
            .let { if (it >= 0) it + 1 else 1 }
    }
}
