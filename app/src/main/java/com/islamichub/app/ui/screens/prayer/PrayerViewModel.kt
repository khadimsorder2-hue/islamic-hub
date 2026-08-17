package com.islamichub.app.ui.screens.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.PrayerTimes

data class PrayerUiState(
    val times: PrayerTimes? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val notificationsScheduled: Boolean = false,
    val nextNotificationTitle: String? = null
)

class PrayerViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(PrayerUiState())
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(
            hasLocationPermission = container.prayerRepository.hasLocationPermission(),
            hasNotificationPermission = container.prayerScheduler.hasNotificationPermission()
        )
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            var times: PrayerTimes? = null
            var error: String? = null

            if (container.prayerRepository.hasLocationPermission() &&
                container.prayerRepository.isLocationEnabled()) {
                val loc = container.prayerRepository.getCurrentLocation()
                if (loc != null) {
                    val result = container.prayerRepository.getPrayerTimes(loc.latitude, loc.longitude)
                    times = result.getOrNull()
                    if (times == null) error = result.exceptionOrNull()?.message
                } else {
                    error = "Could not get current location."
                }
            }
            if (times == null) {
                times = container.prayerRepository.getDefaultPrayerTimes().getOrNull()
            }

            val resolvedTimes = times
            // Schedule notifications if we got times AND have permission
            if (resolvedTimes != null && container.prayerScheduler.hasNotificationPermission()) {
                try {
                    container.prayerScheduler.scheduleToday(resolvedTimes)
                    val scheduleState = container.prayerScheduler.state.value
                    _state.value = PrayerUiState(
                        times = resolvedTimes,
                        isLoading = false,
                        error = error,
                        hasLocationPermission = container.prayerRepository.hasLocationPermission(),
                        hasNotificationPermission = container.prayerScheduler.hasNotificationPermission(),
                        notificationsScheduled = true,
                        nextNotificationTitle = scheduleState.nextNotificationTitle
                    )
                    return@launch
                } catch (e: Exception) {
                    error = (error ?: "") + " (notification schedule failed: ${e.message})"
                }
            }

            _state.value = PrayerUiState(
                times = resolvedTimes,
                isLoading = false,
                error = error,
                hasLocationPermission = container.prayerRepository.hasLocationPermission(),
                hasNotificationPermission = container.prayerScheduler.hasNotificationPermission(),
                notificationsScheduled = false
            )
        }
    }

    fun scheduleNotifications() {
        viewModelScope.launch {
            val times = _state.value.times ?: return@launch
            try {
                container.prayerScheduler.scheduleToday(times)
                _state.value = _state.value.copy(
                    notificationsScheduled = true,
                    nextNotificationTitle = container.prayerScheduler.state.value.nextNotificationTitle
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun saveJamatTime(jamat: com.islamichub.app.data.repo.JamatTime) {
        viewModelScope.launch {
            container.jamatTimeRepository.setJamatTime(jamat)
        }
    }
}
