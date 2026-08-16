package com.islamichub.app.ui.screens.qa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.AnsCategory
import com.islamichub.app.data.local.AnsQA

data class QaUiState(
    val categories: List<AnsCategory> = emptyList(),
    val isLoading: Boolean = true,
    val verifyingId: String? = null,
    val verificationResult: String? = null,
    val apiKeyConfigured: Boolean = false
)

class QaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QaUiState())
    val state: StateFlow<QaUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val data = container.contentRepository.loadAnsData()
                val apiKey = container.settingsRepository.aiApiKey.first()
                val baseUrl = container.settingsRepository.aiBaseUrl.first()
                val model = container.settingsRepository.aiModel.first()
                container.aiService.updateConfig(
                    com.islamichub.app.data.repo.AIService.Config(apiKey = apiKey, baseUrl = baseUrl, model = model)
                )
                _state.value = QaUiState(
                    categories = data.categories ?: emptyList(),
                    isLoading = false,
                    apiKeyConfigured = apiKey.isNotBlank()
                )
            } catch (e: Exception) {
                _state.value = QaUiState(isLoading = false)
            }
        }
    }

    fun verifyWithAi(qa: AnsQA) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                verifyingId = qa.id,
                verificationResult = null
            )
            val prompt = """Verify the following Islamic Q&A for accuracy. Check if the answer is consistent with authentic Islamic sources (Quran and Sahih Hadith). If the answer is correct, confirm with sources. If there are any issues or missing context, point them out.

Question: ${qa.question ?: qa.questionBn}
Answer: ${qa.answer ?: qa.answerBn}
Reference: ${qa.reference ?: "N/A"}

Provide verification in Bangla."""
            val result = container.aiService.ask(prompt)
            if (result.error != null && result.error != "stale") {
                _state.value = _state.value.copy(
                    verifyingId = null,
                    verificationResult = "Error: ${result.error}"
                )
            } else if (result.error == null) {
                _state.value = _state.value.copy(
                    verifyingId = null,
                    verificationResult = result.answer
                )
            }
        }
    }

    fun clearVerification() {
        _state.value = _state.value.copy(verificationResult = null)
    }
}
