package com.islamichub.app.ui.screens.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import androidx.compose.foundation.layout.height
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    container: AppContainer,
    surahNumber: Int,
    initialAyah: Int? = null,
    onBack: () -> Unit
) {
    val vm = remember { QuranReaderViewModel(container, surahNumber) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showTafsirFor by remember { mutableStateOf<Int?>(null) }
    var showQariSelector by remember { mutableStateOf(false) }
    var showWordByWordFor by remember { mutableStateOf<Int?>(null) }
    // v5.16.0 — Andi-style reader theme sheet
    var showThemeSheet by remember { mutableStateOf(false) }
    val readerTheme = state.readerTheme
    val themed = readerTheme != QuranReaderTheme.DEFAULT

    // v5.10.0 — keep the screen awake while reading (user-toggleable in Settings)
    val keepScreenOn by container.settingsRepository.keepScreenOnReading
        .collectAsState(initial = true)
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.DisposableEffect(keepScreenOn) {
        val window = (view.context as? android.app.Activity)?.window
        if (keepScreenOn && window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // v5.10.0 — list state for ayah jump + real reading-position tracking
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showJumpDialog by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(listState) {
        var lastRecorded = -1
        var lastWriteMs = 0L
        androidx.compose.runtime.snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key is Int }?.key as? Int
        }.collect { ayahNum ->
            if (ayahNum != null && ayahNum > 0 && ayahNum != lastRecorded) {
                val now = System.currentTimeMillis()
                if (now - lastWriteMs > 3000) {
                    lastRecorded = ayahNum
                    lastWriteMs = now
                    vm.updateLastRead(ayahNum)
                }
            }
        }
    }

    // v5.11.0 — land on a specific ayah when opened from a bookmark / resume card
    androidx.compose.runtime.LaunchedEffect(state.surah, initialAyah) {
        val target = initialAyah ?: return@LaunchedEffect
        val surah = state.surah ?: return@LaunchedEffect
        if (target in 1..surah.ayahCount && listState.layoutInfo.totalItemsCount > 0) {
            val total = listState.layoutInfo.totalItemsCount
            val nonAyah = (total - surah.ayahCount).coerceAtLeast(0)
            val index = (nonAyah + target - 1).coerceIn(0, (total - 1).coerceAtLeast(0))
            listState.scrollToItem(index)
        }
    }

    // v5.14.0 — bring `ayah` into view (shared by playback auto-follow and the
    // FloatingAudioPlayer click pulse)
    suspend fun scrollToAyah(ayah: Int) {
        val surah = state.surah ?: return
        if (ayah !in 1..surah.ayahCount) return
        val total = listState.layoutInfo.totalItemsCount
        if (total <= 0) return
        val nonAyah = (total - surah.ayahCount).coerceAtLeast(0)
        val index = (nonAyah + ayah - 1).coerceIn(0, (total - 1).coerceAtLeast(0))
        listState.animateScrollToItem(index)
    }

    // v5.14.0 — AUTO-FOLLOW: while audio plays, the screen moves to each
    // playing ayah so the user can read along without searching
    androidx.compose.runtime.LaunchedEffect(state.currentPlayingAyah) {
        val target = state.currentPlayingAyah ?: return@LaunchedEffect
        scrollToAyah(target)
    }

    // v5.14.0 — FloatingAudioPlayer clicked while THIS reader is already open:
    // jump to the exact playing ayah without recreating the screen
    androidx.compose.runtime.LaunchedEffect(state.surah) {
        container.audioController.readerScrollRequests.collect {
            val target = vm.state.value.currentPlayingAyah
            if (target != null) scrollToAyah(target)
        }
    }

    Scaffold(
        // v5.16.0 — reader theme repaints the whole page (Andi adoption);
        // DEFAULT keeps the app Material colors exactly as before
        containerColor = if (themed) readerTheme.bg else MaterialTheme.colorScheme.surface,
        topBar = {
            // v5.16.0 — Andi `fullScreen` reading mode: top bar hidden while
            // reading; a floating pill (below) lets you exit
            if (!state.readerFullscreen) {
            CenterAlignedTopAppBar(
                // v5.16.1 — taller, fully-synced status-bar area: the bar itself
                // adds a fixed 12dp on top of the system inset (windowInsets
                // zeroed to avoid double padding) so the transparent strip with
                // the system clock matches the reader page below.
                modifier = Modifier.statusBarsPadding().padding(top = 12.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
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
                    // v5.16.0 — reader theme picker (Andi-style themes)
                    IconButton(onClick = { showThemeSheet = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "রিডার থিম")
                    }
                    // v5.16.0 — fullscreen reading (Andi fullScreen pref)
                    IconButton(onClick = { vm.toggleReaderFullscreen() }) {
                        Icon(Icons.Filled.Fullscreen, contentDescription = "ফুলস্ক্রিন পড়া")
                    }
                    // v5.9.0 — the "বাংলা অডিও" pill was removed: it flipped a flag that
                    // no playback path ever consumed (Bangla recitation pipeline is not
                    // built yet), so the button was pure confusion. Reciter audio below.
                    // Qari selector button
                    IconButton(onClick = { showQariSelector = true }) {
                        Icon(Icons.Filled.Person, contentDescription = "Select reciter")
                    }
                    // v5.10.0 — jump to a specific ayah
                    IconButton(onClick = { showJumpDialog = true }) {
                        Icon(Icons.Filled.FastForward, contentDescription = "আয়াতে যান")
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
        }
    ) { padding ->
        Box {
        when {
            // v5.13.1 — retry state instead of an endless spinner
            state.loadFailed -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.islamichub.app.ui.components.PremiumEmptyState(
                        icon = Icons.Filled.CloudOff,
                        title = "সূরা লোড করা যায়নি",
                        subtitle = "আবার চেষ্টা করুন — সমস্যা হলে অ্যাপটি বন্ধ করে খুলুন",
                        ctaText = "আবার চেষ্টা করুন",
                        onCta = { vm.retryLoad() }
                    )
                }
            }
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
                    state = listState,
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

                    // v5.15.0 — Andi-style card options: inline REAL word-by-word
                    // chips + the optional বিকল্প অর্থ (মুহিউদ্দীন খান) meaning line
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.showWbwInline,
                                onClick = { vm.toggleWbwInline() },
                                label = { Text("🔤 শব্দে শব্দ", style = MaterialTheme.typography.labelSmall) }
                            )
                            FilterChip(
                                selected = state.showAltMeaning,
                                onClick = { vm.toggleAltMeaning() },
                                label = { Text("📖 বিকল্প অর্থ", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

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
                            // v5.16.0 — theme-aware bismillah color
                            color = if (themed) readerTheme.arabicColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
                            uccaron = vm.effectiveUccharon(ayah.numberInSurah),
                            showUccaron = state.showUccaron,
                            isPlayingAyah = state.currentPlayingAyah == ayah.numberInSurah,
                            isBookmarked = ayah.numberInSurah in state.bookmarkedAyahs,
                            // v5.16.0 — Andi-style reader theme colors (null = app theme)
                            themedCardBg = if (themed) readerTheme.cardBg else null,
                            themedPlayingBg = if (themed) readerTheme.playingBg else null,
                            themedArabicColor = if (themed) readerTheme.arabicColor else null,
                            themedTextColor = if (themed) readerTheme.banglaColor else null,
                            themedUccharonColor = if (themed) readerTheme.uccharonColor else null,
                            wbwWords = state.wbwMap[ayah.numberInSurah] ?: emptyList(),
                            showWbwInline = state.showWbwInline,
                            altMeaning = state.andiMeaningMap[ayah.numberInSurah],
                            showAltMeaning = state.showAltMeaning,
                            onPlayAyah = {
                                // v5.14.0 — ayah-level play/pause toggle
                                if (state.currentPlayingAyah == ayah.numberInSurah && state.isPlayingAudio) {
                                    vm.pauseAudio()
                                } else {
                                    vm.playAyah(ayah.numberInSurah)
                                }
                            },
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
                            },
                            onCopyAyah = {
                                // v5.11.0 — copy full ayah (Arabic + Bangla + English) to clipboard
                                val copyText = "${ayah.arabic}\n\n" +
                                    "বাংলা: ${ayah.bengali}\n\n" +
                                    "English: ${ayah.english}\n\n" +
                                    "— ${surah.nameEnglish} ${surah.nameBengali} : আয়াত ${ayah.numberInSurah}"
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("islamic-hub-ayah", copyText))
                                android.widget.Toast.makeText(context, "আয়াত কপি হয়েছে ✅", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        }
                    }
                }
            }
        }

        // v5.16.0 — fullscreen reading mode: floating theme + exit pill
        // (the top bar is hidden, so the user still needs a way out)
        if (state.readerFullscreen) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 12.dp),
                shape = RoundedCornerShape(50),
                color = if (themed) readerTheme.cardBg else MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 4.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showThemeSheet = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "রিডার থিম",
                            tint = if (themed) readerTheme.banglaColor else MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { vm.toggleReaderFullscreen() }) {
                        Icon(Icons.Filled.FullscreenExit, contentDescription = "ফুলস্ক্রিন বন্ধ করুন",
                            tint = if (themed) readerTheme.banglaColor else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        } // Box
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
                wbwWords = state.wbwMap[ayah],
                uccharon = vm.effectiveUccharon(ayah),
                altMeaning = state.andiMeaningMap[ayah],
                onPlayAudio = { vm.playAyah(ayah) },
                onDismiss = { showWordByWordFor = null }
            )
        }
    }

    // Qari selector sheet
    if (showQariSelector) {
        QariSelectorSheet(
            container = container,
            onDismiss = { showQariSelector = false },
            surahForDownload = surahNumber,
            surahAyahCount = state.surah?.ayahCount ?: 0
        )
    }

    // v5.16.0 — reader theme picker (Andi-style reading themes)
    if (showThemeSheet) {
        ModalBottomSheet(onDismissRequest = { showThemeSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "কুরআন রিডার থিম",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "রিডিং পেজের রঙ — Andi অ্যাপ থেকে নেওয়া থিমসহ মোট ৬টি",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                QuranReaderTheme.entries.forEach { t ->
                    val selected = state.readerTheme == t
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                vm.setReaderTheme(t)
                            }
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (t == QuranReaderTheme.DEFAULT)
                                        MaterialTheme.colorScheme.surfaceVariant else t.bg
                                )
                                .border(
                                    2.dp,
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            t.labelBn,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "নির্বাচিত",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    // v5.10.0 — "আয়াতে যান" jump dialog
    if (showJumpDialog) {
        var jumpInput by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            icon = {
                com.islamichub.app.ui.components.PremiumDialogIcon(
                    icon = Icons.Filled.FastForward,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("আয়াতে যান", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "আয়াত নম্বর লিখুন (১–${state.surah?.ayahCount ?: 0})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jumpInput,
                        onValueChange = { jumpInput = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        label = { Text("আয়াত নম্বর") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = jumpInput.toIntOrNull()
                        val count = state.surah?.ayahCount ?: 0
                        if (target != null && target in 1..count) {
                            showJumpDialog = false
                            val total = listState.layoutInfo.totalItemsCount
                            val nonAyah = (total - count).coerceAtLeast(0)
                            val index = (nonAyah + target - 1).coerceIn(0, (total - 1).coerceAtLeast(0))
                            scope.launch { listState.animateScrollToItem(index) }
                        }
                    },
                    enabled = (jumpInput.toIntOrNull() ?: 0) in 1..(state.surah?.ayahCount ?: 0)
                ) { Text("যান") }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

// v5.9.0 — dead AudioPlaybackBar composable removed (superseded by the
// always-visible FloatingAudioPlayer in the app scaffold).

// ═══════════════════════════════════════════════════════════════════════════
// v5.15.0 — Andi-style ayah card (port of the decoded "Al Quran Bangla"
// reference app reader): ornamental octagonal number box + per-ayah play
// button in the left rail, Arabic mushaf text, teal uccharon, Bangla
// meaning, optional বিকল্প অর্থ, REAL word-by-word chips, then the
// quick-action row. The calm playing tint from v5.14.0 is kept.
// ═══════════════════════════════════════════════════════════════════════════

// Andi reference palette — teal uccaron + sand ornament gold
private val UccharonTeal = Color(0xFF4B7E7A)
private val OrnamentGold = Color(0xFFC9B89B)
private val OrnamentCream = Color(0xFFF2EAD5)

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
    wbwWords: List<com.islamichub.app.data.local.WbwWord> = emptyList(),
    showWbwInline: Boolean = false,
    altMeaning: String? = null,
    showAltMeaning: Boolean = false,
    // v5.16.0 — Andi-style reader theme overrides (null = Material colors)
    themedCardBg: Color? = null,
    themedPlayingBg: Color? = null,
    themedArabicColor: Color? = null,
    themedTextColor: Color? = null,
    themedUccharonColor: Color? = null,
    onPlayAyah: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShowTafsir: () -> Unit,
    onShowWordByWord: () -> Unit,
    onShareAyah: () -> Unit,
    onCopyAyah: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            // v5.14.0 — calm reading mode: the playing ayah gets a soft,
            // single-color tint instead of pulsing/glowing animation so the
            // user can read along comfortably (user request)
            // v5.16.0 — reader-theme overrides take precedence when active
            containerColor = when {
                isPlayingAyah && themedPlayingBg != null -> themedPlayingBg
                isPlayingAyah -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                themedCardBg != null -> themedCardBg
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            // ─── Left rail: ornamental number box + play (Andi layout) ───
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(42.dp)
            ) {
                AyahNumberBadge(
                    number = ayah.numberInSurah,
                    isPlaying = isPlayingAyah
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isPlayingAyah)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.premiumTap(onClick = onPlayAyah)
                ) {
                    Icon(
                        imageVector = if (isPlayingAyah) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "আয়াত চালান",
                        tint = if (isPlayingAyah) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(6.dp).size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            // ─── Content column ───
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showArabic) {
                    Text(
                        text = ayah.arabic,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = arabicSp(MaterialTheme.typography.displaySmall.fontSize * fontScale)
                        ),
                        color = themedArabicColor ?: MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
                // v5.15.0 — Andi teal uccharon line: plain readable text,
                // no chip/glow — soft color so it can be read together
                if (showUccaron && !uccaron.isNullOrBlank()) {
                    Text(
                        text = uccaron,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize * fontScale)
                        ),
                        color = themedUccharonColor ?: UccharonTeal
                    )
                }
                if (showBangla) {
                    Text(
                        text = overrideBangla.ifBlank { ayah.bengali },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize * fontScale)
                        ),
                        color = themedTextColor ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                // v5.15.0 — optional বিকল্প অর্থ (Andi মুহিউদ্দীন খান meaning)
                if (showAltMeaning && !altMeaning.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "বিকল্প অর্থ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrnamentGold
                        )
                        Text(
                            text = altMeaning,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize * fontScale)
                            ),
                            color = themedTextColor?.copy(alpha = 0.85f) ?: MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (showEnglish) {
                    Text(
                        text = ayah.english,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = englishSp(MaterialTheme.typography.bodySmall.fontSize * fontScale)
                        ),
                        color = themedTextColor?.copy(alpha = 0.72f) ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // v5.15.0 — REAL word-by-word chips, right-to-left like the mushaf
                if (showWbwInline && wbwWords.isNotEmpty()) {
                    WbwChipsInline(words = wbwWords)
                }
                // ─── Quick actions (AI tafsir + wbw sheet + bookmark + menu) ───
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
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "বুকমার্ক",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(34.dp)) {
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
                            // v5.11.0 — copy the full ayah (every sibling detail screen
                            // has copy; the reader menu only had word-by-word/tafsir/share)
                            DropdownMenuItem(
                                text = { Text("কপি") },
                                onClick = {
                                    showMenu = false
                                    onCopyAyah()
                                }
                            )
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
        }
    }
}

/**
 * v5.15.0 — ornamental octagonal ayah-number badge (the Andi box_aya look,
 * rebuilt natively in Compose): cream fill, gold frame, number centered.
 * While the ayah plays it turns solid primary so the follow-along position
 * is obvious at a glance.
 */
@Composable
private fun AyahNumberBadge(number: Int, isPlaying: Boolean) {
    val fill = if (isPlaying) MaterialTheme.colorScheme.primary else OrnamentCream
    val frame = if (isPlaying) MaterialTheme.colorScheme.primary else OrnamentGold
    val label = if (isPlaying) MaterialTheme.colorScheme.onPrimary else Color(0xFF6B5636)
    // v5.16.1 — badge was too dominant on the page: compact 34×40 box and a
    // slight transparency so the ornament blends into the card (full opacity
    // kept while the ayah is playing so the highlight still pops).
    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 40.dp)
            .alpha(if (isPlaying) 0.95f else 0.70f),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cut = w * 0.24f
            val octagon = androidx.compose.ui.graphics.Path().apply {
                moveTo(cut, 0f); lineTo(w - cut, 0f); lineTo(w, cut); lineTo(w, h - cut)
                lineTo(w - cut, h); lineTo(cut, h); lineTo(0f, h - cut); lineTo(0f, cut)
                close()
            }
            drawPath(octagon, color = fill)
            drawPath(
                octagon,
                color = frame,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = label
        )
    }
}

/**
 * v5.15.0 — inline word-by-word chips flowing right-to-left like the printed
 * mushaf: each chip carries the arabic word above its Bangla meaning, taken
 * from the ported 77,425-entry wbw dataset (no guessed dictionary splits).
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WbwChipsInline(words: List<com.islamichub.app.data.local.WbwWord>) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides
            androidx.compose.ui.unit.LayoutDirection.Rtl
    ) {
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            words.forEach { word ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = word.ar,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = arabicSp(MaterialTheme.typography.titleMedium.fontSize)
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = word.tr,
                            style = MaterialTheme.typography.labelSmall,
                            color = UccharonTeal
                        )
                    }
                }
            }
        }
    }
}
