package com.islamichub.app.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.ui.theme.premiumTap

import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AudioController
import kotlinx.coroutines.launch

/**
 * v5.18.0 — tiny reciter-preview player (Muslimeen adoption): streams ayah 1
 * of the chosen reciter straight from the CDN so the user can LISTEN before
 * selecting. One player for the whole sheet; picking/leaving stops it.
 */
private class ReciterPreviewPlayer {
    private var mediaPlayer: android.media.MediaPlayer? = null
    var playingEdition: String? = null
        private set

    fun toggle(editionId: String, bitrate: Int, onStateChanged: () -> Unit) {
        stop()
        if (playingEdition == editionId) { playingEdition = null; onStateChanged(); return }
        try {
            val mp = android.media.MediaPlayer()
            mp.setDataSource(
                "https://cdn.islamic.network/quran/audio/$bitrate/$editionId/1.mp3"
            )
            mp.setOnCompletionListener {
                playingEdition = null
                try { it.release() } catch (_: Exception) {}
                onStateChanged()
            }
            mp.setOnErrorListener { mp2, _, _ ->
                playingEdition = null
                try { mp2.release() } catch (_: Exception) {}
                onStateChanged()
                true
            }
            mp.prepareAsync()
            mediaPlayer = mp
            playingEdition = editionId
        } catch (_: Exception) {
            playingEdition = null
        }
        onStateChanged()
    }

    fun stop() {
        mediaPlayer?.let { try { it.stop(); it.release() } catch (_: Exception) {} }
        mediaPlayer = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QariSelectorSheet(
    container: AppContainer,
    onDismiss: () -> Unit,
    /** v5.10.0 — when provided, shows an offline-audio download banner for this surah. */
    surahForDownload: Int? = null,
    surahAyahCount: Int = 0
) {
    val selectedReciter by remember { container.settingsRepository.selectedReciter }.collectAsState(initial = "ar.alafasy")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var downloadProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    // v5.18.0 — reciter preview (listen before you select)
    val previewPlayer = remember { ReciterPreviewPlayer() }
    var previewingEdition by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { previewPlayer.stop() }
    }
    var isDownloaded by remember(surahForDownload, selectedReciter) {
        mutableStateOf(
            if (surahForDownload != null && surahAyahCount > 0)
                container.audioDownloadService.isSurahDownloaded(selectedReciter, surahForDownload, surahAyahCount)
            else false
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_reciter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // v5.10.0 — offline surah-audio download (finally wired to AudioDownloadService)
            if (surahForDownload != null && surahAyahCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .premiumTap(enabled = downloadProgress == null && !isDownloaded, onClick = {
                            val surahId = surahForDownload
                            if (surahId != null) {
                                downloadProgress = 0 to surahAyahCount
                                scope.launch {
                                    val ok = container.audioDownloadService.downloadSurah(
                                        selectedReciter, surahId, surahAyahCount
                                    ) { done, total -> downloadProgress = done to total }
                                    isDownloaded = ok
                                    downloadProgress = null
                                }
                            }
                        }),
                    color = if (isDownloaded) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = when {
                                isDownloaded -> "✓ অফলাইন অডিও সংরক্ষিত"
                                downloadProgress != null ->
                                    "ডাউনলোড হচ্ছে… ${downloadProgress?.first}/${downloadProgress?.second}"
                                else -> "এই সূরার অডিও অফলাইনে সংরক্ষণ করুন"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(6.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AudioController.availableRecitersStatic, key = { it.editionId }) { reciter ->
                    val isSelected = reciter.editionId == selectedReciter
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .premiumTap(onClick = {
                                previewPlayer.stop()
                                previewingEdition = null
                                scope.launch {
                                    container.settingsRepository.setSelectedReciter(reciter.editionId)
                                    onDismiss()
                                }
                            }),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            // v5.13.1 — active/selected card box FULLY invisible:
                            // no tinted container, no shadow. Selection is shown
                            // by the gradient avatar + "✓ নির্বাচিত" label + check icon.
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Rounded profile avatar with gradient
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (isSelected)
                                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                                else listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reciter.displayName.take(2),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reciter.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSelected) "✓ নির্বাচিত" else reciter.editionId,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // v5.18.0 — preview button: hear the reciter before selecting
                            androidx.compose.material3.FilledTonalIconButton(
                                onClick = {
                                    previewPlayer.toggle(reciter.editionId, reciter.bitrate) {
                                        previewingEdition = previewPlayer.playingEdition
                                    }
                                    previewingEdition = previewPlayer.playingEdition
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (previewingEdition == reciter.editionId) Icons.Filled.Pause
                                                  else Icons.Filled.PlayArrow,
                                    contentDescription = "নমুনা শুনুন",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

