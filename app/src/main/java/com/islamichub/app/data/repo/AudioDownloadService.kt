package com.islamichub.app.data.repo

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Audio download service — downloads Quran audio for offline playback.
 * Saves to app's internal storage: filesDir/audio_cache/{reciter}_{surah}_{ayah}.mp3
 */
class AudioDownloadService(private val context: Context) {

    private val downloadDir: File by lazy {
        File(context.filesDir, "audio_cache").apply { mkdirs() }
    }

    data class DownloadResult(
        val success: Boolean,
        val filePath: String? = null,
        val error: String? = null,
        val progress: Int = 0
    )

    /**
     * Download a single ayah audio file.
     * Returns local file path if already downloaded.
     */
    suspend fun downloadAyah(
        reciterEditionId: String,
        surah: Int,
        ayah: Int,
        globalAyahNumber: Int
    ): DownloadResult = withContext(Dispatchers.IO) {
        val fileName = "${reciterEditionId}_${surah}_${ayah}.mp3"
        val localFile = File(downloadDir, fileName)

        // Check if already downloaded
        if (localFile.exists() && localFile.length() > 1000) {
            return@withContext DownloadResult(
                success = true,
                filePath = localFile.absolutePath
            )
        }

        // Download from CDN
        val url = "https://cdn.islamic.network/quran/audio/128/$reciterEditionId/$globalAyahNumber.mp3"
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.connect()

            if (conn.responseCode !in 200..299) {
                return@withContext DownloadResult(
                    success = false,
                    error = "HTTP ${conn.responseCode}"
                )
            }

            val inputStream = conn.inputStream
            val tempFile = File(downloadDir, "$fileName.tmp")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            // Atomic rename
            tempFile.renameTo(localFile)

            DownloadResult(
                success = true,
                filePath = localFile.absolutePath
            )
        } catch (e: Exception) {
            DownloadResult(success = false, error = e.message)
        }
    }

    /**
     * Download all ayahs of a surah for offline playback.
     */
    suspend fun downloadSurah(
        reciterEditionId: String,
        surah: Int,
        ayahCount: Int,
        onProgress: (Int, Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        var success = 0
        for (ayah in 1..ayahCount) {
            val globalNum = globalAyahNumber(surah, ayah)
            val result = downloadAyah(reciterEditionId, surah, ayah, globalNum)
            if (result.success) success++
            onProgress(ayah, ayahCount)
        }
        success == ayahCount
    }

    /**
     * Check if an ayah audio is downloaded offline.
     */
    fun isAyahDownloaded(reciterEditionId: String, surah: Int, ayah: Int): Boolean {
        val fileName = "${reciterEditionId}_${surah}_${ayah}.mp3"
        val file = File(downloadDir, fileName)
        return file.exists() && file.length() > 1000
    }

    /**
     * Check if a surah is fully downloaded.
     */
    fun isSurahDownloaded(reciterEditionId: String, surah: Int, ayahCount: Int): Boolean {
        for (ayah in 1..ayahCount) {
            if (!isAyahDownloaded(reciterEditionId, surah, ayah)) return false
        }
        return true
    }

    /**
     * Get local file path for an ayah if downloaded.
     */
    fun getLocalAyahPath(reciterEditionId: String, surah: Int, ayah: Int): String? {
        val fileName = "${reciterEditionId}_${surah}_${ayah}.mp3"
        val file = File(downloadDir, fileName)
        return if (file.exists() && file.length() > 1000) file.absolutePath else null
    }

    /**
     * Get total downloaded audio size in bytes.
     */
    fun getDownloadedSize(): Long {
        return downloadDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    /**
     * Clear all downloaded audio.
     */
    fun clearDownloads() {
        downloadDir.deleteRecursively()
        downloadDir.mkdirs()
    }

    private fun globalAyahNumber(surah: Int, ayah: Int): Int {
        val cumulative = intArrayOf(
            0, 7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99,
            128, 111, 110, 98, 135, 112, 78, 64, 77, 227, 93, 88, 69, 60, 34, 30,
            73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29, 18,
            45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12,
            30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36,
            25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11, 11,
            8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
        )
        var sum = 0
        for (i in 1 until surah) sum += cumulative[i]
        return sum + ayah
    }
}
