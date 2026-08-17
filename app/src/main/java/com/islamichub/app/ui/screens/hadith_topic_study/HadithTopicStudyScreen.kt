package com.islamichub.app.ui.screens.hadith_topic_study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.HadithTopic
import com.islamichub.app.data.repo.HadithTopicEntry
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.loadAssetImage

// ─── LIST SCREEN ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithTopicStudyListScreen(
    onBack: () -> Unit,
    onTopicClick: (String) -> Unit
) {
    val vm = remember { HadithTopicListViewModel() }
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("হাদিস বিষয়ভিত্তিক", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero
            item {
                PremiumHeroCard(
                    backgroundImage = "hadith-premium-bg.webp",
                    context = context,
                    height = 160
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("হাদিস বিষয়ভিত্তিক",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("একই থিমের সব হাদিস একসাথে",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MenuBook, contentDescription = null,
                                tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(16.dp))
                            Text("  ${state.topics.size}টি বিষয় • ৪টি কালেকশন (২৪,৪২৪ হাদিস)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.95f))
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = vm::updateSearch,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("বিষয় সার্চ করুন: ঈমান, নামাজ, সবর…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
                    singleLine = true
                )
            }

            // Domain filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        DomainChip(
                            label = "সব বিষয়",
                            isSelected = state.selectedDomain == null,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { vm.selectDomain(null) }
                        )
                    }
                    items(state.domains) { domain ->
                        DomainChip(
                            label = domain,
                            isSelected = state.selectedDomain == domain,
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = { vm.selectDomain(domain) }
                        )
                    }
                }
            }

            // Topic cards
            items(state.filteredTopics) { topic ->
                HadithTopicCard(topic = topic, context = context) { onTopicClick(topic.slug) }
            }

            if (state.filteredTopics.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("কোনো বিষয় পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun HadithTopicCard(topic: HadithTopic, context: android.content.Context, onClick: () -> Unit) {
    val bgBitmap = remember(topic.slug) { loadAssetImage(context, "img/${topic.iconHint}") }
    val accent = Color(topic.accentColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            if (bgBitmap != null) {
                Image(
                    bitmap = bgBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        colors = listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.55f))
                    )
                ))
            } else {
                Box(modifier = Modifier.fillMaxSize().background(accent))
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(topic.nameBn,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text(topic.nameEn,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.9f))
                    Text(topic.nameAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.95f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MenuBook, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("  ${topic.domain}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// ─── DETAIL SCREEN ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithTopicStudyDetailScreen(
    container: AppContainer,
    topicSlug: String,
    onBack: () -> Unit
) {
    val vm = remember { HadithTopicDetailViewModel(container) }
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(topicSlug) { vm.load(topicSlug) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(state.topic?.nameBn ?: "বিষয়", fontWeight = FontWeight.Bold, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Text("হাদিস খোঁজা হচ্ছে…",
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
                Text(state.error ?: "বিষয় পাওয়া যায়নি",
                    style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val accent = Color(topic.accentColor)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium topic header
            item {
                val bgBitmap = remember { loadAssetImage(context, "img/hadith-premium-bg.webp") }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        if (bgBitmap != null) {
                            Image(
                                bitmap = bgBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.95f))
                                )
                            ))
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(colors = listOf(accent, accent.copy(alpha = 0.8f)))
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
                                Icon(Icons.Filled.MenuBook, contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(16.dp))
                                Text("  ${state.totalCount}টি হাদিস পাওয়া গেছে",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.95f))
                            }
                        }
                    }
                }
            }

            // Overview
            item { SectionHeader("OVERVIEW", "সংক্ষিপ্ত পরিচিতি", accent) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(topic.overviewBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(20.dp))
                }
            }

            // Stats by collection
            if (state.byCollection.isNotEmpty()) {
                item { SectionHeader("📚 COLLECTIONS", "কালেকশন অনুযায়ী", accent) }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            state.byCollection.forEach { (name, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Book, contentDescription = null,
                                            tint = accent, modifier = Modifier.size(16.dp))
                                        Text("  $name",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accent)
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("$count", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Hadiths
            item { SectionHeader("📜 HADITHS", "সম্পর্কিত হাদিস", accent) }
            items(state.hadiths) { hadith ->
                HadithCard(
                    hadith = hadith,
                    accent = accent,
                    isExpanded = state.expandedHadithRef == hadith.reference,
                    onToggle = { vm.toggleHadithExpand(hadith.reference) }
                )
            }

            // Related concepts
            if (topic.relatedConcepts.isNotEmpty()) {
                item { SectionHeader("🔗 CONCEPTS", "সম্পর্কিত ধারণা", accent) }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            topic.relatedConcepts.forEach { concept ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
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

            // Source attribution
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("তথ্যসূত্র",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("Bundled Sahih Hadith collections: Bukhari (7,589), Muslim (7,563), Tirmidhi (3,998), Abu Dawud (5,274)। বিষয় অনুযায়ী কিওয়ার্ড ম্যাচিং দ্বারা ফিল্টার।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(label: String, titleBn: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).height(20.dp).clip(CircleShape).background(accent))
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, color = accent)
            Text(titleBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun HadithCard(
    hadith: HadithTopicEntry,
    accent: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
                .padding(20.dp)
        ) {
            // Header: reference + collection + relevance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Book, contentDescription = null,
                        tint = accent, modifier = Modifier.size(16.dp))
                    Text("  ${hadith.reference}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = accent)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("রিলিভেন্স: ${hadith.relevanceScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Arabic text
            if (hadith.arabic.isNotBlank()) {
                Text(hadith.arabic,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            // Bangla translation (truncated if not expanded)
            Text(
                text = if (isExpanded) hadith.bangla
                       else if (hadith.bangla.length > 180) hadith.bangla.take(180) + "…"
                       else hadith.bangla,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Expandable: full hadith + grade
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(accent.copy(alpha = 0.3f)))
                    Spacer(Modifier.height(12.dp))
                    // Grade
                    hadith.grades?.firstOrNull()?.let { grade ->
                        grade["name"]?.let { scholar ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, contentDescription = null,
                                    tint = accent, modifier = Modifier.size(16.dp))
                                Text("  $scholar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
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
