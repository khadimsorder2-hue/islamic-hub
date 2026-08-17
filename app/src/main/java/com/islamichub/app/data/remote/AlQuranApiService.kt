package com.islamichub.app.data.remote

import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * AlQuran API (alquran-api.pages.dev) — CORS-free, no API key, supports Bangla.
 *
 * Endpoints:
 *  - /api/quran/surah/{surah}?lang=bn — Full surah with Bangla translation + audio
 *  - /api/quran/surah/{surah}?lang=en — English
 *  - /api/quran/surah/{surah}?lang=ar — Arabic only
 *
 * Returns: {name, transliteration, translation, audio, verses: [{id, text, translation}]}
 *
 * Audio format:
 *   audio: {
 *     "1": {reciter: "Mishary Rashid Al-Afasy", url: "https://server8.mp3quran.net/afs/001.mp3"},
 *     "2": {reciter: "Abu Bakr Al-Shatri", url: "https://server11.mp3quran.net/shatri/001.mp3"},
 *     ...
 *   }
 */
class AlQuranApiService {

    companion object {
        const val BASE_URL = "https://alquran-api.pages.dev/api/quran"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val gson = com.google.gson.Gson()

    data class SurahWithAudio(
        val surahNumber: Int,
        val nameArabic: String,
        val nameTransliteration: String,
        val nameTranslation: String,
        val audioReciters: List<ReciterAudio>,
        val verses: List<VerseWithTranslation>
    )

    data class ReciterAudio(
        val reciterName: String,
        val audioUrl: String
    )

    data class VerseWithTranslation(
        val ayahNumber: Int,
        val arabicText: String,
        val translation: String
    )

    /**
     * Fetch a complete surah with Bangla translation and audio reciter list.
     */
    suspend fun fetchSurahWithAudio(
        surahNumber: Int,
        lang: String = "bn"
    ): Result<SurahWithAudio> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/surah/$surahNumber?lang=$lang"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "islamichub/1.0")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API error ${response.code}")
                )
            }

            val parsed = gson.fromJson(responseBody, JsonObject::class.java)
                ?: return@withContext Result.failure(Exception("Parse error"))

            val nameArabic = parsed.get("name")?.asString ?: ""
            val nameTranslit = parsed.get("transliteration")?.asString ?: ""
            val nameTranslation = parsed.get("translation")?.asString ?: ""

            // Parse audio reciters
            val reciters = mutableListOf<ReciterAudio>()
            parsed.getAsJsonObject("audio")?.entrySet()?.forEach { (_, value) ->
                val audioObj = value.asJsonObject
                reciters.add(ReciterAudio(
                    reciterName = audioObj.get("reciter")?.asString ?: "",
                    audioUrl = audioObj.get("url")?.asString ?: ""
                ))
            }

            // Parse verses
            val verses = mutableListOf<VerseWithTranslation>()
            parsed.getAsJsonArray("verses")?.forEach { verseElement ->
                val verseObj = verseElement.asJsonObject
                verses.add(VerseWithTranslation(
                    ayahNumber = verseObj.get("id")?.asInt ?: 0,
                    arabicText = verseObj.get("text")?.asString ?: "",
                    translation = verseObj.get("translation")?.asString ?: ""
                ))
            }

            Result.success(SurahWithAudio(
                surahNumber = surahNumber,
                nameArabic = nameArabic,
                nameTransliteration = nameTranslit,
                nameTranslation = nameTranslation,
                audioReciters = reciters,
                verses = verses
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch tafsir for a specific ayah from Tafsir API (CDN-based, CORS-free).
     * Uses: https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/{slug}/{surah}/{ayah}.json
     */
    suspend fun fetchTafsir(
        surah: Int,
        ayah: Int,
        tafsirSlug: String = "bn-tafisr-fathul-majid"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/$tafsirSlug/$surah/$ayah.json"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "islamichub/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Tafsir not found (${response.code})"))
            }

            val responseBody = response.body?.string()
            val parsed = gson.fromJson(responseBody, JsonObject::class.java)
            val tafsirText = parsed?.get("tafsir")?.asString
                ?: parsed?.get("text")?.asString
                ?: parsed?.get("content")?.asString
                ?: ""

            if (tafsirText.isBlank()) {
                Result.failure(Exception("Empty tafsir"))
            } else {
                Result.success(tafsirText)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
