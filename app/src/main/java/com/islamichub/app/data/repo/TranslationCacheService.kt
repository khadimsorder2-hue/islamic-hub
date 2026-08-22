package com.islamichub.app.data.repo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Offline cache for Quran.com API translations and tafsirs. */
class TranslationCacheService(private val context: Context) {
    private val gson = Gson()
    private val cacheDir by lazy { File(context.filesDir, "translation_cache").apply { mkdirs() } }

    data class CachedVerse(
        val surah: Int,
        val ayah: Int,
        val translations: Map<String, String>, // translator name → text
        val tafsirs: Map<String, String>,       // tafsir name → text
        val transliteration: String?,
        val fetchedAt: Long = System.currentTimeMillis()
    )

    fun getCachedVerse(surah: Int, ayah: Int): CachedVerse? {
        val file = File(cacheDir, "${surah}_$ayah.json")
        if (!file.exists()) return null
        return try {
            gson.fromJson(file.readText(), CachedVerse::class.java)
        } catch (_: Exception) { null }
    }

    suspend fun cacheVerse(verse: CachedVerse) = withContext(Dispatchers.IO) {
        val file = File(cacheDir, "${verse.surah}_${verse.ayah}.json")
        file.writeText(gson.toJson(verse))
    }

    suspend fun cacheSurah(surah: Int, verses: List<CachedVerse>) = withContext(Dispatchers.IO) {
        val dir = File(cacheDir, "surah_$surah")
        dir.mkdirs()
        verses.forEach { v ->
            val file = File(dir, "${v.ayah}.json")
            file.writeText(gson.toJson(v))
        }
    }

    fun isSurahCached(surah: Int, ayahCount: Int): Boolean {
        val dir = File(cacheDir, "surah_$surah")
        if (!dir.exists()) return false
        return (1..ayahCount).count { File(dir, "$it.json").exists() }
    }

    fun getCacheSize(): Long {
        return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun clearCache() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }
}