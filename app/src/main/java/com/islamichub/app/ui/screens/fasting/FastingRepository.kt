package com.islamichub.app.ui.screens.fasting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.fastingStore: DataStore<Preferences> by preferencesDataStore(name = "fasting_prefs")

data class FastEntry(
    val date: String,       // YYYY-MM-DD
    val type: FastType,
    val completed: Boolean = true,
    val note: String? = null
)

enum class FastType(val label: String, val bangla: String) {
    RAMADAN("Ramadan", "রমজান"),
    SUNNAH("Sunnah", "সুন্নত"),
    NAFL("Nafl", "নফল"),
    SHAWWAL("Shawwal 6", "শাওয়াল ৬"),
    ASHURA("Ashura", "আশুরা (১০ মুহররম)"),
    ARAFAH("Arafah", "আরাফাহ (৯ যিলহজ)"),
    MON_THU("Mon/Thu", "সোম-বৃহস্পতি"),
    QADA("Qada", "কাযা");

    fun isObligatory(): Boolean = this == RAMADAN || this == QADA
}

data class FastingStats(
    val totalFasts: Int = 0,
    val ramadanFasts: Int = 0,
    val naflFasts: Int = 0,
    val qadaFasts: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val thisMonthCount: Int = 0
) {
    val allNafl: Int get() = naflFasts
}

/**
 * Persistent fasting (roza) tracker.
 * Tracks all types of fasts — Ramadan, Sunnah, Nafl, Qada, special days.
 */
class FastingRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_ENTRIES = stringPreferencesKey("fasting_entries_json")
    private val KEY_STREAK = intPreferencesKey("fasting_streak")
    private val KEY_LONGEST = intPreferencesKey("fasting_longest_streak")

    val entries: Flow<List<FastEntry>> = context.fastingStore.data.map { prefs ->
        prefs[KEY_ENTRIES]?.let { json ->
            try {
                val type = object : TypeToken<List<FastEntry>>() {}.type
                gson.fromJson<List<FastEntry>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    val stats: Flow<FastingStats> = entries.map { list -> computeStats(list) }

    private suspend fun computeStats(list: List<FastEntry>): FastingStats {
        val total = list.count { it.completed }
        val ramadan = list.count { it.completed && it.type == FastType.RAMADAN }
        val nafl = list.count { it.completed && (it.type == FastType.NAFL || it.type == FastType.SUNNAH ||
                it.type == FastType.SHAWWAL || it.type == FastType.ASHURA ||
                it.type == FastType.ARAFAH || it.type == FastType.MON_THU) }
        val qada = list.count { it.completed && it.type == FastType.QADA }

        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val monthCount = list.count {
            it.completed && it.date.startsWith(currentMonth)
        }

        // Streak calculation — count consecutive days with at least 1 fast, going backwards from today
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dateSet = list.filter { it.completed }.map { it.date }.toSet()
        var streak = 0
        val cal = java.util.Calendar.getInstance()
        // If user has fasted today, count forward; else start from yesterday
        if (!dateSet.contains(today)) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        while (true) {
            val ds = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (dateSet.contains(ds)) {
                streak++
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else break
        }

        val longestStreakStored = context.fastingStore.data.first()[KEY_LONGEST] ?: 0

        val longestStreak = maxOf(streak, longestStreakStored)

        // Update longest if new record
        if (streak > longestStreakStored) {
            context.fastingStore.edit { it[KEY_LONGEST] = streak }
        }

        return FastingStats(
            totalFasts = total,
            ramadanFasts = ramadan,
            naflFasts = nafl,
            qadaFasts = qada,
            currentStreak = streak,
            longestStreak = longestStreak,
            thisMonthCount = monthCount
        )
    }

    suspend fun addFast(type: FastType, date: String = today(), note: String? = null) = withContext(Dispatchers.IO) {
        context.fastingStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            // Don't allow duplicate same-day same-type
            if (list.none { it.date == date && it.type == type }) {
                list.add(FastEntry(date = date, type = type, completed = true, note = note))
                prefs[KEY_ENTRIES] = gson.toJson(list)
            }
        }
    }

    suspend fun removeFast(date: String, type: FastType) = withContext(Dispatchers.IO) {
        context.fastingStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            list.removeAll { it.date == date && it.type == type }
            prefs[KEY_ENTRIES] = gson.toJson(list)
        }
    }

    suspend fun resetAll() = withContext(Dispatchers.IO) {
        context.fastingStore.edit { prefs ->
            prefs.remove(KEY_ENTRIES)
            prefs.remove(KEY_STREAK)
            prefs.remove(KEY_LONGEST)
        }
    }

    private fun currentList(prefs: Preferences): List<FastEntry> {
        return prefs[KEY_ENTRIES]?.let { json ->
            try {
                val type = object : TypeToken<List<FastEntry>>() {}.type
                gson.fromJson<List<FastEntry>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
