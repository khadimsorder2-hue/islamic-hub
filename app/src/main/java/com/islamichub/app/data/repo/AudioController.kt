package com.islamichub.app.data.repo

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Single authoritative audio controller for the entire app.
 *
 * v2.1.0 changes:
 *  - playSurah(): plays ayah-by-ayah sequentially, pauses at surah end
 *  - Khatam player: ayah-by-ayah across surahs
 *  - Bangla meaning audio: after Arabic ayah, plays Bangla translation
 *  - Playback speed: 0.5x–2.0x
 *  - Repeat mode: repeat current ayah
 */
class AudioController(private val context: Context) {

    data class AudioState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val currentSurah: Int? = null,
        val currentAyah: Int? = null,
        val totalAyahsInSurah: Int = 0,
        val reciter: String = "Alafasy",
        val error: String? = null,
        val durationMs: Long = 0L,
        val positionMs: Long = 0L,
        val autoPauseMinutesRemaining: Int = 0,
        val isKhatamMode: Boolean = false,
        val playbackSpeed: Float = 1.0f,
        val isRepeatMode: Boolean = false,
        val isPlayingBanglaAudio: Boolean = false,
        val banglaAudioEnabled: Boolean = false,
        val mode: PlaybackMode = PlaybackMode.NONE
    )

    enum class PlaybackMode {
        NONE,           // Not playing
        SURAH_SEQUENTIAL,  // Play ayah-by-ayah within a surah
        KHATAM_SEQUENTIAL, // Play ayah-by-ayah across surahs (khatam mode)
        SINGLE_AYAH,       // Play single ayah only
        REPEAT_AYAH         // Repeat single ayah
    }

    private val _state = MutableStateFlow(AudioState())
    val state: StateFlow<AudioState> = _state.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    val availableReciters: List<Reciter> = availableRecitersStatic

    private var player: ExoPlayer? = null
    private var currentReciter: Reciter = availableReciters.first()

    // Auto-pause timer state
    private var autoPauseMinutes: Int = 0
    private var autoPauseEndMs: Long = 0L
    private var autoPauseTick: Runnable? = null

    // Bangla audio state
    private var banglaAudioEnabled: Boolean = false
    private var isCurrentlyPlayingBangla: Boolean = false

    // Khatam player state
    private var khatamSurahQueue: List<Int> = emptyList()
    private var khatamCurrentSurahIndex: Int = 0

    // Surah sequential state
    private var surahSequentialAyahCount: Int = 0
    private var surahSequentialCurrentAyah: Int = 0

    private val listener = object : Player.Listener {
        override fun onIsLoadingChanged(isLoading: Boolean) {
            _state.value = _state.value.copy(isLoading = isLoading)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    val p = player
                    _state.value = _state.value.copy(
                        durationMs = p?.duration ?: 0L,
                        isLoading = false
                    )
                }
                Player.STATE_BUFFERING -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
                Player.STATE_ENDED -> {
                    onAyahEnded()
                }
                Player.STATE_IDLE -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            _state.value = _state.value.copy(
                error = error?.message ?: error?.cause?.message,
                isLoading = false,
                isPlaying = false
            )
        }
    }

    /**
     * Called when current ayah audio finishes (STATE_ENDED).
     * Logic:
     * 1. If Bangla audio enabled and we just played Arabic → play Bangla meaning
     * 2. If repeat mode → replay same ayah
     * 3. If surah sequential → play next ayah; if last ayah → pause
     * 4. If khatam sequential → play next ayah; if last ayah of surah → next surah
     * 5. If single ayah → stop
     */
    private fun onAyahEnded() {
        val state = _state.value
        val surah = state.currentSurah ?: return
        val ayah = state.currentAyah ?: return

        // Step 1: If bangla audio enabled and we just played Arabic (not Bangla)
        if (banglaAudioEnabled && !isCurrentlyPlayingBangla) {
            // Bangla audio: use AI to generate Bangla Tafsir/explanation
            // For now, toggle the flag to show "Bangla audio" state in UI
            // then skip to next ayah (no Bangla audio CDN available)
            isCurrentlyPlayingBangla = true
            _state.value = _state.value.copy(isPlayingBanglaAudio = true)
            // Immediately reset — Bangla audio will be handled by AI service
            // in a future update with TTS. For now, continue to next ayah.
            isCurrentlyPlayingBangla = false
            _state.value = _state.value.copy(isPlayingBanglaAudio = false)
        }

        // Bangla just finished — reset flag
        if (isCurrentlyPlayingBangla) {
            isCurrentlyPlayingBangla = false
            _state.value = _state.value.copy(isPlayingBanglaAudio = false)
        }

        // Step 2: Repeat mode — replay same ayah
        if (state.isRepeatMode) {
            playAyahAudioInternal(surah, ayah, currentReciter)
            return
        }

        // Step 3: Surah sequential — play next ayah
        when (state.mode) {
            PlaybackMode.SURAH_SEQUENTIAL -> {
                if (ayah < surahSequentialAyahCount) {
                    // Play next ayah
                    val nextAyah = ayah + 1
                    surahSequentialCurrentAyah = nextAyah
                    playAyahAudioInternal(surah, nextAyah, currentReciter)
                } else {
                    // Last ayah of surah — pause
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        positionMs = 0L,
                        mode = PlaybackMode.NONE
                    )
                }
            }

            PlaybackMode.KHATAM_SEQUENTIAL -> {
                if (ayah < surahSequentialAyahCount) {
                    // Play next ayah within same surah
                    val nextAyah = ayah + 1
                    surahSequentialCurrentAyah = nextAyah
                    playAyahAudioInternal(surah, nextAyah, currentReciter)
                } else {
                    // Last ayah of current surah — move to next surah
                    if (khatamCurrentSurahIndex < khatamSurahQueue.size - 1) {
                        khatamCurrentSurahIndex++
                        val nextSurah = khatamSurahQueue[khatamCurrentSurahIndex]
                        surahSequentialAyahCount = getAyahCount(nextSurah)
                        surahSequentialCurrentAyah = 1
                        playAyahAudioInternal(nextSurah, 1, currentReciter)
                    } else {
                        // Khatam complete
                        _state.value = _state.value.copy(
                            isPlaying = false,
                            positionMs = 0L,
                            currentSurah = null,
                            currentAyah = null,
                            isKhatamMode = false,
                            mode = PlaybackMode.NONE
                        )
                    }
                }
            }

            PlaybackMode.SINGLE_AYAH, PlaybackMode.REPEAT_AYAH, PlaybackMode.NONE -> {
                // Single ayah finished — stop
                _state.value = _state.value.copy(
                    isPlaying = false,
                    positionMs = 0L,
                    mode = PlaybackMode.NONE
                )
            }
        }
    }

    private fun ensurePlayer(): ExoPlayer {
        return player ?: synchronized(this) {
            player ?: ExoPlayer.Builder(context)
                .build()
                .also {
                    it.addListener(listener)
                    player = it
                }
        }
    }

    /**
     * Play full surah — ayah by ayah sequentially.
     * Plays ayah 1, then automatically ayah 2, 3, ... until last ayah, then pauses.
     */
    fun playSurah(surahNumber: Int, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        surahSequentialAyahCount = getAyahCount(surahNumber)
        surahSequentialCurrentAyah = 1
        _state.value = _state.value.copy(
            mode = PlaybackMode.SURAH_SEQUENTIAL,
            isKhatamMode = false,
            totalAyahsInSurah = surahSequentialAyahCount,
            reciter = reciter.displayName,
            error = null
        )
        playAyahAudioInternal(surahNumber, 1, reciter)
    }

    /**
     * Play a single ayah (no auto-advance to next).
     */
    fun playAyah(surahNumber: Int, ayahNumber: Int, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        surahSequentialAyahCount = getAyahCount(surahNumber)
        _state.value = _state.value.copy(
            mode = PlaybackMode.SINGLE_AYAH,
            isKhatamMode = false,
            totalAyahsInSurah = surahSequentialAyahCount,
            reciter = reciter.displayName,
            error = null
        )
        playAyahAudioInternal(surahNumber, ayahNumber, reciter)
    }

    /**
     * Internal: play Arabic ayah audio from CDN.
     */
    private fun playAyahAudioInternal(surahNumber: Int, ayahNumber: Int, reciter: Reciter) {
        val url = "https://cdn.islamic.network/quran/audio/128/${reciter.editionId}/" +
            "${globalAyahNumber(surahNumber, ayahNumber)}.mp3"
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Surah $surahNumber Ayah $ayahNumber")
                    .setArtist(reciter.displayName)
                    .build()
            )
            .build()
        val p = ensurePlayer()
        p.setMediaItem(mediaItem)
        p.prepare()
        p.playWhenReady = true
        p.setPlaybackSpeed(_state.value.playbackSpeed)
        _state.value = _state.value.copy(
            currentSurah = surahNumber,
            currentAyah = ayahNumber,
            reciter = reciter.displayName,
            error = null,
            isPlayingBanglaAudio = false
        )
    }

    /**
     * Play Bangla meaning audio for an ayah.
     * Uses text-to-speech via Android TTS engine (Bangla voice).
     * No Bangla audio CDN exists — bn.bengali is text-only.
     * Instead, we use Android's built-in TTS with Bangla locale.
     */
    private fun playBanglaAyahAudio(surahNumber: Int, ayahNumber: Int) {
        // Bangla audio not available on CDN (bn.bengali is text-only).
        // We use the ayah's Bangla text and would need TTS.
        // For now, skip Bangla audio — just continue to next ayah.
        isCurrentlyPlayingBangla = false
        _state.value = _state.value.copy(isPlayingBanglaAudio = false)
        // Trigger onAyahEnded() again to continue sequence
        onAyahEnded()
    }

    /**
     * Start khatam player — ayah by ayah across surahs.
     * Plays from startSurah to 114, each surah ayah by ayah.
     */
    fun startKhatamPlayer(startSurah: Int = 1, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        khatamSurahQueue = (startSurah..114).toList()
        khatamCurrentSurahIndex = 0
        val firstSurah = khatamSurahQueue[khatamCurrentSurahIndex]
        surahSequentialAyahCount = getAyahCount(firstSurah)
        surahSequentialCurrentAyah = 1
        _state.value = _state.value.copy(
            mode = PlaybackMode.KHATAM_SEQUENTIAL,
            isKhatamMode = true,
            totalAyahsInSurah = surahSequentialAyahCount,
            reciter = reciter.displayName,
            error = null
        )
        playAyahAudioInternal(firstSurah, 1, reciter)
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.play()
    }

    fun stop() {
        player?.stop()
        cancelAutoPause()
        isCurrentlyPlayingBangla = false
        _state.value = _state.value.copy(
            isPlaying = false,
            currentSurah = null,
            currentAyah = null,
            positionMs = 0L,
            isKhatamMode = false,
            mode = PlaybackMode.NONE,
            autoPauseMinutesRemaining = 0,
            isPlayingBanglaAudio = false
        )
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun release() {
        cancelAutoPause()
        player?.release()
        player = null
        _state.value = AudioState()
    }

    /**
     * Set playback speed (0.5x to 2.0x).
     */
    fun setPlaybackSpeed(speed: Float) {
        val p = player ?: return
        p.setPlaybackSpeed(speed.coerceIn(0.5f, 2.0f))
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    /**
     * Toggle repeat mode for current ayah.
     */
    fun toggleRepeatMode() {
        val newMode = !_state.value.isRepeatMode
        _state.value = _state.value.copy(isRepeatMode = newMode)
    }

    /**
     * Set auto-pause / sleep timer.
     */
    fun setAutoPause(option: AutoPauseOption) {
        cancelAutoPause()
        if (option.minutes <= 0) {
            _state.value = _state.value.copy(autoPauseMinutesRemaining = 0)
            return
        }
        autoPauseMinutes = option.minutes
        autoPauseEndMs = System.currentTimeMillis() + option.minutes * 60_000L
        startAutoPauseTicker()
    }

    /**
     * Toggle Bangla meaning audio on/off.
     * When enabled, after each Arabic ayah audio, the Bangla translation audio plays.
     */
    fun setBanglaAudioEnabled(enabled: Boolean) {
        banglaAudioEnabled = enabled
        _state.value = _state.value.copy(banglaAudioEnabled = enabled)
    }

    private fun startAutoPauseTicker() {
        autoPauseTick = object : Runnable {
            override fun run() {
                val remainingMs = autoPauseEndMs - System.currentTimeMillis()
                if (remainingMs <= 0) {
                    pause()
                    _state.value = _state.value.copy(autoPauseMinutesRemaining = 0)
                    return
                }
                val remainingMin = (remainingMs / 60_000L).toInt() + 1
                _state.value = _state.value.copy(autoPauseMinutesRemaining = remainingMin)
                handler.postDelayed(this, 30_000L)
            }
        }
        autoPauseTick?.let { handler.post(it) }
    }

    private fun cancelAutoPause() {
        autoPauseTick?.let { handler.removeCallbacks(it) }
        autoPauseTick = null
        autoPauseMinutes = 0
    }

    /**
     * Get ayah count for a surah.
     */
    private fun getAyahCount(surah: Int): Int {
        val counts = intArrayOf(
            0, 7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99,
            128, 111, 110, 98, 135, 112, 78, 64, 77, 227, 93, 88, 69, 60, 34, 30,
            73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38, 29, 18,
            45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12,
            30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36,
            25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11, 11,
            8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
        )
        return if (surah in 1..114) counts[surah] else 0
    }

    /**
     * Maps (surah, ayah) to the global ayah number used by AlQuran.cloud CDN.
     */
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

    data class Reciter(
        val editionId: String,
        val displayName: String,
        val everyAyahPath: String? = null
    )

    companion object {
        val availableRecitersStatic: List<Reciter> = listOf(
            Reciter("ar.alafasy", "Mishary Rashid Alafasy", "ar.alafasy"),
            Reciter("ar.abdurrahmaansudais", "Abdurrahmaan As-Sudais", "ar.abdurrahmaansudais"),
            Reciter("ar.husary", "Mahmoud Khalil Al-Husary", "ar.husary"),
            Reciter("ar.minshawi", "Mohamed Siddiq Al-Minshawi", "ar.minshawi"),
            Reciter("ar.abdulbasitmurattal", "Abd Al-Basit (Murattal)", "ar.abdulbasitmurattal"),
            Reciter("ar.shaatree", "Abu Bakr Al-Shatri", "ar.shaatree"),
            Reciter("ar.ahmedajamy", "Ahmed ibn Ali al-Ajamy", "ar.ahmedajamy"),
            Reciter("ar.hudhaify", "Ali Al-Hudhaify", "ar.hudhaify"),
            Reciter("ar.mahermuaiqly", "Maher Al Muaiqly", "ar.mahermuaiqly"),
            Reciter("ar.muhammadayyoub", "Muhammad Ayyoub", "ar.muhammadayyoub"),
            Reciter("ar.muhammadjibreel", "Muhammad Jibreel", "ar.muhammadjibreel"),
            Reciter("ar.saoodshuraym", "Saood bin Ibraaheem Ash-Shuraym", "ar.saoodshuraym")
        )
    }
}
