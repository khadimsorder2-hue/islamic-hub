package com.islamichub.app.ui.screens.zakat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Zakat calculation model based on classical Islamic fiqh.
 *
 * Nisab thresholds:
 *  - Gold nisab: 85 grams of gold (≈ 7.5 tola)
 *  - Silver nisab: 595 grams of silver (≈ 52.5 tola)
 *
 * Zakat due = 2.5% of total zakatable assets if total exceeds the
 * LOWER of the two nisab values (Hanafi school uses silver nisab
 * which is more beneficial to the poor).
 */
data class ZakatInput(
    val goldGrams: String = "",
    val goldTola: String = "",
    val silverGrams: String = "",
    val cashBDT: String = "",
    val cashForeign: String = "",
    val bankBalance: String = "",
    val businessInventoryValue: String = "",
    val receivableDebts: String = "",
    val investmentsValue: String = "",
    val goldPricePerGram: String = "",     // BDT per gram (24k)
    val silverPricePerGram: String = "",  // BDT per gram
    val liabilities: String = ""
)

data class ZakatResult(
    val goldValue: Double = 0.0,
    val silverValue: Double = 0.0,
    val cashTotal: Double = 0.0,
    val businessAssets: Double = 0.0,
    val receivables: Double = 0.0,
    val investments: Double = 0.0,
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val netZakatable: Double = 0.0,
    val goldNisab: Double = 0.0,
    val silverNisab: Double = 0.0,
    val isAboveNisab: Boolean = false,
    val zakatDue: Double = 0.0,
    val isCalculated: Boolean = false
)

data class ZakatUiState(
    val input: ZakatInput = ZakatInput(),
    val result: ZakatResult = ZakatResult(),
    val autoGoldPrice: String = "8,500",
    val autoSilverPrice: String = "95"
)

class ZakatViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(ZakatUiState())
    val uiState: StateFlow<ZakatUiState> = _uiState.asStateFlow()

    fun updateInput(transform: (ZakatInput) -> ZakatInput) {
        _uiState.update { it.copy(input = transform(it.input)) }
    }

    fun calculate() {
        val input = _uiState.value.input
        val goldPrice = parseNum(input.goldPricePerGram.ifBlank { _uiState.value.autoGoldPrice })
        val silverPrice = parseNum(input.silverPricePerGram.ifBlank { _uiState.value.autoSilverPrice })

        val goldGramsTotal = parseNum(input.goldGrams) + parseNum(input.goldTola) * 11.664
        val silverGramsTotal = parseNum(input.silverGrams)

        val goldValue = goldGramsTotal * goldPrice
        val silverValue = silverGramsTotal * silverPrice

        val cashTotal = parseNum(input.cashBDT) + parseNum(input.cashForeign) + parseNum(input.bankBalance)
        val businessAssets = parseNum(input.businessInventoryValue)
        val receivables = parseNum(input.receivableDebts)
        val investments = parseNum(input.investmentsValue)
        val liabilities = parseNum(input.liabilities)

        val totalAssets = goldValue + silverValue + cashTotal + businessAssets + receivables + investments
        val netZakatable = (totalAssets - liabilities).coerceAtLeast(0.0)

        val goldNisab = 85.0 * goldPrice
        val silverNisab = 595.0 * silverPrice
        // Use silver nisab (Hanafi school, more lenient to poor)
        val nisab = minOf(goldNisab, silverNisab)
        val isAboveNisab = netZakatable >= nisab
        val zakatDue = if (isAboveNisab) netZakatable * 0.025 else 0.0

        val result = ZakatResult(
            goldValue = goldValue,
            silverValue = silverValue,
            cashTotal = cashTotal,
            businessAssets = businessAssets,
            receivables = receivables,
            investments = investments,
            totalAssets = totalAssets,
            totalLiabilities = liabilities,
            netZakatable = netZakatable,
            goldNisab = goldNisab,
            silverNisab = silverNisab,
            isAboveNisab = isAboveNisab,
            zakatDue = zakatDue,
            isCalculated = true
        )
        _uiState.update { it.copy(result = result) }
    }

    fun reset() {
        _uiState.update { ZakatUiState() }
    }

    private fun parseNum(s: String): Double {
        return s.replace(",", "").replace(" ", "").trim().toDoubleOrNull() ?: 0.0
    }
}
