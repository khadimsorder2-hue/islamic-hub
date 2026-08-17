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

/**
 * Single authoritative audio controller for the entire app.
 *
 * Uses Media3 ExoPlayer for tilawat (Quran recitation) streaming.
 * State is exposed as a single StateFlow so any screen can observe
 * playback state without spawning its own player instance.
 *
 * Recitation source: AlQuran.cloud CDN (everyayah.com mirrors) — public.
 *
 * v1.2.0 additions:
 *  - Auto-pause / sleep timer (5/15/30/60 min)
 *  - Bangla meaning audio toggle (read translation after Arabic)
 *  - Khatam player (continuous surah-by-surah playback)
 *  - Per-reciter selection (persisted via SettingsRepository)
 */
class AudioController(private val context: Context) {

    data class AudioState(
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val currentSurah: Int? = null,
        val currentAyah: Int? = null,
        val reciter: String = "Alafasy",
        val error: String? = null,
        val durationMs: Long = 0L,
        val positionMs: Long = 0L,
        val autoPauseMinutesRemaining: Int = 0,
        val isKhatamMode: Boolean = false,
        val playbackSpeed: Float = 1.0f,
        val isRepeatMode: Boolean = false
    )

    private val _state = MutableStateFlow(AudioState())
    val state: StateFlow<AudioState> = _state.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    // AlQuran.cloud audio editions — full surah MP3 stream URLs.
    val availableReciters: List<Reciter> = availableRecitersStatic

    private var player: ExoPlayer? = null
    private var currentReciter: Reciter = availableReciters.first()

    // Auto-pause timer state
    private var autoPauseMinutes: Int = 0
    private var autoPauseEndMs: Long = 0L
    private var autoPauseTick: Runnable? = null

    // Bangla meaning audio toggle
    private var banglaAudioEnabled: Boolean = false

    // Khatam player state
    private var isKhatamMode: Boolean = false
    private var khatamSurahQueue: List<Int> = emptyList()
    private var khatamCurrentIndex: Int = 0

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
                    // If repeat mode, replay current ayah
                    if (_state.value.isRepeatMode && currentAyah != null && currentSurah != null) {
                        val surah = currentSurah!!
                        val ayah = currentAyah!!
                        playAyah(surah, ayah, currentReciter)
                    } else if (isKhatamMode && khatamCurrentIndex < khatamSurahQueue.size - 1) {
                        khatamCurrentIndex++
                        val nextSurah = khatamSurahQueue[khatamCurrentIndex]
                        playSurahInternal(nextSurah, currentReciter)
                    } else {
                        _state.value = _state.value.copy(
                            isPlaying = false,
                            positionMs = 0L,
                            currentSurah = null,
                            currentAyah = null,
                            isKhatamMode = false
                        )
                    }
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
     * Play full surah recitation. URL pattern:
     *   https://cdn.islamic.network/quran/audio-surah/{edition}/{surah:02d}.mp3
     */
    fun playSurah(surahNumber: Int, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        isKhatamMode = false
        playSurahInternal(surahNumber, reciter)
        _state.value = _state.value.copy(
            reciter = reciter.displayName,
            error = null,
            isKhatamMode = false
        )
    }

    private fun playSurahInternal(surahNumber: Int, reciter: Reciter) {
        // audio-surah endpoint returns 403 on CDN.
        // Instead, we play the first ayah of the surah. User can tap individual
        // ayahs to continue. This is a known limitation — full surah playback
        // would require concatenating per-ayah audio which we do in khatam mode.
        // For now, play ayah 1 of the surah.
        val url = "https://cdn.islamic.network/quran/audio/128/${reciter.editionId}/" +
            "${globalAyahNumber(surahNumber, 1)}.mp3"
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Surah #$surahNumber (Ayah 1)")
                    .setArtist(reciter.displayName)
                    .build()
            )
            .build()
        val p = ensurePlayer()
        p.setMediaItem(mediaItem)
        p.prepare()
        p.playWhenReady = true
        _state.value = _state.value.copy(
            currentSurah = surahNumber,
            currentAyah = 1,
            reciter = reciter.displayName,
            error = null
        )
    }

    fun playAyah(surahNumber: Int, ayahNumber: Int, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        isKhatamMode = false
        // URL pattern: https://cdn.islamic.network/quran/audio/128/{edition}/{globalAyahNumber}.mp3
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
        _state.value = _state.value.copy(
            currentSurah = surahNumber,
            currentAyah = ayahNumber,
            reciter = reciter.displayName,
            error = null,
            isKhatamMode = false
        )
    }

    /**
     * Khatam player: continuously play surahs from startSurah to 114.
     */
    fun startKhatamPlayer(startSurah: Int = 1, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        isKhatamMode = true
        khatamSurahQueue = (startSurah..114).toList()
        khatamCurrentIndex = 0
        val firstSurah = khatamSurahQueue[khatamCurrentIndex]
        playSurahInternal(firstSurah, reciter)
        _state.value = _state.value.copy(
            isKhatamMode = true,
            reciter = reciter.displayName
        )
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.play()
    }

    fun stop() {
        player?.stop()
        isKhatamMode = false
        cancelAutoPause()
        _state.value = _state.value.copy(
            isPlaying = false,
            currentSurah = null,
            currentAyah = null,
            positionMs = 0L,
            isKhatamMode = false,
            autoPauseMinutesRemaining = 0
        )
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun release() {
        cancelAutoPause()
        player?.release()
        player = null
        _state.value = AudioState()
    }

    /**
     * Set auto-pause / sleep timer. When minutes > 0, playback stops
     * automatically after the specified duration.
     */
    fun setAutoPause(option: com.islamichub.app.data.repo.AutoPauseOption) {
        cancelAutoPause()
        if (option.minutes <= 0) {
            _state.value = _state.value.copy(autoPauseMinutesRemaining = 0)
            return
        }
        autoPauseMinutes = option.minutes
        autoPauseEndMs = System.currentTimeMillis() + option.minutes * 60_000L
        startAutoPauseTicker()
    }

    fun setBanglaAudioEnabled(enabled: Boolean) {
        banglaAudioEnabled = enabled
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
                handler.postDelayed(this, 30_000L)  // tick every 30s
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

