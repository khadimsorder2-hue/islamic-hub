package com.islamichub.app.ui.screens.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.model.Ayah
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.screens.tafsir.TafsirFullScreen
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.AppSpacing
import com.islamichub.app.ui.theme.AppRadius
import com.islamichub.app.ui.theme.AppIconSizes
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.englishSp
import com.islamichub.app.ui.theme.premiumGlow
import com.islamichub.app.ui.theme.premiumPulseHighlight
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance

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
                    // Font size decrease
                    IconButton(onClick = { vm.decreaseFontSize() }) {
                        Text("A-", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Font size increase
                    IconButton(onClick = { vm.increaseFontSize() }) {
                        Text("A+", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // v5.9.0 — the "বাংলা অডিও" pill was removed: it flipped a flag that
                    // no playback path ever consumed (Bangla recitation pipeline is not
                    // built yet), so the button was pure confusion. Reciter audio below.
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
                    // Reciter + Bangla audio + font size banner
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
                                // v5.9.0 — "বাংলা অডিও চালু" pill removed: the flag behind
                                // it never influenced playback (no Bangla recitation pipeline
                                // yet), so it advertised a feature that does not exist.
                                Text(
                                    text = "  ফন্ট: ${"%.0f".format(state.quranFontScale * 100)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Note: Audio playback is controlled via the app-level
                    // FloatingAudioPlayer (visible across all screens when playing).
                    // Old in-list mini player removed per user request.

                    // ─── Multi Bangla Translation Selector (v5.1+) ───
                    if (state.availableTranslations.isNotEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppRadius.md.value),
                                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f)
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📖 বাংলা অনুবাদ",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (state.isLoadingOnlineTranslations) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(AppIconSizes.small),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                    if (!state.isLoadingOnlineTranslations) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                        ) {
                                            // "অফলাইন" chip first
                                            item {
                                                FilterChip(
                                                    selected = state.selectedTranslationIndex == -1,
                                                    onClick = { vm.selectTranslation(-1) },
                                                    label = { Text("অফলাইন", style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                            items(state.availableTranslations.size) { idx ->
                                                FilterChip(
                                                    selected = state.selectedTranslationIndex == idx,
                                                    onClick = { vm.selectTranslation(idx) },
                                                    label = {
                                                        Text(
                                                            state.availableTranslations[idx],
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (state.isLoadingOnlineTranslations) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppRadius.md.value),
                                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(AppIconSizes.small),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = " বাংলা অনুবাদ লোড হচ্ছে…",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // ─── Offline Download Button ───
                    if (state.surah != null) {
                        item {
                            val isCached = container.translationCache.isSurahCached(surahNumber, state.surah!!.ayahCount)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppRadius.md),
                                color = if (isCached) AppColors.success.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vm.downloadForOffline() }
                                        .padding(AppSpacing.md),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        if (isCached) "✓ অফলাইনে সংরক্ষিত" else "📥 অফলাইনে ডাউনলোড করুন",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCached) AppColors.success else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Surah header card with premium background
                    item {
                        PremiumHeroCard(
                            backgroundImage = "quran-premium-bg.webp",
                            context = context,
                            height = 200
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = surah.nameArabic,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = MaterialTheme.typography.displayMedium.fontSize * state.quranFontScale
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "${surah.nameEnglish} • ${surah.englishMeaning}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${surah.revelationType.label} • ${surah.ayahCount} আয়াত",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
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

                    itemsIndexed(surah.ayahs, key = { _, ayah -> ayah.numberInSurah }) { ayahIndex, ayah ->
                        Box(modifier = Modifier.staggerEntrance(ayahIndex)) {
                        val overrideBangla = if (state.selectedTranslationIndex >= 0 && state.onlineTranslationsLoaded)
                            vm.getBanglaTextForAyah(ayah.numberInSurah, ayah.bengali)
                        else ayah.bengali
                        AyahCard(
                            ayah = ayah,
                            fontScale = state.quranFontScale,
                            showArabic = state.showArabic,
                            showBangla = state.showBangla,
                            showEnglish = state.showEnglish,
                            overrideBangla = overrideBangla,
                            uccaron = state.uccaronMap[ayah.numberInSurah],
                            showUccaron = state.showUccaron,
                            isPlayingAyah = state.currentPlayingAyah == ayah.numberInSurah,
                            isBookmarked = ayah.numberInSurah in state.bookmarkedAyahs,
                            onPlayAyah = { vm.playAyah(ayah.numberInSurah) },
                            onToggleBookmark = { vm.toggleBookmark(ayah.numberInSurah) },
                            onShowTafsir = { showTafsirFor = ayah.numberInSurah },
                            onShowWordByWord = { showWordByWordFor = ayah.numberInSurah },
                            onShareAyah = {
                                val shareText = if (state.selectedTranslationIndex >= 0 && state.onlineTranslationsLoaded)
                                    overrideBangla else ayah.bengali
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT,
                                        "${surah.nameEnglish} (${surah.englishMeaning}) — আয়াত ${ayah.numberInSurah}:\n\n" +
                                        "${ayah.arabic}\n\n" +
                                        "বাংলা: $shareText\n\n" +
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
    }

    // Tafsir full screen popup (replaces old ModalBottomSheet)
    showTafsirFor?.let { ayah ->
        com.islamichub.app.ui.screens.tafsir.TafsirFullScreen(
            container = container,
            surah = surahNumber,
            ayah = ayah,
            onClose = { showTafsirFor = null }
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

// v5.9.0 — dead AudioPlaybackBar composable removed (superseded by the
// always-visible FloatingAudioPlayer in the app scaffold).

@Composable
private fun AyahCard(
    ayah: Ayah,
    fontScale: Float,
    showArabic: Boolean,
    showBangla: Boolean,
    showEnglish: Boolean,
    overrideBangla: String = "",
    uccaron: String? = null,
    showUccaron: Boolean = true,
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
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .premiumPulseHighlight(
                    active = isPlayingAyah,
                    color = MaterialTheme.colorScheme.primary,
                    cornerRadius = 18.dp
                )
                .padding(16.dp),
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
                    IconButton(
                        onClick = onPlayAyah,
                        modifier = Modifier
                            .size(36.dp)
                            .premiumGlow(
                                active = isPlayingAyah,
                                color = MaterialTheme.colorScheme.primary
                            )
                    ) {
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
                    // ─── More menu ───
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
                        fontSize = arabicSp(MaterialTheme.typography.displaySmall.fontSize * fontScale)
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
            // ─── v5.5 Bangla uccaron (transliteration) — right under Arabic ───
            if (showUccaron && !uccaron.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "উচ্চারণ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uccaron,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showBangla) {
                Text(
                    text = overrideBangla.ifBlank { ayah.bengali },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize * fontScale)
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (showEnglish) {
                Text(
                    text = ayah.english,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = englishSp(MaterialTheme.typography.bodySmall.fontSize * fontScale)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ─── v5.5 AI quick-action row: labeled gradient AI pill + word-by-word ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.premiumTap(onClick = onShowTafsir)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI তাফসীর",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.premiumTap(onClick = onShowWordByWord)
                ) {
                    Text(
                        text = "🔤 শব্দে শব্দ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}
