package com.islamichub.app.ui.screens.ai_scholar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AIService

data class AiScholarUiState(
    val messages: List<AIService.ChatMessage> = emptyList(),
    val isThinking: Boolean = false,
    val error: String? = null,
    val warning: String? = null,
    val apiKeyConfigured: Boolean = false,
    val provider: String = "gemini",
    val model: String = "gemini-2.5-flash"
)

class AiScholarViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AiScholarUiState())
    val state: StateFlow<AiScholarUiState> = _state.asStateFlow()

    init { loadConfig() }

    private fun loadConfig() {
        viewModelScope.launch {
            val apiKey = container.settingsRepository.aiApiKey.first()
            val baseUrl = container.settingsRepository.aiBaseUrl.first()
            val model = container.settingsRepository.aiModel.first()
            val provider = container.settingsRepository.aiProvider.first()
            container.aiService.updateConfig(
                AIService.Config(
                    apiKey = apiKey,
                    baseUrl = baseUrl,
                    model = model,
                    provider = provider
                )
            )
            _state.value = _state.value.copy(
                apiKeyConfigured = apiKey.isNotBlank(),
                provider = provider,
                model = model
            )
        }
    }

    fun sendQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            // Append user message
            val newMessages = _state.value.messages + AIService.ChatMessage("user", question)
            _state.value = _state.value.copy(
                messages = newMessages,
                isThinking = true,
                error = null,
                warning = null
            )

            // Build history (last 10 messages)
            val history = newMessages.dropLast(1).takeLast(10)

            // Call AI service
            val result = container.aiService.ask(question, history)

            if (result.error == "stale") {
                // Stale response — ignore
                return@launch
            }
            if (result.error != null) {
                _state.value = _state.value.copy(
                    isThinking = false,
                    error = result.error
                )
                return@launch
            }

            // Append AI response
            val finalMessages = newMessages + AIService.ChatMessage("assistant", result.answer)
            _state.value = _state.value.copy(
                messages = finalMessages,
                isThinking = false,
                warning = result.warning
            )
        }
    }

    fun sendQuickQuestion(question: String) {
        sendQuestion(question)
    }

    fun clearChat() {
        _state.value = AiScholarUiState(
            apiKeyConfigured = _state.value.apiKeyConfigured,
            provider = _state.value.provider,
            model = _state.value.model
        )
    }

    fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }
}
