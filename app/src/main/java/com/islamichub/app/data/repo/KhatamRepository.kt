package com.islamichub.app.data.repo

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.khatamStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "khatam_prefs")

data class KhatamProgress(
    val id: String,
    val startDate: String,
    val completedSurahs: List<Int>,        // list of surah numbers read
    val completedAyahs: MutableMap<Int, IntRange>, // surah -> ayah range completed
    val isComplete: Boolean = false,
    val completionDate: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Khatam (full Quran reading) tracker.
 * Tracks progress through all 114 surahs / 6236 ayahs.
 */
class KhatamRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY = stringPreferencesKey("khatam_progress_json")

    val currentKhatam: Flow<KhatamProgress?> = context.khatamStore.data.map { prefs ->
        prefs[KEY]?.let { json ->
            try { gson.fromJson(json, KhatamProgress::class.java) } catch (_: Exception) { null }
        }
    }

    val progressPercent: Flow<Float> = currentKhatam.map { k ->
        if (k == null) 0f
        else {
            // 6236 total ayahs in Quran
            val totalAyahs = k.completedAyahs.values.sumOf { (it.last - it.first + 1) }
            (totalAyahs.toFloat() / 6236f).coerceIn(0f, 1f)
        }
    }

    val completedSurahCount: Flow<Int> = currentKhatam.map { k ->
        k?.completedSurahs?.size ?: 0
    }

    suspend fun startNew() = withContext(Dispatchers.IO) {
        val today = today()
        val newKhatam = KhatamProgress(
            id = "khatam_$today",
            startDate = today,
            completedSurahs = emptyList(),
            completedAyahs = mutableMapOf()
        )
        context.khatamStore.edit { it[KEY] = gson.toJson(newKhatam) }
    }

    /**
     * Mark a surah as fully read.
     */
    suspend fun markSurahCompleted(surahNumber: Int, ayahCount: Int) = withContext(Dispatchers.IO) {
        context.khatamStore.edit { prefs ->
            val current = currentKhatam(prefs)
            if (current == null) return@edit
            val newCompleted = (current.completedSurahs + surahNumber).distinct()
            val newAyahs = current.completedAyahs.toMutableMap()
            newAyahs[surahNumber] = 1..ayahCount
            val isComplete = newCompleted.size == 114
            val updated = current.copy(
                completedSurahs = newCompleted,
                completedAyahs = newAyahs,
                isComplete = isComplete,
                completionDate = if (isComplete) today() else null
            )
            prefs[KEY] = gson.toJson(updated)
        }
    }

    /**
     * Mark a single ayah as read (progress tracking within a surah).
     */
    suspend fun markAyahRead(surahNumber: Int, ayahNumber: Int) = withContext(Dispatchers.IO) {
        context.khatamStore.edit { prefs ->
            val current = currentKhatam(prefs) ?: return@edit
            val newAyahs = current.completedAyahs.toMutableMap()
            val existing = newAyahs[surahNumber]
            newAyahs[surahNumber] = if (existing == null) {
                ayahNumber..ayahNumber
            } else {
                val start = minOf(existing.first, ayahNumber)
                val end = maxOf(existing.last, ayahNumber)
                start..end
            }
            val updated = current.copy(completedAyahs = newAyahs)
            prefs[KEY] = gson.toJson(updated)
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        context.khatamStore.edit { it.remove(KEY) }
    }

    private fun currentKhatam(prefs: Preferences): KhatamProgress? {
        return prefs[KEY]?.let { json ->
            try { gson.fromJson(json, KhatamProgress::class.java) } catch (_: Exception) { null }
        }
    }

    private fun today(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
