package com.islamichub.app.data.repo

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.qadaStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "qada_prefs")

data class QadaEntry(
    val prayer: String,         // Fajr, Dhuhr, Asr, Maghrib, Isha
    val date: String,           // YYYY-MM-DD
    val count: Int = 1,         // how many of this prayer missed on that date
    val completed: Int = 0,     // how many have been made up
    val timestamp: Long = System.currentTimeMillis()
)

data class QadaSummary(
    val fajr: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0
) {
    val total: Int get() = fajr + dhuhr + asr + maghrib + isha
    fun asMap(): Map<String, Int> = mapOf(
        "Fajr" to fajr, "Dhuhr" to dhuhr, "Asr" to asr,
        "Maghrib" to maghrib, "Isha" to isha
    )
}

/**
 * Persistent Qada (missed prayer) tracker.
 * Single-writer: all writes go through [edit].
 */
class QadaRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_ENTRIES = stringPreferencesKey("qada_entries_json")

    val entries: Flow<List<QadaEntry>> = context.qadaStore.data.map { prefs ->
        prefs[KEY_ENTRIES]?.let { json ->
            try {
                val type = object : TypeToken<List<QadaEntry>>() {}.type
                gson.fromJson<List<QadaEntry>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    val summary: Flow<QadaSummary> = entries.map { list ->
        val map = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").associateWith { p ->
            list.filter { it.prayer == p }.sumOf { (it.count - it.completed).coerceAtLeast(0) }
        }
        QadaSummary(
            fajr = map["Fajr"] ?: 0,
            dhuhr = map["Dhuhr"] ?: 0,
            asr = map["Asr"] ?: 0,
            maghrib = map["Maghrib"] ?: 0,
            isha = map["Isha"] ?: 0
        )
    }

    suspend fun addMissed(prayer: String, count: Int = 1, date: String = today()) = withContext(Dispatchers.IO) {
        context.qadaStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val existing = list.find { it.prayer == prayer && it.date == date }
            if (existing != null) {
                list[list.indexOf(existing)] = existing.copy(count = existing.count + count)
            } else {
                list.add(QadaEntry(prayer = prayer, date = date, count = count))
            }
            prefs[KEY_ENTRIES] = gson.toJson(list)
        }
    }

    suspend fun markCompleted(prayer: String, count: Int = 1) = withContext(Dispatchers.IO) {
        context.qadaStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            // Find oldest entry for this prayer that has outstanding count
            val sorted = list.filter { it.prayer == prayer && (it.count - it.completed) > 0 }
                .sortedBy { it.timestamp }
            var remaining = count
            for (entry in sorted) {
                if (remaining <= 0) break
                val outstanding = entry.count - entry.completed
                val toComplete = minOf(outstanding, remaining)
                list[list.indexOf(entry)] = entry.copy(completed = entry.completed + toComplete)
                remaining -= toComplete
            }
            prefs[KEY_ENTRIES] = gson.toJson(list)
        }
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        context.qadaStore.edit { it.remove(KEY_ENTRIES) }
    }

    private fun currentList(prefs: Preferences): List<QadaEntry> {
        return prefs[KEY_ENTRIES]?.let { json ->
            try {
                val type = object : TypeToken<List<QadaEntry>>() {}.type
                gson.fromJson<List<QadaEntry>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    private fun today(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
