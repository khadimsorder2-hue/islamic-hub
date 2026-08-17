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
    val categories: List<QaCategory> = emptyList(),
    val isLoading: Boolean = true,
    val verifyingId: String? = null,
    val verificationResult: String? = null,
    val apiKeyConfigured: Boolean = false
)

data class QaCategory(
    val id: String,
    val name: String,
    val icon: String?,
    val color: String?,
    val items: List<QaItem>
)

data class QaItem(
    val id: String,
    val question: String,
    val answer: String,
    val reference: String?,
    val arabic: String?
)

class QaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(QaUiState())
    val state: StateFlow<QaUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val ansData = container.contentRepository.loadAnsData()
                val apiKey = container.settingsRepository.aiApiKey.first()
                val baseUrl = container.settingsRepository.aiBaseUrl.first()
                val model = container.settingsRepository.aiModel.first()
                container.aiService.updateConfig(
                    com.islamichub.app.data.repo.AIService.Config(apiKey = apiKey, baseUrl = baseUrl, model = model)
                )

                val categories = ansData.toList().map { (key, cat) ->
                    QaCategory(
                        id = key,
                        name = cat.name ?: key,
                        icon = cat.icon,
                        color = cat.color,
                        items = (cat.questions ?: emptyList()).mapIndexed { idx, qa ->
                            QaItem(
                                id = "${key}_$idx",
                                question = qa.question ?: "",
                                answer = qa.answer ?: "",
                                reference = qa.reference,
                                arabic = qa.arabic
                            )
                        }
                    )
                }

                _state.value = QaUiState(
                    categories = categories,
                    isLoading = false,
                    apiKeyConfigured = apiKey.isNotBlank()
                )
            } catch (e: Exception) {
                _state.value = QaUiState(isLoading = false)
            }
        }
    }

    fun verifyWithAi(item: QaItem) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                verifyingId = item.id,
                verificationResult = null
            )
            val prompt = """নিচের ইসলামিক প্রশ্নোত্তরটি যাচাই করুন। উত্তরটি কুরআন ও সহীহ হাদিসের সাথে সঙ্গতিপূর্ণ কিনা নিশ্চিত করুন।

প্রশ্ন: ${item.question}
উত্তর: ${item.answer}
${item.reference?.let { "সূত্র: $it" } ?: ""}

বাংলায় যাচাই করুন।"""
            val result = container.aiService.ask(prompt)
            if (result.error != null && result.error != "stale") {
                _state.value = _state.value.copy(
                    verifyingId = null,
                    verificationResult = "ত্রুটি: ${result.error}"
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
