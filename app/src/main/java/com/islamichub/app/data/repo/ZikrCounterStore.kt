package com.islamichub.app.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * v5.18.0 — per-day adhkar (zikr) counter store, adopted from the Muslimeen
 * app's AdhkarCounterDto (adhkarId + count + date). One JSON blob keeps
 * {adhkarId: count} for TODAY only; the blob resets automatically when the
 * date rolls over, exactly like a daily tasbih counter.
 */
private val Context.zikrCounterStore: DataStore<Preferences> by preferencesDataStore(name = "zikr_counter_prefs")

class ZikrCounterStore(private val context: Context) {

    private data class Blob(val date: String, val counts: Map<String, Int>)

    private val gson = Gson()
    private val key = stringPreferencesKey("todays_counts")

    private fun today(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    private suspend fun readBlob(): Blob = withContext(Dispatchers.IO) {
        val prefs = context.zikrCounterStore.data.first()
        val raw = prefs[key]
        val parsed = if (raw != null) {
            try { gson.fromJson(raw, Blob::class.java) } catch (_: Exception) { null }
        } else null
        // date rollover → fresh counter
        if (parsed != null && parsed.date == today()) parsed else Blob(today(), emptyMap())
    }

    private suspend fun writeBlob(blob: Blob) = withContext(Dispatchers.IO) {
        context.zikrCounterStore.edit { prefs ->
            prefs[key] = gson.toJson(blob)
        }
    }

    /** Today's counts keyed by adhkarId. */
    suspend fun todays(): Map<Int, Int> =
        readBlob().counts.entries.associate { (k, v) -> (k.toIntOrNull() ?: 0) to v }.filterKeys { it != 0 }

    suspend fun increment(adhkarId: Int) {
        val blob = readBlob()
        val next = blob.counts.toMutableMap()
        next[adhkarId.toString()] = (next[adhkarId.toString()] ?: 0) + 1
        writeBlob(blob.copy(counts = next))
    }

    suspend fun reset(adhkarId: Int) {
        val blob = readBlob()
        val next = blob.counts.toMutableMap()
        next.remove(adhkarId.toString())
        writeBlob(blob.copy(counts = next))
    }

    suspend fun resetAll() = writeBlob(Blob(today(), emptyMap()))
}
