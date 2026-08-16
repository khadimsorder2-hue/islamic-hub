package com.islamichub.app.ui.screens.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.ui.screens.tafsir.TafsirBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    container: AppContainer,
    surahNumber: Int,
    onBack: () -> Unit
) {
    val vm = remember { QuranReaderViewModel(container, surahNumber) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showTafsirFor by remember { mutableStateOf<Int?>(null) }
    var showQariSelector by remember { mutableStateOf(false) }
    var showWordByWordFor by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.surah?.nameEnglish ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = state.surah?.englishMeaning ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Qari selector button
                    IconButton(onClick = { showQariSelector = true }) {
                        Icon(Icons.Filled.Person, contentDescription = "Select reciter")
                    }
                    // Play full surah
                    if (state.surah != null) {
                        IconButton(onClick = { vm.playSurah() }) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = "Play full surah")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            state.notAvailable -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Surah #$surahNumber not available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            else -> {
                val surah = state.surah!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reciter banner
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showQariSelector = true }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "কারী: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = state.selectedReciterName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "  (পরিবর্তন করুন)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Audio playback bar
                    if (state.isPlayingAudio || state.isLoadingAudio || state.currentPlayingAyah != null) {
                        item {
                            AudioPlaybackBar(
                                isLoading = state.isLoadingAudio,
                                isPlaying = state.isPlayingAudio,
                                ayahLabel = state.currentPlayingAyah?.let { "আয়াত $it" } ?: "সম্পূর্ণ সূরা",
                                onPlayPause = { vm.toggleAudio() },
                                onStop = { vm.stopAudio() }
                            )
                        }
                    }

                    // Surah header card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = surah.nameArabic,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = MaterialTheme.typography.displayMedium.fontSize * state.quranFontScale
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${surah.nameEnglish} • ${surah.englishMeaning}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${surah.revelationType.label} • ${surah.ayahCount} আয়াত",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.quran_bismillah),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize * state.quranFontScale
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    items(surah.ayahs, key = { it.numberInSurah }) { ayah ->
                        AyahCard(
                            ayah = ayah,
                            fontScale = state.quranFontScale,
                            showArabic = state.showArabic,
                            showBangla = state.showBangla,
                            showEnglish = state.showEnglish,
                            isPlayingAyah = state.currentPlayingAyah == ayah.numberInSurah,
                            isBookmarked = ayah.numberInSurah in state.bookmarkedAyahs,
                            onPlayAyah = { vm.playAyah(ayah.numberInSurah) },
                            onToggleBookmark = { vm.toggleBookmark(ayah.numberInSurah) },
                            onShowTafsir = { showTafsirFor = ayah.numberInSurah },
                            onShowWordByWord = { showWordByWordFor = ayah.numberInSurah },
                            onShareAyah = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT,
                                        "${surah.nameEnglish} (${surah.englishMeaning}) — আয়াত ${ayah.numberInSurah}:\n\n" +
                                        "${ayah.arabic}\n\n" +
                                        "বাংলা: ${ayah.bengali}\n\n" +
                                        "English: ${ayah.english}\n\n" +
                                        "— Islamic Hub থেকে শেয়ার করা হয়েছে"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "আয়াত শেয়ার করুন"))
                            }
                        )
                    }
                }
            }
        }
    }

    // Tafsir bottom sheet
    showTafsirFor?.let { ayah ->
        TafsirBottomSheet(
            container = container,
            surah = surahNumber,
            ayah = ayah,
            onDismiss = { showTafsirFor = null }
        )
    }

    // Word-by-word bottom sheet
    showWordByWordFor?.let { ayah ->
        val ayahObj = state.surah?.ayahs?.firstOrNull { it.numberInSurah == ayah }
        if (ayahObj != null) {
            WordByWordBottomSheet(
                ayah = ayahObj,
                onPlayAudio = { vm.playAyah(ayah) },
                onDismiss = { showWordByWordFor = null }
            )
        }
    }

    // Qari selector sheet
    if (showQariSelector) {
        QariSelectorSheet(
            container = container,
            onDismiss = { showQariSelector = false }
        )
    }
}

@Composable
private fun AudioPlaybackBar(
    isLoading: Boolean,
    isPlaying: Boolean,
    ayahLabel: String,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            } else {
                FilledTonalIconButton(onClick = onPlayPause, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoading) "বাফার হচ্ছে…" else if (isPlaying) "চলছে" else "বিরতি",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = ayahLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = onStop, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop")
            }
        }
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun AyahCard(
    ayah: Ayah,
    fontScale: Float,
    showArabic: Boolean,
    showBangla: Boolean,
    showEnglish: Boolean,
    isPlayingAyah: Boolean,
    isBookmarked: Boolean,
    onPlayAyah: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShowTafsir: () -> Unit,
    onShowWordByWord: () -> Unit,
    onShareAyah: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayingAyah)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = ayah.numberInSurah.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPlayAyah, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "আয়াত চালান",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "বুকমার্ক",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "আরও",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_word_by_word)) },
                                onClick = {
                                    showMenu = false
                                    onShowWordByWord()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_tafsir)) },
                                onClick = {
                                    showMenu = false
                                    onShowTafsir()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                onClick = {
                                    showMenu = false
                                    onShareAyah()
                                }
                            )
                        }
                    }
                }
            }
            if (showArabic) {
                Text(
                    text = ayah.arabic,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = MaterialTheme.typography.displaySmall.fontSize * fontScale
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
            if (showBangla) {
                Text(
                    text = ayah.bengali,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * fontScale
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (showEnglish) {
                Text(
                    text = ayah.english,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize * fontScale
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
