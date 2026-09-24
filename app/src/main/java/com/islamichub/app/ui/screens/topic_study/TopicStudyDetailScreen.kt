package com.islamichub.app.ui.screens.topic_study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.data.repo.Bookmark
import com.islamichub.app.data.repo.QuranTopicCatalog
import com.islamichub.app.data.repo.toShellThematicTopic
import com.islamichub.app.ui.components.loadAssetImage
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.englishSp
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicStudyDetailScreen(
    container: AppContainer,
    topicSlug: String,
    onBack: () -> Unit,
    onRelatedTopicClick: (String) -> Unit
) {
    val vm = remember { TopicDetailViewModel(container) }
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // v5.5 — bookmarked ayah references ("2:255") for the action chips
    val bookmarks by container.bookmarkRepository.bookmarks.collectAsState(initial = emptyList())
    val bookmarkedRefs = remember(bookmarks) {
        bookmarks.map { "${it.surahNumber}:${it.ayahNumber}" }.toSet()
    }

    // v5.5 — functional action-chip handlers
    val playAyahAudio: (Int, Int) -> Unit = { s, a ->
        scope.launch {
            val reciterId = container.settingsRepository.selectedReciter.first()
            val reciter = AudioController.availableRecitersStatic.firstOrNull {
                it.editionId == reciterId
            } ?: AudioController.availableRecitersStatic.first()
            container.audioController.playAyah(s, a, reciter)
        }
    }
    val toggleTopicBookmark: (com.islamichub.app.ui.screens.topic_study.ResolvedAyah) -> Unit = { a ->
        scope.launch {
            container.bookmarkRepository.toggle(
                Bookmark(
                    surahNumber = a.surahNumber,
                    ayahNumber = a.ayahNumber,
                    surahName = a.surahNameEn,
                    surahNameBn = a.surahNameBn,
                    arabicSnippet = a.arabic.take(120)
                )
            )
        }
    }
    val shareTopicAyah: (com.islamichub.app.ui.screens.topic_study.ResolvedAyah) -> Unit = { a ->
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "${a.surahNameEn} (${a.surahNameBn}) — আয়াত ${a.ayahNumber}:\n\n" +
                    "${a.arabic}\n\n" +
                    "বাংলা: ${a.bengali}\n\n" +
                    "English: ${a.english}\n\n" +
                    "— Islamic Hub থেকে শেয়ার করা হয়েছে"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "আয়াত শেয়ার করুন"))
    }

    // Trigger load on first composition
    LaunchedEffect(topicSlug) {
        vm.load(topicSlug)
    }

    // v5.7.0 — AI explanation popup (Thematic Quran AI)
    var showTopicAI by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        state.topic?.nameBn ?: "বিষয়",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // v5.7.0 — AI ব্যাখ্যা button
                    IconButton(onClick = { showTopicAI = true }) {
                        Icon(
                            Icons.Filled.Psychology,
                            contentDescription = "AI ব্যাখ্যা",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("আয়াত লোড হচ্ছে…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        val topic = state.topic ?: run {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("বিষয় পাওয়া যায়নি", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val accent = Color(topic.accentColor)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Premium topic header ────────────────────────
            item {
                val bgBitmap = remember { loadAssetImage(context, "img/premium-quran-bg.webp") }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (bgBitmap != null) {
                            Image(
                                bitmap = bgBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        accent.copy(alpha = 0.85f),
                                        accent.copy(alpha = 0.95f)
                                    )
                                )
                            ))
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(accent, accent.copy(alpha = 0.8f))
                                )
                            ))
                        }

                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(topic.nameBn,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text(topic.nameEn,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White.copy(alpha = 0.95f))
                            Text(topic.nameAr,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoStories, contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.95f),
                                    modifier = Modifier.size(16.dp))
                                Text("  ${topic.allAyahs.size}টি সম্পর্কিত আয়াত",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.95f))
                            }
                        }
                    }
                }
            }

            // ─── OVERVIEW ──────────────────────────────────────
            item {
                SectionHeader("OVERVIEW", "সংক্ষিপ্ত পরিচিতি", accent)
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = topic.overviewBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // ─── KEY AYAHS (Quran section preview) ────────────
            item {
                SectionHeader("📖 QURAN", "মূল আয়াত", accent)
            }
            itemsIndexed(state.resolvedKeyAyahs) { idx, ayah ->
                AyahCard(
                    ayah = ayah,
                    accent = accent,
                    container = container,
                    isBookmarked = ayah.reference in bookmarkedRefs,
                    onPlay = { playAyahAudio(ayah.surahNumber, ayah.ayahNumber) },
                    onBookmark = { toggleTopicBookmark(ayah) },
                    onShare = { shareTopicAyah(ayah) },
                    isExpanded = state.expandedAyahRef == ayah.reference,
                    onToggle = { vm.toggleAyahExpand(ayah.reference) },
                    modifier = Modifier.staggerEntrance(idx, enabled = state.resolvedKeyAyahs.size <= 20)
                )
            }

            // ─── TAFSIR (all ayahs with tafsir) ────────────────
            if (state.resolvedAllAyahs.size > state.resolvedKeyAyahs.size) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionHeader("📚 TAFSIR", "সকল আয়াত ও তাফসির", accent, modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(accent.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "মোট ${state.resolvedAllAyahs.size} আয়াত",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                items(state.resolvedAllAyahs.take(state.visibleAyahs)) { ayah ->
                    AyahCard(
                        ayah = ayah,
                        accent = accent,
                        container = container,
                        isBookmarked = ayah.reference in bookmarkedRefs,
                        onPlay = { playAyahAudio(ayah.surahNumber, ayah.ayahNumber) },
                        onBookmark = { toggleTopicBookmark(ayah) },
                        onShare = { shareTopicAyah(ayah) },
                        isExpanded = state.expandedAyahRef == ayah.reference,
                        onToggle = { vm.toggleAyahExpand(ayah.reference) }
                    )
                }
                // v5.3.1 — load-more for large keyword-engine result sets
                if (state.hasMoreAyahs) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { vm.loadMoreAyahs() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = accent.copy(alpha = 0.08f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "আরও ${state.resolvedAllAyahs.size - state.visibleAyahs}টি আয়াত দেখুন",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${state.visibleAyahs} / ${state.resolvedAllAyahs.size} দেখানো হচ্ছে",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ─── RELATED TOPICS ────────────────────────────────
            if (topic.relatedTopics.isNotEmpty()) {
                item {
                    SectionHeader("🌿 RELATED TOPICS", "সম্পর্কিত বিষয়", accent)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = accent.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topic.relatedTopics.forEach { slug ->
                                // v5.3.1 — look up related topics in BOTH the curated
                                // dataset and the keyword-driven catalog
                                val related = TopicStudyData.getTopic(slug)
                                    ?: QuranTopicCatalog.get(slug)?.toShellThematicTopic()
                                if (related != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onRelatedTopicClick(slug) }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(related.accentColor).copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.Spa, contentDescription = null,
                                                    tint = Color(related.accentColor), modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(related.nameBn,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold)
                                                // Shell topics have no resolved count yet — show category only
                                                if (related.allAyahs.isNotEmpty()) {
                                                    Text("${related.allAyahs.size} আয়াত • ${related.categoryBn}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                } else {
                                                    Text(related.categoryBn,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ─── RELATED STORIES ──────────────────────────────
            if (topic.relatedStories.isNotEmpty()) {
                item {
                    SectionHeader("📜 RELATED STORIES", "সম্পর্কিত গল্প", accent)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            topic.relatedStories.forEach { story ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp))
                                    Text("  $story",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                            }
                        }
                    }
                }
            }

            // ─── QURAN CONNECTIONS ───────────────────────────
            if (topic.relatedConcepts.isNotEmpty()) {
                item {
                    SectionHeader("🔗 QURAN CONNECTIONS", "কুরআনের সংযোগ", accent)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            topic.relatedConcepts.forEach { concept ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Text("  $concept",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // ─── Source attribution ─────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("তথ্যসূত্র",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Islamic.app Topics (CC BY 4.0) • Quranic Arabic Corpus • IslamicHub Bangla তাফসির সারসংক্ষেপ। আয়াত টেক্সট বান্ডেল করা পূর্ণ কুরআন ডাটাবেস থেকে নেওয়া।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // v5.7.0 — Thematic Quran AI explanation popup
    com.islamichub.app.ui.components.AIExplanationPopup(
        container = container,
        title = state.topic?.nameBn ?: "বিষয়ভিত্তিক কুরআন",
        question = if (state.topic != null)
            "\"${state.topic!!.nameBn}\" বিষয়টি সম্পর্কে কুরআন কী বলে? " +
                "সংক্ষেপ: ${state.topic!!.overviewBn.take(300)}\nপ্রাসঙ্গিক আয়াত: " +
                state.resolvedKeyAyahs.take(8).joinToString(", ") { it.reference }
        else "",
        context = "বিষয়ভিত্তিক (থিম্যাটিক) কুরআন স্টাডি",
        show = showTopicAI,
        onDismiss = { showTopicAI = false }
    )
}

@Composable
private fun SectionHeader(label: String, titleBn: String, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent)
            Text(titleBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun AyahCard(
    ayah: ResolvedAyah,
    accent: Color,
    container: AppContainer,
    isBookmarked: Boolean,
    onPlay: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // v5.5 — bundled Bangla uccaron, resolved lazily per card
    var uccaron by remember(ayah.reference) { mutableStateOf<String?>(null) }
    LaunchedEffect(ayah.reference) {
        try {
            uccaron = container.quranRepository.banglaUccaron(ayah.surahNumber, ayah.ayahNumber)
        } catch (_: Exception) {
            uccaron = null
        }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Header: reference + relation badge — ONLY the header toggles expand
            // (v5.5 bugfix: the whole card used to be clickable, so tapping the
            // tafsir content or the action chips collapsed it immediately)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null,
                        tint = accent, modifier = Modifier.size(16.dp))
                    Text("  ${ayah.reference}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent)
                    if (ayah.surahNameBn.isNotBlank()) {
                        Text("  •  ${ayah.surahNameBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(ayah.relation.bangla,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "সংকুচিত করুন" else "তাফসির দেখুন",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp).size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Arabic text
            if (ayah.arabic.isNotBlank()) {
                Text(ayah.arabic,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = arabicSp(MaterialTheme.typography.headlineSmall.fontSize)
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            // v5.5 — Bangla uccaron under the Arabic
            val uccaronText = uccaron
            if (!uccaronText.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accent.copy(alpha = 0.10f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("উচ্চারণ",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(uccaronText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
            }

            // Bengali translation
            if (ayah.bengali.isNotBlank()) {
                Text(ayah.bengali,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = banglaSp(MaterialTheme.typography.bodyLarge.fontSize)
                    ),
                    color = MaterialTheme.colorScheme.onSurface)
            }

            // Expandable: Tafsir + actions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(accent.copy(alpha = 0.3f))
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lightbulb, contentDescription = null,
                            tint = accent, modifier = Modifier.size(16.dp))
                        Text("  তাফসির",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accent)
                    }
                    Spacer(Modifier.height(8.dp))
                    // v5.5 — premium tafsir panel (was: plain text with a
                    // card-wide tap-to-collapse behind it)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = accent.copy(alpha = 0.07f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(ayah.tafsirBn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Reference line
                    if (ayah.surahNameEn.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Book, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp))
                            Text("  Reference: ${ayah.surahNameEn} ${ayah.reference}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = englishSp(MaterialTheme.typography.labelSmall.fontSize)
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Action row — v5.5: chips are now fully functional
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionChip(Icons.Filled.PlayArrow, "শোনো", accent,
                            onClick = onPlay)
                        ActionChip(
                            if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            "সংরক্ষণ", accent,
                            active = isBookmarked,
                            onClick = onBookmark)
                        ActionChip(Icons.Filled.Share, "শেয়ার", accent,
                            onClick = onShare)
                    }
                }
            }

            // Toggle indicator row — clickable, centered, mirrors the header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggle)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    active: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) accent.copy(alpha = 0.28f) else accent.copy(alpha = 0.1f))
            .premiumTap(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            Text(" $label",
                style = MaterialTheme.typography.labelSmall,
                color = accent, fontWeight = FontWeight.Medium)
        }
    }
}
