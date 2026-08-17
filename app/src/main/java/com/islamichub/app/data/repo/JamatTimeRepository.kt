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

private val Context.jamatStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "jamat_prefs")

data class JamatTime(
    val prayerName: String,  // Fajr, Dhuhr, Asr, Maghrib, Isha, Jummah
    val jamatTime: String,   // HH:mm
    val mosqueName: String = "",
    val enabled: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Repository for custom jamat (congregation) times.
 * User can set local mosque jamat times + get notifications.
 */
class JamatTimeRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_JAMAT = stringPreferencesKey("jamat_times_json")
    private val KEY_ADHAN_SOUND = stringPreferencesKey("adhan_sound")

    val jamatTimes: Flow<List<JamatTime>> = context.jamatStore.data.map { prefs ->
        prefs[KEY_JAMAT]?.let { json ->
            try {
                val type = object : TypeToken<List<JamatTime>>() {}.type
                gson.fromJson<List<JamatTime>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    val adhanSound: Flow<String> = context.jamatStore.data.map {
        it[KEY_ADHAN_SOUND] ?: "azan2.mp3"
    }

    suspend fun setJamatTime(jamat: JamatTime) = withContext(Dispatchers.IO) {
        context.jamatStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            val existing = list.indexOfFirst { it.prayerName == jamat.prayerName }
            if (existing >= 0) {
                list[existing] = jamat
            } else {
                list.add(jamat)
            }
            prefs[KEY_JAMAT] = gson.toJson(list)
        }
    }

    suspend fun removeJamatTime(prayerName: String) = withContext(Dispatchers.IO) {
        context.jamatStore.edit { prefs ->
            val list = currentList(prefs).toMutableList()
            list.removeAll { it.prayerName == prayerName }
            prefs[KEY_JAMAT] = gson.toJson(list)
        }
    }

    suspend fun setAdhanSound(soundFile: String) = withContext(Dispatchers.IO) {
        context.jamatStore.edit { it[KEY_ADHAN_SOUND] = soundFile }
    }

    private fun currentList(prefs: Preferences): List<JamatTime> {
        return prefs[KEY_JAMAT]?.let { json ->
            try {
                val type = object : TypeToken<List<JamatTime>>() {}.type
                gson.fromJson<List<JamatTime>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }
}
