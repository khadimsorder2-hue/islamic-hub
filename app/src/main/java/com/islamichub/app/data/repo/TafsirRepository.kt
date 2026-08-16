package com.islamichub.app.data.repo

import android.content.Context
import com.islamichub.app.data.remote.AladhanApi
import com.islamichub.app.data.remote.AladhanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Tafsir repository — fetches tafsir per ayah from AlQuran.cloud API,
 * caches to local file system so it works offline once fetched.
 *
 * Supports multiple sources via SettingsRepository.TafsirSource.
 */
class TafsirRepository(
    private val context: Context,
    private val api: AladhanApi
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
                return@withContext cacheFile.readText()
            } catch (_: Exception) { /* fall through */ }
        }

        // Otherwise fetch from API
        try {
            // Use AlQuran.cloud ayah endpoint
            // https://api.alquran.cloud/v1/ayah/{surah}:{ayah}/{edition}
            val url = "https://api.alquran.cloud/v1/ayah/${surah}:${ayah}/${editionId}/quran-uthmani"
            val response = fetchFromApi(url)
            if (response != null) {
                try {
                    cacheFile.writeText(response)
                } catch (_: Exception) { /* best-effort */ }
            }
            response
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
        // Check cache first
        val cacheFile = File(cacheDir, "surah_${editionId}_${surah}.json")
        if (cacheFile.exists()) {
            try {
                val text = cacheFile.readText()
                val parsed = parseSurahResponse(text, editionId)
                if (parsed != null) return@withContext parsed
            } catch (_: Exception) { /* fall through */ }
        }

        // Fetch
        try {
            val url = "https://api.alquran.cloud/v1/surah/${surah}/${editionId}"
            val text = fetchRawFromApi(url) ?: return@withContext null
            // Cache
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

    private suspend fun fetchFromApi(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.setRequestProperty("User-Agent", "islamichub/1.0")
            conn.connect()
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (_: Exception) { null }
    }

    private suspend fun fetchRawFromApi(url: String): String? = fetchFromApi(url)

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
