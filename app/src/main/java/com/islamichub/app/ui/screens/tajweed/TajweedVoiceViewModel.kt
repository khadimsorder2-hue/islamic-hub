package com.islamichub.app.ui.screens.tajweed

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AIService
import com.islamichub.app.util.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * v5.16.0 — ViewModel for the Tajweed VOICE checker, a 1:1 port of the web
 * TajbeedCheckerService state machine:
 *
 *   intro → recording → review → analyzing → result (→ review/intro)
 *
 * Recording is handled by [VoiceRecorder] (30s cap, auto-stop, live
 * amplitude); review plays the captured WAV back through MediaPlayer
 * (mirrors the web <audio preview>); submit base64-encodes the WAV and
 * sends it to Gemini via AIService.analyzeAudio, then parses the strict
 * JSON into an AIService.VoiceAnalysis for the card UI.
 */
class TajweedVoiceViewModel(private val container: AppContainer) : ViewModel() {

    enum class Phase { INTRO, RECORDING, REVIEW, ANALYZING, RESULT }

    data class UiState(
        val phase: Phase = Phase.INTRO,
        val elapsedSec: Int = 0,
        val amplitude: Float = 0f,
        val isPlaying: Boolean = false,
        val error: String? = null,
        val analysis: AIService.VoiceAnalysis? = null,
        val apiKeyConfigured: Boolean = true,
        val autoStopInfo: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val recorder = VoiceRecorder()
    private var recording: VoiceRecorder.Recording? = null
    private var player: MediaPlayer? = null
    private var tickerJob: Job? = null
    private var tempWav: File? = null

    init {
        viewModelScope.launch {
            val apiKey = container.settingsRepository.aiApiKey.first()
            _state.value = _state.value.copy(apiKeyConfigured = apiKey.isNotBlank())
        }
    }

    /** Called after the runtime permission is granted, from the UI. */
    fun startRecording(context: Context) {
        if (_state.value.phase == Phase.RECORDING) return
        recording = null
        _state.value = UiState(phase = Phase.RECORDING, apiKeyConfigured = _state.value.apiKeyConfigured)
        val ok = recorder.start(viewModelScope) {
            // 30s auto-stop — parity with the web timer toast
            _state.value = _state.value.copy(autoStopInfo = "রেকর্ডিং সময় শেষ (৩০ সেকেন্ড)")
            viewModelScope.launch { finishRecordingToReview() }
        }
        if (!ok) {
            _state.value = _state.value.copy(phase = Phase.INTRO, error = "মাইক্রোফোন চালু করতে সমস্যা হয়েছে")
            return
        }
        tickerJob = viewModelScope.launch {
            while (recorder.recording) {
                _state.value = _state.value.copy(
                    elapsedSec = (recorder.elapsedMs / 1000).toInt(),
                    amplitude = recorder.amplitude
                )
                delay(100)
            }
        }
    }

    fun stopRecording() {
        if (_state.value.phase != Phase.RECORDING) return
        viewModelScope.launch { finishRecordingToReview() }
    }

    private suspend fun finishRecordingToReview() {
        tickerJob?.cancel()
        val rec = recorder.stop()
        _state.value = _state.value.copy(amplitude = 0f)
        if (rec == null || rec.wavBytes.size <= 44) {
            _state.value = _state.value.copy(phase = Phase.INTRO, error = "কোনো অডিও রেকর্ড হয়নি, আবার চেষ্টা করুন")
            return
        }
        recording = rec
        // keep the WAV around for MediaPlayer preview
        withContext(Dispatchers.IO) {
            try {
                val f = File(container.context.cacheDir, "tajweed_voice_preview.wav")
                f.writeBytes(rec.wavBytes)
                tempWav = f
            } catch (_: Exception) { }
        }
        _state.value = _state.value.copy(phase = Phase.REVIEW, elapsedSec = (rec.durationMs / 1000).toInt())
    }

    /** REVIEW screen play/pause toggle (web: <audio controls>). */
    fun togglePreview(context: Context) {
        if (_state.value.isPlaying) {
            player?.pause()
            _state.value = _state.value.copy(isPlaying = false)
            return
        }
        val f = tempWav ?: return
        try {
            val p = player
            if (p == null) {
                val np = MediaPlayer()
                np.setDataSource(f.absolutePath)
                np.setOnCompletionListener {
                    _state.value = _state.value.copy(isPlaying = false)
                }
                np.prepare()
                np.start()
                player = np
            } else {
                p.start()
            }
            _state.value = _state.value.copy(isPlaying = true)
        } catch (e: Exception) {
            _state.value = _state.value.copy(error = "প্রিভিউ চালানো যায়নি")
        }
    }

    /** "AI কে পাঠান" — parity with web submitAudio(). */
    fun submitAudio() {
        val rec = recording ?: run {
            _state.value = _state.value.copy(phase = Phase.INTRO, error = "আগে রেকর্ড করুন")
            return
        }
        _state.value = _state.value.copy(phase = Phase.ANALYZING, error = null)
        stopPreview()
        viewModelScope.launch {
            val result = container.aiService.analyzeAudio(rec.wavBytes)
            when {
                result.error == "stale" -> return@launch
                result.error != null -> {
                    _state.value = _state.value.copy(phase = Phase.REVIEW, error = result.error)
                }
                else -> {
                    val parsed = container.aiService.parseVoiceAnalysis(result.answer)
                    _state.value = _state.value.copy(phase = Phase.RESULT, analysis = parsed)
                }
            }
        }
    }

    /** "নতুন রেকর্ড শুরু করুন" — parity with web reRecord(). */
    fun reRecord() {
        stopPreview()
        recording = null
        _state.value = UiState(apiKeyConfigured = _state.value.apiKeyConfigured)
    }

    fun dismissAutoStopInfo() {
        _state.value = _state.value.copy(autoStopInfo = null)
    }

    fun stopPreview() {
        try {
            player?.let { if (it.isPlaying) it.stop() }
            player?.release()
        } catch (_: Exception) {}
        player = null
        _state.value = _state.value.copy(isPlaying = false)
    }

    override fun onCleared() {
        recorder.cancel()
        stopPreview()
        tempWav?.delete()
        super.onCleared()
    }
}
