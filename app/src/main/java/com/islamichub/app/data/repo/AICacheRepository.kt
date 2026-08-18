package com.islamichub.app.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
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

private val Context.aiCacheStore: DataStore<Preferences> by preferencesDataStore(name = "ai_cache_prefs")

/**
 * AI response cache — persists all AI answers (Tafsir, Hadith, Scholar Q&A).
 *
 * Benefits:
 *  - Instant answer if user asks the same question again
 *  - Saves API quota (especially for free Gemini tier)
 *  - Works offline after first fetch
 *
 * Cache key = SHA-256 of normalized input (provider|model|prompt)
 * Cache value = AI answer + metadata (timestamp, tokens, source)
 *
 * User can clear all cache from Settings → AI → Clear Cache.
 */
data class CachedAIResponse(
    val cacheKey: String,
    val question: String,         // original prompt (for display in cache list)
    val answer: String,
    val provider: String,
    val model: String,
    val timestamp: Long,
    val type: String              // "scholar" | "tafsir" | "hadith_explanation" | "general"
)

class AICacheRepository(private val context: Context) {

    private val gson = Gson()
    private val KEY_CACHE = stringPreferencesKey("ai_cache_json")

    /** Get all cached responses (newest first) */
    val allEntries: Flow<List<CachedAIResponse>> = context.aiCacheStore.data.map { prefs ->
        prefs[KEY_CACHE]?.let { json ->
            try {
                val type = object : TypeToken<List<CachedAIResponse>>() {}.type
                gson.fromJson<List<CachedAIResponse>>(json, type)?.sortedByDescending { it.timestamp } ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    /** Total cached entries count (for Settings display) */
    val count: Flow<Int> = allEntries.map { it.size }

    /**
     * Look up a cached response by (provider, model, prompt).
     * Returns null if not cached.
     */
    suspend fun lookup(provider: String, model: String, prompt: String): CachedAIResponse? = withContext(Dispatchers.IO) {
        val key = buildKey(provider, model, prompt)
        val all = readList()
        all.find { it.cacheKey == key }
    }

    /**
     * Store a response in cache. Overwrites if same key exists.
     */
    suspend fun put(
        provider: String,
        model: String,
        prompt: String,
        answer: String,
        type: String = "general"
    ) = withContext(Dispatchers.IO) {
        context.aiCacheStore.edit { prefs ->
            val list = readList(prefs).toMutableList()
            val key = buildKey(provider, model, prompt)
            val entry = CachedAIResponse(
                cacheKey = key,
                question = prompt.take(200),
                answer = answer,
                provider = provider,
                model = model,
                timestamp = System.currentTimeMillis(),
                type = type
            )
            // Remove existing with same key
            list.removeAll { it.cacheKey == key }
            list.add(entry)
            // Cap at 500 entries (LRU-ish: keep newest)
            val capped = if (list.size > 500) list.sortedByDescending { it.timestamp }.take(500) else list
            prefs[KEY_CACHE] = gson.toJson(capped)
        }
    }

    /** Clear all cached AI responses */
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        context.aiCacheStore.edit { it.remove(KEY_CACHE) }
    }

    /** Delete a single cached entry by its key */
    suspend fun delete(cacheKey: String) = withContext(Dispatchers.IO) {
        context.aiCacheStore.edit { prefs ->
            val list = readList(prefs).toMutableList()
            list.removeAll { it.cacheKey == cacheKey }
            prefs[KEY_CACHE] = gson.toJson(list)
        }
    }

    private fun readList(prefs: Preferences): List<CachedAIResponse> {
        return prefs[KEY_CACHE]?.let { json ->
            try {
                val type = object : TypeToken<List<CachedAIResponse>>() {}.type
                gson.fromJson<List<CachedAIResponse>>(json, type) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } ?: emptyList()
    }

    private suspend fun readList(): List<CachedAIResponse> {
        val prefs = context.aiCacheStore.data.first()
        return readList(prefs)
    }

    /** Build a stable cache key — SHA-256 of provider|model|normalizedPrompt */
    private fun buildKey(provider: String, model: String, prompt: String): String {
        val normalized = prompt.trim().lowercase().replace("\\s+".toRegex(), " ")
        val raw = "$provider|$model|$normalized"
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
