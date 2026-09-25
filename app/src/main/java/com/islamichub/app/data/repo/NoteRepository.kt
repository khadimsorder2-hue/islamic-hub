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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.noteStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "notepad_prefs")

/**
 * v5.12.0 — A single user-created note (ColorNote style).
 * The note belongs to a category (Quran / Hadith / Thematic / Dua / …),
 * has one of 8 pastel colors, can be pinned to the top, and stores
 * creation + last-edited timestamps.
 */
data class Note(
    val id: String,
    val title: String = "",
    val content: String = "",
    val categoryId: String = "personal",
    val colorIndex: Int = 0,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Notepad repository — JSON list persisted in its own DataStore file
 * (same pattern as KhatamRepository / JamatTimeRepository).
 */
class NoteRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY = stringPreferencesKey("notes_json")

    /** All notes: pinned first, then most recently edited first. */
    val notes: Flow<List<Note>> = context.noteStore.data.map { prefs ->
        prefs[KEY]?.let { json ->
            try {
                val type = object : TypeToken<List<Note>>() {}.type
                val parsed = gson.fromJson<List<Note>>(json, type) ?: emptyList()
                parsed.sortedWith(
                    compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt }
                )
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    suspend fun get(id: String): Note? = withContext(Dispatchers.IO) {
        try {
            val prefs = context.noteStore.data.first()
            prefs[KEY]?.let { json ->
                val type = object : TypeToken<List<Note>>() {}.type
                val parsed = gson.fromJson<List<Note>>(json, type) ?: emptyList()
                parsed.find { it.id == id }
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Insert or update a note (matched by id). */
    suspend fun upsert(note: Note) = withContext(Dispatchers.IO) {
        context.noteStore.edit { prefs ->
            val current = parse(prefs[KEY])
            val updated = current.filterNot { it.id == note.id } + note
            prefs[KEY] = gson.toJson(updated)
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        context.noteStore.edit { prefs ->
            val current = parse(prefs[KEY])
            prefs[KEY] = gson.toJson(current.filterNot { it.id == id })
        }
    }

    suspend fun setPinned(id: String, pinned: Boolean) = withContext(Dispatchers.IO) {
        context.noteStore.edit { prefs ->
            val current = parse(prefs[KEY])
            prefs[KEY] = gson.toJson(current.map {
                if (it.id == id) it.copy(pinned = pinned, updatedAt = System.currentTimeMillis())
                else it
            })
        }
    }

    /** Full replace — used by backup restore. */
    suspend fun replaceAll(replacement: List<Note>) = withContext(Dispatchers.IO) {
        context.noteStore.edit { prefs ->
            prefs[KEY] = gson.toJson(replacement)
        }
    }

    private fun parse(json: String?): List<Note> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<Note>>() {}.type
            gson.fromJson<List<Note>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
