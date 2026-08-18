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
    val totalFasts: Int = 0,
    val qadaPending: Int = 0,
    val isLoading: Boolean = true,
    val backupMessage: String? = null
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val name = container.settingsRepository.userName.first()
                val totalZikr = container.trackerRepository.totalZikr.first()
                val totalAyahs = container.trackerRepository.totalAyahsRead.first()
                val totalHadiths = container.trackerRepository.totalHadithsRead.first()
                val bookmarkCount = container.bookmarkRepository.bookmarks.first().size
                val khatamPercent = container.khatamRepository.progressPercent.first()
                val khatamSurahs = container.khatamRepository.completedSurahCount.first()
                val streak = container.trackerRepository.prayerStreak.first()

                // Fasting stats — fallback to 0 if repository fails
                val fastingStats = try {
                    container.fastingRepository.stats.first()
                } catch (_: Exception) { null }

                // Qada pending
                val qada = try {
                    container.qadaRepository.summary.first()
                } catch (_: Exception) { null }

                _state.value = ProfileUiState(
                    userName = name,
                    totalZikr = totalZikr,
                    totalAyahs = totalAyahs,
                    totalHadiths = totalHadiths,
                    bookmarkCount = bookmarkCount,
                    khatamPercent = khatamPercent * 100f,
                    khatamSurahs = khatamSurahs,
                    prayerStreak = streak,
                    totalFasts = fastingStats?.totalFasts ?: 0,
                    qadaPending = qada?.total ?: 0,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { container.settingsRepository.setUserName(name) }
        _state.value = _state.value.copy(userName = name)
    }

    fun backup() {
        viewModelScope.launch {
            try {
                val result = container.backupRestoreService.export()
                _state.value = _state.value.copy(
                    backupMessage = result.fold(
                        onSuccess = { "✓ ব্যাকআপ সফল: $it" },
                        onFailure = { "ব্যাকআপ ব্যর্থ: ${it.message}" }
                    )
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(backupMessage = "ব্যাকআপ ব্যর্থ: ${e.message}")
            }
        }
    }

    fun restore() {
        viewModelScope.launch {
            try {
                val backups = container.backupRestoreService.listBackups()
                if (backups.isEmpty()) {
                    _state.value = _state.value.copy(backupMessage = "কোনো ব্যাকআপ পাওয়া যায়নি")
                    return@launch
                }
                val latest = backups.first()
                val result = container.backupRestoreService.import(latest.absolutePath)
                _state.value = _state.value.copy(
                    backupMessage = result.fold(
                        onSuccess = { "✓ পুনরুদ্ধার সফল" },
                        onFailure = { "পুনরুদ্ধার ব্যর্থ: ${it.message}" }
                    )
                )
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(backupMessage = "পুনরুদ্ধার ব্যর্থ: ${e.message}")
            }
        }
    }
}
