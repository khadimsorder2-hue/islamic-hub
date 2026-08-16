package com.islamichub.app.ui.screens.namaz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer

data class NamazShikkhaUiState(
    val isLoading: Boolean = true,
    val selectedMadhhab: String = "হানাফী",
    val selectedGender: String = "পুরুষ",
    val selectedPrayer: String = "fajr",
    val apiKeyConfigured: Boolean = false
)

class NamazShikkhaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(NamazShikkhaUiState())
    val state: StateFlow<NamazShikkhaUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val apiKey = container.settingsRepository.aiApiKey.first()
            _state.value = NamazShikkhaUiState(
                isLoading = false,
                apiKeyConfigured = apiKey.isNotBlank()
            )
        }
    }

    fun setMadhhab(m: String) { _state.value = _state.value.copy(selectedMadhhab = m) }
    fun setGender(g: String) { _state.value = _state.value.copy(selectedGender = g) }
    fun setPrayer(p: String) { _state.value = _state.value.copy(selectedPrayer = p) }
}
