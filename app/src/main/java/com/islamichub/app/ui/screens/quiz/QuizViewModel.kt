package com.islamichub.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val currentScreen: QuizScreen = QuizScreen.Category,
    val selectedCategory: QuizCategory? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOption: Int? = null,
    val isAnswered: Boolean = false,
    val score: Int = 0,
    val totalAnswered: Int = 0,
    val showExplanation: Boolean = false,
    val totalScore: Int = 0,
    val totalAttempts: Int = 0
)

enum class QuizScreen { Category, Question, Result }

class QuizViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val KEY_TOTAL_SCORE = "quiz_total_score"
    private val KEY_TOTAL_ATTEMPTS = "quiz_total_attempts"

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val score = container.context.getSharedPreferences("quiz_stats", android.content.Context.MODE_PRIVATE).getInt(KEY_TOTAL_SCORE, 0)
            val attempts = container.context.getSharedPreferences("quiz_stats", android.content.Context.MODE_PRIVATE).getInt(KEY_TOTAL_ATTEMPTS, 0)
            _uiState.update { it.copy(totalScore = score, totalAttempts = attempts) }
        }
    }

    fun selectCategory(category: QuizCategory) {
        _uiState.update {
            it.copy(
                currentScreen = QuizScreen.Question,
                selectedCategory = category,
                currentQuestionIndex = 0,
                selectedOption = null,
                isAnswered = false,
                score = 0,
                totalAnswered = 0,
                showExplanation = false
            )
        }
    }

    fun selectOption(index: Int) {
        if (_uiState.value.isAnswered) return
        _uiState.update { it.copy(selectedOption = index) }
    }

    fun confirmAnswer() {
        val state = _uiState.value
        val correctIdx = state.selectedCategory?.questions?.get(state.currentQuestionIndex)?.correctIndex ?: return
        val isCorrect = state.selectedOption == correctIdx
        _uiState.update {
            it.copy(
                isAnswered = true,
                score = if (isCorrect) it.score + 1 else it.score,
                totalAnswered = it.totalAnswered + 1,
                showExplanation = true
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val category = state.selectedCategory ?: return
        if (state.currentQuestionIndex + 1 >= category.questions.size) {
            // Save stats and go to result
            val newTotal = state.totalScore + state.score
            val newAttempts = state.totalAttempts + 1
            container.context.getSharedPreferences("quiz_stats", android.content.Context.MODE_PRIVATE)
                .edit().putInt(KEY_TOTAL_SCORE, newTotal).putInt(KEY_TOTAL_ATTEMPTS, newAttempts).apply()
            _uiState.update {
                it.copy(
                    currentScreen = QuizScreen.Result,
                    totalScore = newTotal,
                    totalAttempts = newAttempts
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedOption = null,
                    isAnswered = false,
                    showExplanation = false
                )
            }
        }
    }

    fun restartQuiz() {
        val category = _uiState.value.selectedCategory ?: return
        selectCategory(category)
    }

    fun backToCategories() {
        _uiState.update {
            it.copy(
                currentScreen = QuizScreen.Category,
                selectedCategory = null,
                currentQuestionIndex = 0,
                selectedOption = null,
                isAnswered = false,
                score = 0,
                totalAnswered = 0,
                showExplanation = false
            )
        }
    }
}
