package com.islamichub.app.ui.screens.tasbih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.DhikrOption

data class TasbihUiState(
    val count: Int = 0,
    val total: Int = 0,
    val round: Int = 0,
    val dhikrOptions: List<DhikrOption> = emptyList(),
    val currentDhikrId: String = "subhanallah",
    val target: Int = 33,
    val justCompletedRound: Boolean = false
)

class TasbihViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(TasbihUiState())
    val state: StateFlow<TasbihUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val opts = container.duaRepository.dhikrOptions()
            container.tasbihRepository.dhikrId.collect { dhikrId ->
                val opt = opts.firstOrNull { it.id == dhikrId } ?: opts.first()
                _state.value = _state.value.copy(
                    dhikrOptions = opts,
                    currentDhikrId = dhikrId,
                    target = opt.defaultTarget
                )
            }
        }
        viewModelScope.launch {
            container.tasbihRepository.count.collect { count ->
                _state.value = _state.value.copy(count = count)
            }
        }
        viewModelScope.launch {
            container.tasbihRepository.total.collect { total ->
                _state.value = _state.value.copy(total = total)
            }
        }
        viewModelScope.launch {
            container.tasbihRepository.round.collect { round ->
                _state.value = _state.value.copy(round = round)
            }
        }
    }

    fun onDhikrChange(id: String) {
        viewModelScope.launch { container.tasbihRepository.setDhikr(id) }
    }

    fun onIncrement() {
        viewModelScope.launch {
            val target = _state.value.target
            container.tasbihRepository.increment()
            val completed = container.tasbihRepository.checkRoundComplete(target)
            if (completed) {
                _state.value = _state.value.copy(justCompletedRound = true)
            }
        }
    }

    fun onReset() {
        viewModelScope.launch { container.tasbihRepository.reset() }
    }

    fun onResetAll() {
        viewModelScope.launch { container.tasbihRepository.resetAll() }
    }

    fun clearRoundFlag() {
        _state.value = _state.value.copy(justCompletedRound = false)
    }
}
