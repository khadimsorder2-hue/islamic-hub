package com.islamichub.app.ui.screens.namaz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.FullNamazCategory
import com.islamichub.app.data.local.FullNamazData
import com.islamichub.app.data.local.FullNamazPrayer
import com.islamichub.app.data.local.FullNamazStep
import com.islamichub.app.data.local.ExtendedNamazItem
import com.islamichub.app.data.local.FullNamazAssetSource

data class NamazShikkhaUiState(
    val isLoading: Boolean = true,
    val selectedMadhhab: String = "হানাফী",
    val selectedGender: String = "পুরুষ",
    val fullData: FullNamazData? = null,
    val extendedNamaz: List<ExtendedNamazItem> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val selectedPrayerIndex: Int = 0,
    val apiKeyConfigured: Boolean = false
)

class NamazShikkhaViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(NamazShikkhaUiState())
    val state: StateFlow<NamazShikkhaUiState> = _state.asStateFlow()

    private val assetSource: FullNamazAssetSource by lazy {
        FullNamazAssetSource(container.let { it.run { android.app.Application() } })
    }

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val ctx = (container.javaClass.getDeclaredField("context").apply { isAccessible = true }
                    .get(container) as android.content.Context)
                val source = FullNamazAssetSource(ctx)
                val fullData = source.loadFullNamazData()
                val extended = source.loadExtendedNamazData()

                val apiKey = container.settingsRepository.aiApiKey.first()
                _state.value = NamazShikkhaUiState(
                    isLoading = false,
                    selectedMadhhab = fullData.defaultMadhhab ?: "হানাফী",
                    selectedGender = fullData.defaultGender ?: "পুরুষ",
                    fullData = fullData,
                    extendedNamaz = extended.additionalNamaz ?: emptyList(),
                    apiKeyConfigured = apiKey.isNotBlank()
                )
            } catch (e: Exception) {
                _state.value = NamazShikkhaUiState(isLoading = false)
            }
        }
    }

    fun setMadhhab(m: String) { _state.value = _state.value.copy(selectedMadhhab = m) }
    fun setGender(g: String) { _state.value = _state.value.copy(selectedGender = g) }
    fun setCategoryIndex(i: Int) { _state.value = _state.value.copy(selectedCategoryIndex = i) }
    fun setPrayerIndex(i: Int) { _state.value = _state.value.copy(selectedPrayerIndex = i) }

    /**
     * Get the step-by-step sequence for a prayer.
     * Combines: niyyah → sana → taawwuz → tasmiah → surah_fatiha → surah_xxx
     * → takbir → ruku → qawma → sajda → jalsa → sajda → (repeat for rakat 2)
     * → tashahhud → darood → dua_masura → salam
     */
    fun getStepsForPrayer(prayer: FullNamazPrayer): List<FullNamazStep> {
        val data = _state.value.fullData ?: return emptyList()
        val steps = data.commonSteps ?: return emptyList()
        val result = mutableListOf<FullNamazStep>()

        // Niyyah
        prayer.niyyah?.let { steps[it]?.let { s -> result.add(s) } }

        val rakats = prayer.totalRakats ?: 2
        for (rakat in 1..rakats) {
            // Takbir tahrima (only first rakat)
            if (rakat == 1) {
                steps["takbir_tahrima"]?.let { result.add(it) }
                steps["sana"]?.let { result.add(it) }
            }
            // Taawwuz + tasmiah
            steps["taawwuz"]?.let { result.add(it) }
            steps["tasmiah"]?.let { result.add(it) }
            // Surah Fatiha
            steps["surah_fatiha"]?.let { result.add(it) }
            // Another surah
            if (rakat == 1) {
                steps["surah_ikhlas"]?.let { result.add(it) }
            }
            // Ruku
            steps["ruku"]?.let { result.add(it) }
            // Qawma
            steps["qawma"]?.let { result.add(it) }
            // Sajda
            steps["sajda"]?.let { result.add(it) }
            // Jalsa
            steps["jalsa"]?.let { result.add(it) }
            // Sajda again
            steps["sajda"]?.let { result.add(it) }
        }

        // Final sitting
        steps["tashahhud"]?.let { result.add(it) }
        steps["darood"]?.let { result.add(it) }
        steps["dua_masura"]?.let { result.add(it) }
        steps["salam"]?.let { result.add(it) }

        // Witr special: add qunoot before ruku in 3rd rakat
        if (prayer.id.contains("witr")) {
            val qunoot = steps["dua_qunoot"]
            if (qunoot != null) {
                // Insert before the 3rd rakat's ruku
                val insertPos = result.indexOfFirst { it.nameBn?.contains("রুকু") == true } + 2
                if (insertPos in 0..result.size) {
                    result.add(insertPos, qunoot)
                }
            }
        }

        return result
    }
}
