package com.islamichub.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.ui.theme.toBanglaDigits
import kotlinx.coroutines.delay

/**
 * Floating mini audio player that overlays on all screens.
 * Shows when audio is playing or loading.
 * Positioned at the bottom, above the bottom navigation bar.
 */
@Composable
fun FloatingAudioPlayer(
    container: AppContainer,
    modifier: Modifier = Modifier,
    /** v5.9.0 — tap the title row to jump into that surah in the Quran reader. */
    onOpenReader: ((Int) -> Unit)? = null,
    /** v5.9.0 — add bottom inset when the bottom navigation bar is hidden. */
    padForNavigationBar: Boolean = false
) {
    val audioState by container.audioController.state.collectAsState()
    var elapsedSeconds by remember { mutableStateOf(0) }
    var positionMs by remember { mutableStateOf(0L) }

    LaunchedEffect(audioState.isPlaying) {
        if (audioState.isPlaying) {
            while (true) {
                delay(1000)
                elapsedSeconds++
                // Update position from ExoPlayer
                positionMs = container.audioController.getCurrentPosition()
            }
        }
    }

    LaunchedEffect(audioState.currentSurah, audioState.currentAyah) {
        elapsedSeconds = 0
    }

    AnimatedVisibility(
        visible = audioState.isPlaying || audioState.isLoading || audioState.currentSurah != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (padForNavigationBar) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onOpenReader != null && audioState.currentSurah != null)
                            Modifier.clickable { onOpenReader(audioState.currentSurah!!) }
                        else Modifier
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play/Pause button (v5.11.0 — haptic tick on press)
                val haptics = LocalHapticFeedback.current
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (audioState.isPlaying) {
                                container.audioController.pause()
                            } else {
                                container.audioController.resume()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (audioState.isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White
                        )
                    }
                }

                // Title info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (audioState.currentAyah != null)
                            "সূরা ${audioState.currentSurah?.toBanglaDigits()} • আয়াত ${audioState.currentAyah?.toBanglaDigits()}"
                            else "সূরা ${audioState.currentSurah?.toBanglaDigits()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                    // Show position / total duration
                    val posStr = formatTime((positionMs / 1000).toInt())
                    val durStr = if (audioState.durationMs > 0) formatTime((audioState.durationMs / 1000).toInt()) else "--:--"
                    Text(
                        text = "${audioState.reciter} • $posStr / $durStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                // v5.10.0 — repeat-ayah toggle (was dead state before)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { container.audioController.toggleRepeatMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (audioState.isRepeatMode) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat ayah",
                        tint = if (audioState.isRepeatMode) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // v5.10.0 — playback speed cycle (1.0x → 1.25x → 1.5x → 2.0x → 1.0x)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            val next = when (audioState.playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                            container.audioController.setPlaybackSpeed(next)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (audioState.playbackSpeed == audioState.playbackSpeed.toInt().toFloat())
                            "${audioState.playbackSpeed.toInt()}x"
                        else "${audioState.playbackSpeed}x",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Stop button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            container.audioController.stop()
                            elapsedSeconds = 0
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // v5.11.0 — inline playback error row (AudioState.error existed but was
            // never surfaced before; failures were completely silent)
            audioState.error?.let { err ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠ $err",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    Text(
                        text = "ঠিক আছে",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { container.audioController.clearError() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // v5.11.0 — determinate SEEKABLE progress (was an endless-loop
            // indeterminate bar; seekTo/getDuration existed but were never wired)
            if (audioState.isPlaying || audioState.isLoading) {
                val durMs = audioState.durationMs
                if (durMs > 0 && !audioState.isLoading) {
                    var dragProgress by remember { mutableStateOf<Float?>(null) }
                    val sliderValue = dragProgress
                        ?: (if (durMs > 0) (positionMs.toFloat() / durMs).coerceIn(0f, 1f) else 0f)
                    Slider(
                        value = sliderValue,
                        onValueChange = { dragProgress = it },
                        onValueChangeFinished = {
                            dragProgress?.let { p ->
                                container.audioController.seekTo((p * durMs).toLong())
                            }
                            dragProgress = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
                        )
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
