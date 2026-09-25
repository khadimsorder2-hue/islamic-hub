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

/** One completed khatam kept permanently in the history log. */
data class KhatamHistoryEntry(
    val id: String,
    val startDate: String,
    val completionDate: String,
    val daysTaken: Int
)

/** Grouping granularity for the Khatam history dashboard. */
enum class KhatamPeriod { DAY, WEEK, MONTH, YEAR }

data class KhatamPeriodStat(
    val periodKey: String,
    val label: String,
    val khatamsCompleted: Int
)

/**
 * Khatam (full Quran reading) tracker.
 * Tracks progress through all 114 surahs / 6236 ayahs.
 */
class KhatamRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY = stringPreferencesKey("khatam_progress_json")
    private val KEY_HISTORY = stringPreferencesKey("khatam_history_json")

    val currentKhatam: Flow<KhatamProgress?> = context.khatamStore.data.map { prefs ->
        prefs[KEY]?.let { json ->
            try { gson.fromJson(json, KhatamProgress::class.java) } catch (_: Exception) { null }
        }
    }

    /** Every khatam ever completed — this used to be lost forever on reset(). */
    val history: Flow<List<KhatamHistoryEntry>> = context.khatamStore.data.map { prefs ->
        prefs[KEY_HISTORY]?.let { json ->
            try {
                val type = object : TypeToken<List<KhatamHistoryEntry>>() {}.type
                gson.fromJson<List<KhatamHistoryEntry>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    val totalKhatamsCompleted: Flow<Int> = history.map { it.size }

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
            val completionDate = if (isComplete) today() else null
            val updated = current.copy(
                completedSurahs = newCompleted,
                completedAyahs = newAyahs,
                isComplete = isComplete,
                completionDate = completionDate
            )
            prefs[KEY] = gson.toJson(updated)

            // BUGFIX: a completed khatam used to live only in KEY, so calling
            // reset() (or starting a new khatam) permanently erased any record
            // that it ever happened. Now every completion is appended to a
            // permanent history log first.
            if (isComplete && completionDate != null) {
                val historyType = object : TypeToken<List<KhatamHistoryEntry>>() {}.type
                val existingHistory: List<KhatamHistoryEntry> = prefs[KEY_HISTORY]?.let {
                    try { gson.fromJson<List<KhatamHistoryEntry>>(it, historyType) ?: emptyList() } catch (_: Exception) { emptyList() }
                } ?: emptyList()

                val daysTaken = try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val start = sdf.parse(current.startDate)
                    val end = sdf.parse(completionDate)
                    if (start != null && end != null) (((end.time - start.time) / 86_400_000L) + 1).toInt() else 1
                } catch (_: Exception) { 1 }

                val newHistory = existingHistory + KhatamHistoryEntry(
                    id = current.id,
                    startDate = current.startDate,
                    completionDate = completionDate,
                    daysTaken = daysTaken.coerceAtLeast(1)
                )
                prefs[KEY_HISTORY] = gson.toJson(newHistory)
            }
        }
    }

    /** Resets only the *in-progress* khatam. History of completed khatams is preserved. */
    suspend fun reset() = withContext(Dispatchers.IO) {
        context.khatamStore.edit { it.remove(KEY) }
    }

    /** Permanently erases the completed-khatam history too. Separate, explicit, and rarely what the UI should call. */
    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        context.khatamStore.edit { it.remove(KEY_HISTORY) }
    }

    /** History dashboard: completed khatams grouped into day/week/month/year buckets, most recent first. */
    fun statsFor(period: KhatamPeriod): Flow<List<KhatamPeriodStat>> = history.map { list ->
        val cal = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        fun keyFor(dateStr: String): Pair<String, String>? {
            val date = try { sdf.parse(dateStr) } catch (_: Exception) { null } ?: return null
            cal.time = date
            return when (period) {
                KhatamPeriod.DAY -> dateStr to dateStr
                KhatamPeriod.WEEK -> {
                    val year = cal.get(java.util.Calendar.YEAR)
                    val week = cal.get(java.util.Calendar.WEEK_OF_YEAR)
                    "%d-W%02d".format(year, week) to "সপ্তাহ %d, %d".format(week, year)
                }
                KhatamPeriod.MONTH -> {
                    java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(date) to
                        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(date)
                }
                KhatamPeriod.YEAR -> {
                    val year = cal.get(java.util.Calendar.YEAR).toString()
                    year to year
                }
            }
        }

        list.mapNotNull { entry -> keyFor(entry.completionDate)?.let { (key, label) -> key to label } }
            .groupBy({ it.first }, { it.second })
            .map { (key, labels) -> KhatamPeriodStat(periodKey = key, label = labels.first(), khatamsCompleted = labels.size) }
            .sortedByDescending { it.periodKey }
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
