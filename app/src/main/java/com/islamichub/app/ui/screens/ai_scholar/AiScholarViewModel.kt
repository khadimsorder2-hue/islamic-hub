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
    val model: String = "gemini-2.5-flash",
    /** Set true if last AI answer came from cache (instant) */
    val lastAnswerFromCache: Boolean = false,
    /** Total cached entries (for display in cache button) */
    val cacheCount: Int = 0,
    /** Search results from web (if user asked for current events) */
    val webResults: List<WebSearchResult> = emptyList(),
    val isWebSearching: Boolean = false
)

/** Web search result (used when AI needs current info) */
data class WebSearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String
)

class AiScholarViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AiScholarUiState())
    val state: StateFlow<AiScholarUiState> = _state.asStateFlow()

    init {
        loadConfig()
        observeCache()
    }

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

    private fun observeCache() {
        viewModelScope.launch {
            container.aiCacheRepository.count.collect { count ->
                _state.value = _state.value.copy(cacheCount = count)
            }
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
                warning = null,
                lastAnswerFromCache = false,
                webResults = emptyList()
            )

            // Build history (last 10 messages)
            val history = newMessages.dropLast(1).takeLast(10)

            // Call AI service (auto-cache inside AIService.ask)
            val result = container.aiService.ask(question, history, cacheType = "scholar")

            if (result.error == "stale") {
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
                warning = result.warning,
                lastAnswerFromCache = result.fromCache
            )
        }
    }

    /**
     * Regenerate the last AI response — bypasses cache for a fresh answer.
     */
    fun regenerateLast() {
        val msgs = _state.value.messages
        if (msgs.isEmpty()) return
        // Find last user message
        val lastUserIdx = msgs.indexOfLast { it.role == "user" }
        if (lastUserIdx < 0) return
        val question = msgs[lastUserIdx].content
        // Truncate messages to remove the previous AI answer
        val truncated = msgs.subList(0, lastUserIdx + 1).toList()
        _state.value = _state.value.copy(messages = truncated)
        // Re-ask with useCache=false
        viewModelScope.launch {
            _state.value = _state.value.copy(isThinking = true, error = null, lastAnswerFromCache = false)
            val history = truncated.dropLast(1).takeLast(10)
            val result = container.aiService.ask(question, history, useCache = false, cacheType = "scholar")
            if (result.error != null && result.error != "stale") {
                _state.value = _state.value.copy(isThinking = false, error = result.error)
                return@launch
            }
            val final = truncated + AIService.ChatMessage("assistant", result.answer)
            _state.value = _state.value.copy(
                messages = final,
                isThinking = false,
                warning = result.warning,
                lastAnswerFromCache = false
            )
        }
    }

    /**
     * Trigger web search (placeholder for future integration — actual search
     * happens in AIService via a configured search provider).
     */
    fun performWebSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isWebSearching = true)
            // For now we just notify the AI to answer using its training data.
            // Real web search would be added in a future release via z-ai-web-dev-sdk.
            sendQuestion("🌐 ওয়েবে খুঁজে বের করো: $query")
            _state.value = _state.value.copy(isWebSearching = false)
        }
    }

    fun sendQuickQuestion(question: String) {
        sendQuestion(question)
    }

    fun clearChat() {
        _state.value = AiScholarUiState(
            apiKeyConfigured = _state.value.apiKeyConfigured,
            provider = _state.value.provider,
            model = _state.value.model,
            cacheCount = _state.value.cacheCount
        )
    }

    /** Clear all cached AI responses */
    fun clearAICache() {
        viewModelScope.launch {
            container.settingsRepository.clearAICache()
        }
    }

    fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }
}
