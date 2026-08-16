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
    val hasLocationPermission: Boolean = false
)

class PrayerViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(PrayerUiState())
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(hasLocationPermission = container.prayerRepository.hasLocationPermission())
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
                    result.getOrNull()?.let { times = it }
                    if (times == null) error = result.exceptionOrNull()?.message
                } else {
                    error = "Could not get current location."
                }
            }
            if (times == null) {
                container.prayerRepository.getDefaultPrayerTimes().getOrNull()?.let { times = it }
            }

            _state.value = PrayerUiState(
                times = times,
                isLoading = false,
                error = error,
                hasLocationPermission = container.prayerRepository.hasLocationPermission()
            )
        }
    }
}
