package com.islamichub.app.ui.screens.tajweed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer

data class TajweedUiState(
    val inputText: String = "",
    val result: String? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val apiKeyConfigured: Boolean = false
)

class TajweedViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(TajweedUiState())
    val state: StateFlow<TajweedUiState> = _state.asStateFlow()

    init { loadConfig() }

    private fun loadConfig() {
        viewModelScope.launch {
            val apiKey = container.settingsRepository.aiApiKey.first()
            val baseUrl = container.settingsRepository.aiBaseUrl.first()
            val model = container.settingsRepository.aiModel.first()
            container.aiService.updateConfig(
                com.islamichub.app.data.repo.AIService.Config(apiKey = apiKey, baseUrl = baseUrl, model = model)
            )
            _state.value = _state.value.copy(apiKeyConfigured = apiKey.isNotBlank())
        }
    }

    fun onInputChange(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    fun analyze() {
        val input = _state.value.inputText.trim()
        if (input.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isAnalyzing = true, error = null, result = null)
            val prompt = """Analyze the following Arabic Quranic text for Tajweed rules. Identify any Tajweed errors and provide corrections. Explain which Tajweed rules apply (e.g., Nur rules, Idgham, Ikhfa, Iqlab, Qalqalah, Madd, Ghunnah).

Text to analyze:
$input

Format your response:
1. Detected Tajweed rules applied correctly
2. Errors found (if any) with correction
3. Suggestions for improvement

Be specific and reference Tajweed rule names."""
            val result = container.aiService.ask(prompt)
            if (result.error != null && result.error != "stale") {
                _state.value = _state.value.copy(isAnalyzing = false, error = result.error)
            } else if (result.error == null) {
                _state.value = _state.value.copy(isAnalyzing = false, result = result.answer)
            }
        }
    }
}
