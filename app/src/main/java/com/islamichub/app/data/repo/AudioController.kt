package com.islamichub.app.data.repo

import android.content.Context
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
        val positionMs: Long = 0L
    )

    private val _state = MutableStateFlow(AudioState())
    val state: StateFlow<AudioState> = _state.asStateFlow()

    // AlQuran.cloud audio editions — full surah MP3 stream URLs.
    // Format: https://cdn.islamic.network/quran/audio-surah/{edition}/{surah}.mp3
    val availableReciters: List<Reciter> = listOf(
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

    private var player: ExoPlayer? = null
    private var currentReciter: Reciter = availableReciters.first()

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
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        positionMs = 0L,
                        currentSurah = null,
                        currentAyah = null
                    )
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
        val url = "https://cdn.islamic.network/quran/audio-surah/${reciter.editionId}/${surahNumber.toString().padStart(3, '0')}.mp3"
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Surah #$surahNumber")
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
            currentAyah = null,
            reciter = reciter.displayName,
            error = null
        )
    }

    fun playAyah(surahNumber: Int, ayahNumber: Int, reciter: Reciter = currentReciter) {
        currentReciter = reciter
        // AlQuran.cloud ayah-by-ayah audio
        // URL: https://cdn.islamic.network/quran/audio/{edition}/{ayah_number_global}.mp3
        // We use everyayah.com mirror for ayah-by-ayah, format:
        //   https://everyayah.com/data/{reciter_path}/{surah:03d}{ayah:03d}.mp3
        val reciterPath = reciter.everyAyahPath ?: reciter.editionId
        val url = "https://cdn.islamic.network/quran/audio/${reciter.editionId}/" +
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
            error = null
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
        _state.value = _state.value.copy(
            isPlaying = false,
            currentSurah = null,
            currentAyah = null,
            positionMs = 0L
        )
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun release() {
        player?.release()
        player = null
        _state.value = AudioState()
    }

    /**
     * Maps (surah, ayah) to the global ayah number used by AlQuran.cloud CDN.
     * Surah 1 has 7 ayahs → surah 2 ayah 1 = global ayah 8.
     */
    private fun globalAyahNumber(surah: Int, ayah: Int): Int {
        // Cumulative ayah counts per surah (standard Hafs numbering).
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
}
