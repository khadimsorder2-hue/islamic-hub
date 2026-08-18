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

private val Context.trackerStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "tracker_prefs")

/**
 * Daily tracker for: prayer completion, zikr count, Quran reading.
 * Stores a list of DayTracker entries keyed by date (YYYY-MM-DD).
 */
data class DayTracker(
    val date: String,            // YYYY-MM-DD
    val fajrDone: Boolean = false,
    val dhuhrDone: Boolean = false,
    val asrDone: Boolean = false,
    val maghribDone: Boolean = false,
    val ishaDone: Boolean = false,
    val zikrCount: Int = 0,
    val ayahsRead: Int = 0,
    val surahsRead: Int = 0,
    val hadithsRead: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun prayersDone(): Int = listOf(fajrDone, dhuhrDone, asrDone, maghribDone, ishaDone).count { it }
    fun isFullDay(): Boolean = prayersDone() == 5
}

class TrackerRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_DAYS = stringPreferencesKey("tracker_days_json")
    private val KEY_TODAY_READING = stringPreferencesKey("last_read_ayah_json")

    val days: Flow<List<DayTracker>> = context.trackerStore.data.map { prefs ->
        prefs[KEY_DAYS]?.let { json ->
            try {
                val type = object : TypeToken<List<DayTracker>>() {}.type
                gson.fromJson<List<DayTracker>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    val today: Flow<DayTracker> = days.map { list ->
        val t = today()
        list.find { it.date == t } ?: DayTracker(date = t)
    }

    val prayerStreak: Flow<Int> = days.map { computeStreak(it) }

    val totalZikr: Flow<Int> = days.map { list -> list.sumOf { it.zikrCount } }

    val totalAyahsRead: Flow<Int> = days.map { list -> list.sumOf { it.ayahsRead } }

    val totalHadithsRead: Flow<Int> = days.map { list -> list.sumOf { it.hadithsRead } }

    suspend fun togglePrayer(prayer: String) = withContext(Dispatchers.IO) {
        context.trackerStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val t = today()
            val idx = list.indexOfFirst { it.date == t }
            val todayEntry = if (idx >= 0) list[idx] else DayTracker(date = t)
            val updated = when (prayer) {
                "Fajr" -> todayEntry.copy(fajrDone = !todayEntry.fajrDone)
                "Dhuhr" -> todayEntry.copy(dhuhrDone = !todayEntry.dhuhrDone)
                "Asr" -> todayEntry.copy(asrDone = !todayEntry.asrDone)
                "Maghrib" -> todayEntry.copy(maghribDone = !todayEntry.maghribDone)
                "Isha" -> todayEntry.copy(ishaDone = !todayEntry.ishaDone)
                else -> todayEntry
            }
            if (idx >= 0) list[idx] = updated else list.add(updated)
            prefs[KEY_DAYS] = gson.toJson(list)
        }
    }

    suspend fun addZikr(count: Int = 1) = withContext(Dispatchers.IO) {
        context.trackerStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val t = today()
            val idx = list.indexOfFirst { it.date == t }
            val todayEntry = if (idx >= 0) list[idx] else DayTracker(date = t)
            val updated = todayEntry.copy(zikrCount = todayEntry.zikrCount + count)
            if (idx >= 0) list[idx] = updated else list.add(updated)
            prefs[KEY_DAYS] = gson.toJson(list)
        }
    }

    suspend fun recordAyahRead() = withContext(Dispatchers.IO) {
        context.trackerStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val t = today()
            val idx = list.indexOfFirst { it.date == t }
            val todayEntry = if (idx >= 0) list[idx] else DayTracker(date = t)
            val updated = todayEntry.copy(ayahsRead = todayEntry.ayahsRead + 1)
            if (idx >= 0) list[idx] = updated else list.add(updated)
            prefs[KEY_DAYS] = gson.toJson(list)
        }
    }

    suspend fun recordSurahRead() = withContext(Dispatchers.IO) {
        context.trackerStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val t = today()
            val idx = list.indexOfFirst { it.date == t }
            val todayEntry = if (idx >= 0) list[idx] else DayTracker(date = t)
            val updated = todayEntry.copy(surahsRead = todayEntry.surahsRead + 1)
            if (idx >= 0) list[idx] = updated else list.add(updated)
            prefs[KEY_DAYS] = gson.toJson(list)
        }
    }

    suspend fun recordHadithRead() = withContext(Dispatchers.IO) {
        context.trackerStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val t = today()
            val idx = list.indexOfFirst { it.date == t }
            val todayEntry = if (idx >= 0) list[idx] else DayTracker(date = t)
            val updated = todayEntry.copy(hadithsRead = todayEntry.hadithsRead + 1)
            if (idx >= 0) list[idx] = updated else list.add(updated)
            prefs[KEY_DAYS] = gson.toJson(list)
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        context.trackerStore.edit { it.remove(KEY_DAYS) }
    }

    private fun currentList(prefs: Preferences): List<DayTracker> {
        return prefs[KEY_DAYS]?.let { json ->
            try {
                val type = object : TypeToken<List<DayTracker>>() {}.type
                gson.fromJson<List<DayTracker>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    private fun computeStreak(list: List<DayTracker>): Int {
        if (list.isEmpty()) return 0
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sorted = list.sortedByDescending { it.date }
        var streak = 0
        var expectedDate = java.util.Calendar.getInstance()
        for (entry in sorted) {
            try {
                val entryDate = sdf.parse(entry.date) ?: continue
                val entryCal = java.util.Calendar.getInstance().apply { time = entryDate }
                // Check if entry is for expectedDate (today, then yesterday, etc.)
                val diff = ((expectedDate.timeInMillis - entryCal.timeInMillis) / 86_400_000L).toInt()
                if (diff == 0 && entry.isFullDay()) {
                    streak++
                    expectedDate.add(java.util.Calendar.DAY_OF_YEAR, -1)
                } else if (diff == 0 && !entry.isFullDay()) {
                    // Streak broken
                    break
                } else if (diff > 0) {
                    // Skip missing days — streak broken
                    break
                }
            } catch (_: Exception) { continue }
        }
        return streak
    }

    private fun today(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}

/**
 * Persistent "last read ayah" — for quick resume.
 */
data class LastRead(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val surahNameBn: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LastReadRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY = stringPreferencesKey("last_read_json")

    val lastRead: Flow<LastRead?> = context.trackerStore.data.map { prefs ->
        prefs[KEY]?.let { json ->
            try { gson.fromJson(json, LastRead::class.java) } catch (_: Exception) { null }
        }
    }

    suspend fun set(read: LastRead) = withContext(Dispatchers.IO) {
        context.trackerStore.edit { it[KEY] = gson.toJson(read) }
    }
}
