package com.islamichub.app.data.repo

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.bookmarkStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "bookmark_prefs")

data class Bookmark(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val surahNameBn: String,
    val arabicSnippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Persistent bookmark repository for Quran ayahs.
 * Single writer (DataStore.edit), single source of truth.
 */
class BookmarkRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_BOOKMARKS = stringPreferencesKey("bookmarks_json")

    val bookmarks: Flow<List<Bookmark>> = context.bookmarkStore.data.map { prefs ->
        prefs[KEY_BOOKMARKS]?.let { json ->
            try {
                val type = object : TypeToken<List<Bookmark>>() {}.type
                gson.fromJson<List<Bookmark>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    suspend fun isBookmarked(surah: Int, ayah: Int): Boolean = withContext(Dispatchers.IO) {
        val current = bookmarks.let { flow ->
            var v: List<Bookmark> = emptyList()
            flow.collect { v = it; return@collect }
            v
        }
        current.any { it.surahNumber == surah && it.ayahNumber == ayah }
    }

    suspend fun toggle(bookmark: Bookmark): Boolean = withContext(Dispatchers.IO) {
        var added = false
        context.bookmarkStore.edit { prefs ->
            val list = prefs[KEY_BOOKMARKS]?.let { json ->
                try {
                    val type = object : TypeToken<List<Bookmark>>() {}.type
                    gson.fromJson<List<Bookmark>>(json, type) ?: emptyList()
                } catch (_: Exception) { emptyList() }
            } ?: emptyList()

            val existing = list.find { it.surahNumber == bookmark.surahNumber && it.ayahNumber == bookmark.ayahNumber }
            val newList = if (existing != null) {
                added = false
                list - existing
            } else {
                added = true
                (list + bookmark).sortedByDescending { it.timestamp }
            }
            prefs[KEY_BOOKMARKS] = gson.toJson(newList)
        }
        added
    }

    suspend fun remove(surah: Int, ayah: Int) = withContext(Dispatchers.IO) {
        context.bookmarkStore.edit { prefs ->
            val list = prefs[KEY_BOOKMARKS]?.let { json ->
                try {
                    val type = object : TypeToken<List<Bookmark>>() {}.type
                    gson.fromJson<List<Bookmark>>(json, type) ?: emptyList()
                } catch (_: Exception) { emptyList() }
            } ?: emptyList()
            val newList = list.filterNot { it.surahNumber == surah && it.ayahNumber == ayah }
            prefs[KEY_BOOKMARKS] = gson.toJson(newList)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        context.bookmarkStore.edit { it.remove(KEY_BOOKMARKS) }
    }
}
