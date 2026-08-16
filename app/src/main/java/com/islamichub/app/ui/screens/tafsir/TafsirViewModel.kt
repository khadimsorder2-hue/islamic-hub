package com.islamichub.app.ui.screens.tafsir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.TafsirSource

data class TafsirUiState(
    val isLoading: Boolean = true,
    val tafsirText: String? = null,
    val source: TafsirSource = TafsirSource.BN_MUKHTASAR,
    val isCached: Boolean = false,
    val error: String? = null
)

class TafsirViewModel(
    private val container: AppContainer,
    private val surah: Int,
    private val ayah: Int
) : ViewModel() {
    private val _state = MutableStateFlow(TafsirUiState())
    val state: StateFlow<TafsirUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val source = container.settingsRepository.tafsirSource.first()
            try {
                val tafsir = container.tafsirRepository.getTafsir(surah, ayah, source.editionId)
                if (tafsir != null) {
                    _state.value = TafsirUiState(
                        isLoading = false,
                        tafsirText = tafsir,
                        source = source,
                        isCached = true
                    )
                } else {
                    _state.value = TafsirUiState(
                        isLoading = false,
                        error = "Could not load tafsir. Check your internet connection.",
                        source = source
                    )
                }
            } catch (e: Exception) {
                _state.value = TafsirUiState(
                    isLoading = false,
                    error = e.message,
                    source = source
                )
            }
        }
    }

    fun changeSource(source: TafsirSource) {
        viewModelScope.launch {
            container.settingsRepository.setTafsirSource(source)
            load()
        }
    }
}
