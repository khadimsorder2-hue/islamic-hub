package com.islamichub.app.data.repo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Tafsir repository — fetches tafsir per ayah from AlQuran.cloud API,
 * caches to local file system so it works offline once fetched.
 *
 * API: https://api.alquran.cloud/v1/ayah/{reference}/{edition}
 *   - reference: "surah:ayah" (e.g., "1:1") or global ayah number
 *   - edition: tafsir edition ID (e.g., "bn.mukhtasar")
 *
 * Available tafsir editions (see SettingsRepository.TafsirSource):
 *  - bn.mukhtasar           — Bangla Tafsir Mukhtasar
 *  - bn.taisirulquran       — Bangla Taisirul Quran
 *  - en.jalalayn            — English Tafsir Jalalayn
 *  - en.ibnkathir           — English Ibn Kathir (surah-level only)
 */
class TafsirRepository(
    private val context: Context
) {

    private val cacheDir: File by lazy {
        File(context.filesDir, "tafsir_cache").apply { mkdirs() }
    }

    /**
     * Fetch tafsir for a specific ayah.
     * Cache key: {edition}_{surah}_{ayah}.txt
     * Returns null if fetching fails AND no cache exists.
     */
    suspend fun getTafsir(
        surah: Int,
        ayah: Int,
        editionId: String
    ): String? = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, "${editionId}_${surah}_${ayah}.txt")

        // Try cache first
        if (cacheFile.exists()) {
            try {
                val cached = cacheFile.readText()
                if (cached.isNotBlank()) return@withContext cached
            } catch (_: Exception) { /* fall through */ }
        }

        // Otherwise fetch from API
        try {
            val url = "https://api.alquran.cloud/v1/ayah/${surah}:${ayah}/${editionId}"
            val response = fetchFromApi(url) ?: return@withContext null
            // Parse JSON response
            val tafsirText = parseAyahTafsir(response) ?: return@withContext null
            // Cache
            try { cacheFile.writeText(tafsirText) } catch (_: Exception) {}
            tafsirText
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fetch multiple ayahs' tafsir for a surah in one API call.
     * AlQuran.cloud: /v1/surah/{surah}/{edition}
     */
    suspend fun getSurahTafsir(
        surah: Int,
        editionId: String
    ): List<SurahAyahTafsir>? = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, "surah_${editionId}_${surah}.json")
        if (cacheFile.exists()) {
            try {
                val text = cacheFile.readText()
                val parsed = parseSurahResponse(text, editionId)
                if (parsed != null) return@withContext parsed
            } catch (_: Exception) { /* fall through */ }
        }

        try {
            val url = "https://api.alquran.cloud/v1/surah/${surah}/${editionId}"
            val text = fetchFromApi(url) ?: return@withContext null
            try { cacheFile.writeText(text) } catch (_: Exception) {}
            parseSurahResponse(text, editionId)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    suspend fun cacheSizeBytes(): Long = withContext(Dispatchers.IO) {
        cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    // ─── Internals ────────────────────────────────────────────────────────

    private fun fetchFromApi(url: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.setRequestProperty("User-Agent", "islamichub/1.0")
            conn.requestMethod = "GET"
            conn.connect()
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAyahTafsir(json: String): String? {
        return try {
            val gson = com.google.gson.Gson()
            val tree = gson.fromJson(json, com.google.gson.JsonObject::class.java)
            val data = tree?.getAsJsonObject("data") ?: return null
            data.get("text")?.asString
        } catch (_: Exception) { null }
    }

    private fun parseSurahResponse(text: String, editionId: String): List<SurahAyahTafsir>? {
        return try {
            val gson = com.google.gson.Gson()
            val tree = gson.fromJson(text, com.google.gson.JsonObject::class.java)
            val data = tree?.getAsJsonObject("data") ?: return null
            val ayahs = data.getAsJsonArray("ayahs") ?: return null
            val result = mutableListOf<SurahAyahTafsir>()
            for (i in 0 until ayahs.size()) {
                val a = ayahs[i].asJsonObject
                val numInSurah = a.get("numberInSurah")?.asInt ?: (i + 1)
                val tafsirText = a.get("text")?.asString ?: ""
                result.add(SurahAyahTafsir(
                    ayahNumber = numInSurah,
                    text = tafsirText,
                    editionId = editionId
                ))
            }
            result
        } catch (_: Exception) { null }
    }
}

data class SurahAyahTafsir(
    val ayahNumber: Int,
    val text: String,
    val editionId: String
)
