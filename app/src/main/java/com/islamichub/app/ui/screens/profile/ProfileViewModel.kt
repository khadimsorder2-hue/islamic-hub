package com.islamichub.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer

data class ProfileUiState(
    val userName: String = "",
    val totalZikr: Int = 0,
    val totalAyahs: Int = 0,
    val totalHadiths: Int = 0,
    val bookmarkCount: Int = 0,
    val khatamPercent: Float = 0f,
    val khatamSurahs: Int = 0,
    val prayerStreak: Int = 0,
    val isLoading: Boolean = true
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val name = container.settingsRepository.userName.first()
            val totalZikr = container.trackerRepository.totalZikr.first()
            val totalAyahs = container.trackerRepository.totalAyahsRead.first()
            val bookmarkCount = container.bookmarkRepository.bookmarks.first().size
            val khatamPercent = container.khatamRepository.progressPercent.first()
            val khatamSurahs = container.khatamRepository.completedSurahCount.first()
            val streak = container.trackerRepository.prayerStreak.first()

            _state.value = ProfileUiState(
                userName = name,
                totalZikr = totalZikr,
                totalAyahs = totalAyahs,
                bookmarkCount = bookmarkCount,
                khatamPercent = khatamPercent * 100f,
                khatamSurahs = khatamSurahs,
                prayerStreak = streak,
                isLoading = false
            )
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { container.settingsRepository.setUserName(name) }
        _state.value = _state.value.copy(userName = name)
    }
}

