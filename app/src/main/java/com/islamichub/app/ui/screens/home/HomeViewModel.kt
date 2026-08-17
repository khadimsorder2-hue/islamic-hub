package com.islamichub.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.data.model.PrayerTimes

data class HomeUiState(
    val hijriDate: String = "",
    val nextPrayerName: String = "",
    val nextPrayerTime: String = "",
    val timeRemaining: String = "",
    val ayahOfDay: Ayah? = null,
    val hadithOfDay: String = "",
    val hadithReference: String = "",
    val prayerTimes: PrayerTimes? = null,
    val isLoading: Boolean = true,
    // v3.1.0 daily progress
    val todayPrayersDone: Int = 0,
    val todayTasbihCount: Int = 0,
    val streakDays: Int = 0
)

class HomeViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val ayah = container.quranRepository.ayahOfDay()
            val (hadith, ref) = container.quranRepository.hadithOfDay()

            // Try location-based prayer times
            var times: PrayerTimes? = null
            if (container.prayerRepository.hasLocationPermission() &&
                container.prayerRepository.isLocationEnabled()) {
                val loc = container.prayerRepository.getCurrentLocation()
                if (loc != null) {
                    val result = container.prayerRepository.getPrayerTimes(loc.latitude, loc.longitude)
                    result.getOrNull()?.let { times = it }
                }
            }
            if (times == null) {
                container.prayerRepository.getDefaultPrayerTimes().getOrNull()?.let { times = it }
            }

            val nextPrayer = times?.let { computeNextPrayer(it) }

            // Daily progress (v3.1.0)
            val todayTracker = container.trackerRepository.today.first()
            val tasbihTotal = container.tasbihRepository.total.first()
            val streak = container.trackerRepository.prayerStreak.first()

            _uiState.value = HomeUiState(
                hijriDate = times?.hijriDate ?: "",
                nextPrayerName = nextPrayer?.first ?: "",
                nextPrayerTime = nextPrayer?.second ?: "",
                timeRemaining = nextPrayer?.third ?: "",
                ayahOfDay = ayah,
                hadithOfDay = hadith,
                hadithReference = ref,
                prayerTimes = times,
                isLoading = false,
                todayPrayersDone = todayTracker.prayersDone(),
                todayTasbihCount = tasbihTotal,
                streakDays = streak
            )
        }
    }

    private fun computeNextPrayer(times: PrayerTimes): Triple<String, String, String>? {
        val now = System.currentTimeMillis()
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val today = java.util.Calendar.getInstance()
        fun parse(t: String): Long {
            val parts = t.split(":")
            val c = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(java.util.Calendar.MINUTE, parts.getOrNull(1)?.takeIf { it.matches(Regex("\\d+")) }?.toInt() ?: 0)
                set(java.util.Calendar.SECOND, 0)
            }
            return c.timeInMillis
        }
        val list = listOf(
            Triple("Fajr", times.fajr, parse(times.fajr)),
            Triple("Dhuhr", times.dhuhr, parse(times.dhuhr)),
            Triple("Asr", times.asr, parse(times.asr)),
            Triple("Maghrib", times.maghrib, parse(times.maghrib)),
            Triple("Isha", times.isha, parse(times.isha))
        )
        val upcoming = list.firstOrNull { it.third > now } ?: list.first()
        val diff = upcoming.third - now
        val hours = diff / 3_600_000
        val mins = (diff % 3_600_000) / 60_000
        val remaining = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        return Triple(upcoming.first, upcoming.second, remaining)
    }
}

@Composable
fun homeViewModel(container: AppContainer): HomeViewModel {
    return remember { HomeViewModel(container) }
}
