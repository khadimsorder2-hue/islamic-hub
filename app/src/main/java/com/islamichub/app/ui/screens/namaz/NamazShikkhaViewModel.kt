package com.islamichub.app.ui.screens.namaz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.NamazShikkhaData
import com.islamichub.app.data.local.NamazStep

data class NamazShikkhaUiState(
    val data: NamazShikkhaData? = null,
    val selectedMadhhab: String = "হানাফী",
    val selectedGender: String = "পুরুষ",
    val selectedPrayer: String = "fajr",
    val isLoading: Boolean = true,
    val error: String? = null
)

class NamazShikkhaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(NamazShikkhaUiState())
    val state: StateFlow<NamazShikkhaUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadNamazShikkha()
                _state.value = NamazShikkhaUiState(
                    data = data,
                    selectedMadhhab = data.defaultMadhhab ?: "হানাফী",
                    selectedGender = data.defaultGender ?: "পুরুষ",
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = NamazShikkhaUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun setMadhhab(m: String) { _state.value = _state.value.copy(selectedMadhhab = m) }
    fun setGender(g: String) { _state.value = _state.value.copy(selectedGender = g) }
    fun setPrayer(p: String) { _state.value = _state.value.copy(selectedPrayer = p) }
}
