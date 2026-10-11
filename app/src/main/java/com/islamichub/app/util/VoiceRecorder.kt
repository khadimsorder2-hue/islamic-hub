package com.islamichub.app.util

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * v5.16.0 — Tajweed Voice AI recorder (Android port of the web
 * TajbeedCheckerService MediaRecorder flow).
 *
 * Web version: navigator.mediaDevices.getUserMedia → MediaRecorder → Blob
 * (30-second cap with a live timer).
 *
 * Android version: AudioRecord captures 16 kHz / mono / 16-bit PCM, which we
 * wrap in a RIFF/WAV header on stop. WAV is one of the audio MIME types the
 * Gemini inline-data endpoint officially supports, so no re-encoding step is
 * needed — the bytes can go straight into an inline_data part as audio/wav.
 *
 * The recorder also reports RMS amplitude (0f..1f) while recording so the UI
 * can draw a live pulsing mic indicator, mirroring the web modal's animated
 * "settings_voice" state.
 */
class VoiceRecorder {

    data class Recording(val wavBytes: ByteArray, val durationMs: Long) {
        val mimeType: String get() = "audio/wav"
    }

    companion object {
        const val SAMPLE_RATE = 16000
        const val MAX_DURATION_SEC = 30 // parity with web RECORDING_LIMIT_SEC

        /** Mono 16-bit PCM bytes per second: 16000 samples × 2 bytes. */
        private const val BYTES_PER_SEC = SAMPLE_RATE * 2
    }

    private val isRecording = AtomicBoolean(false)
    private var recordJob: Job? = null

    /** Live output level 0f..1f, polled by the UI ~10×/s for the mic pulse. */
    @Volatile
    var amplitude: Float = 0f
        private set

    /** Elapsed recording milliseconds, updated by the recording loop. */
    @Volatile
    var elapsedMs: Long = 0L
        private set

    private var audioRecord: AudioRecord? = null

    @Volatile
    private var lastRecording: Recording? = null

    val recording: Boolean get() = isRecording.get()

    @SuppressLint("MissingPermission") // caller checks RECORD_AUDIO first
    fun start(scope: CoroutineScope, onLimitReached: (() -> Unit)? = null): Boolean {
        if (isRecording.get()) return false
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) return false
        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf.coerceAtLeast(SAMPLE_RATE) // ≥ 0.5s buffer
            )
        } catch (_: Exception) {
            return false
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return false
        }
        audioRecord = record
        isRecording.set(true)
        amplitude = 0f
        elapsedMs = 0L
        lastRecording = null
        recordJob = scope.launch(Dispatchers.IO) {
            val pcm = ByteArrayOutputStream(BYTES_PER_SEC * MAX_DURATION_SEC)
            val buffer = ShortArray(SAMPLE_RATE / 10) // 100 ms chunks
            record.startRecording()
            val startedAt = System.currentTimeMillis()
            var hitLimit = false
            try {
                while (isRecording.get()) {
                    elapsedMs = System.currentTimeMillis() - startedAt
                    if (elapsedMs >= MAX_DURATION_SEC * 1000L) {
                        hitLimit = true
                        isRecording.set(false)
                        break
                    }
                    // 3-arg ShortArray overload: (data, offsetInShorts, sizeInShorts)
                    // — the 2-arg form resolves to ByteBuffer and fails to compile
                    val read = record.read(buffer, 0, buffer.size)
                    if (read <= 0) continue
                    // RMS → 0..1 amplitude for the live mic pulse
                    var sum = 0.0
                    for (i in 0 until read) sum += buffer[i].toDouble() * buffer[i]
                    val rms = kotlin.math.sqrt(sum / read)
                    amplitude = ((rms / Short.MAX_VALUE) * 2.5).toFloat().coerceIn(0f, 1f)
                    for (i in 0 until read) {
                        pcm.write(buffer[i].toInt() and 0xFF)
                        pcm.write((buffer[i].toInt() shr 8) and 0xFF)
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { record.stop() } catch (_: Exception) {}
                try { record.release() } catch (_: Exception) {}
                if (audioRecord === record) audioRecord = null
            }
            val bytes = pcm.toByteArray()
            if (bytes.isNotEmpty()) {
                val rec = Recording(
                    wavBytes = buildWav(bytes, SAMPLE_RATE, 1, 16),
                    durationMs = elapsedMs
                )
                lastRecording = rec
                if (hitLimit) onLimitReached?.invoke()
            }
        }
        return true
    }

    /**
     * Stop recording and return the captured WAV, or null if nothing captured.
     * Safe to call after a 30s auto-stop: the flag may already be cleared by
     * the recording loop, in which case we just join the job and return the
     * finished recording.
     */
    suspend fun stop(): Recording? = withContext(Dispatchers.IO) {
        val wasRecording = isRecording.getAndSet(false)
        recordJob?.join()
        if (wasRecording) amplitude = 0f
        lastRecording
    }

    /** Hard cancel — discard whatever was captured. */
    fun cancel() {
        isRecording.set(false)
        recordJob?.cancel()
        try { audioRecord?.stop() } catch (_: Exception) {}
        try { audioRecord?.release() } catch (_: Exception) {}
        audioRecord = null
        amplitude = 0f
        elapsedMs = 0L
    }

    /** Standard 44-byte RIFF/WAVE header for 16-bit PCM. */
    private fun buildWav(pcm: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val totalLen = pcm.size + 36
        val header = ByteArray(44)
        fun putAscii(off: Int, s: String) { s.forEachIndexed { i, c -> header[off + i] = c.code.toByte() } }
        fun putIntLe(off: Int, v: Int) {
            header[off] = (v and 0xFF).toByte(); header[off + 1] = ((v shr 8) and 0xFF).toByte()
            header[off + 2] = ((v shr 16) and 0xFF).toByte(); header[off + 3] = ((v shr 24) and 0xFF).toByte()
        }
        putAscii(0, "RIFF"); putIntLe(4, totalLen); putAscii(8, "WAVE")
        putAscii(12, "fmt "); putIntLe(16, 16)
        header[20] = 1; header[21] = 0 // PCM
        header[22] = channels.toByte(); header[23] = 0
        putIntLe(24, sampleRate); putIntLe(28, byteRate)
        header[32] = blockAlign.toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        putAscii(36, "data"); putIntLe(40, pcm.size)
        return header + pcm
    }
}
