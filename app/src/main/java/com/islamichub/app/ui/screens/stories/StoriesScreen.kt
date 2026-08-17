package com.islamichub.app.ui.screens.stories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.FullProphet
import com.islamichub.app.data.local.FullKhalifa
import com.islamichub.app.data.local.FullStoryChapter
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.components.loadAssetImage
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { StoriesViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var selectedProphet by remember { mutableStateOf<FullProphet?>(null) }
    var selectedKhalifa by remember { mutableStateOf<FullKhalifa?>(null) }
    var selectedChapter by remember { mutableStateOf<Pair<String, FullStoryChapter>?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ইসলামিক গল্প") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(modifier = Modifier.padding(padding).fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero
            item {
                PremiumHeroCard(
                    backgroundImage = "stories-premium-bg.webp",
                    context = context,
                    height = 180
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("ইসলামিক গল্প",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("নবী, রাসূল, খুলাফায়ে রাশেদীন, মে'রাজ ও সীরাত",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // ─── Prophets ───
            if (state.prophets.isNotEmpty()) {
                item { PremiumSectionHeader(title = "নবী ও রাসূলগণের জীবনী") }
                items(state.prophets, key = { it.id }) { prophet ->
                    StoryCardProphet(prophet, context) { selectedProphet = prophet }
                }
            }

            // ─── Khalifas ───
            if (state.khalifas.isNotEmpty()) {
                item { PremiumSectionHeader(title = "খুলাফায়ে রাশেদীন") }
                items(state.khalifas, key = { it.id }) { khalifa ->
                    StoryCardKhalifa(khalifa, context) { selectedKhalifa = khalifa }
                }
            }

            // ─── Meraj ───
            if (state.merajChapters.isNotEmpty()) {
                item { PremiumSectionHeader(title = state.merajTitle) }
                items(state.merajChapters, key = { it.id ?: it.title ?: "" }) { chapter ->
                    ChapterCard(chapter, context) {
                        selectedChapter = state.merajTitle to chapter
                    }
                }
            }

            // ─── Sirat ───
            if (state.siratChapters.isNotEmpty()) {
                item { PremiumSectionHeader(title = state.siratTitle) }
                items(state.siratChapters.take(10), key = { it.id ?: it.title ?: "" }) { chapter ->
                    ChapterCard(chapter, context) {
                        selectedChapter = state.siratTitle to chapter
                    }
                }
                if (state.siratChapters.size > 10) {
                    item {
                        Text("এবং আরও ${state.siratChapters.size - 10}টি অধ্যায়...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }

    // Prophet detail
    selectedProphet?.let { prophet ->
        AlertDialog(
            onDismissRequest = { selectedProphet = null },
            title = { Text(prophet.name ?: "", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(prophet.title ?: "", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(prophet.summary ?: "", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp))
                    Text(prophet.details ?: "", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp))
                    prophet.ref?.let { Text("সূত্র: $it", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            confirmButton = { TextButton(onClick = { selectedProphet = null }) { Text("বন্ধ করুন") } }
        )
    }

    // Khalifa detail
    selectedKhalifa?.let { khalifa ->
        AlertDialog(
            onDismissRequest = { selectedKhalifa = null },
            title = { Text(khalifa.name ?: "", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(khalifa.title ?: "", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(khalifa.summary ?: "", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp))
                    Text(khalifa.details ?: "", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp))
                }
            },
            confirmButton = { TextButton(onClick = { selectedKhalifa = null }) { Text("বন্ধ করুন") } }
        )
    }

    // Chapter detail
    selectedChapter?.let { (sectionTitle, chapter) ->
        AlertDialog(
            onDismissRequest = { selectedChapter = null },
            title = { Text(chapter.title ?: "", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(chapter.content ?: "", style = MaterialTheme.typography.bodyMedium)
                    chapter.highlight?.let {
                        if (it.isNotBlank()) {
                            Text(it, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedChapter = null }) { Text("বন্ধ করুন") } }
        )
    }
}

@Composable
private fun StoryCardProphet(
    prophet: FullProphet,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val bgBitmap = remember(prophet.id) { loadAssetImage(context, "img/prophets-premium-bg.webp") }
    StoryCardBase(
        title = prophet.name ?: "",
        subtitle = prophet.title ?: "",
        summary = prophet.summary ?: "",
        bgBitmap = bgBitmap,
        onClick = onClick
    )
}

@Composable
private fun StoryCardKhalifa(
    khalifa: FullKhalifa,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val bgBitmap = remember(khalifa.id) { loadAssetImage(context, "img/khalifas-premium-bg.webp") }
    StoryCardBase(
        title = khalifa.name ?: "",
        subtitle = khalifa.title ?: "",
        summary = khalifa.summary ?: "",
        bgBitmap = bgBitmap,
        onClick = onClick
    )
}

@Composable
private fun ChapterCard(
    chapter: FullStoryChapter,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val bgBitmap = remember(chapter.id) { loadAssetImage(context, "img/stories-premium-bg.webp") }
    StoryCardBase(
        title = chapter.title ?: "",
        subtitle = chapter.content?.take(80) + if ((chapter.content?.length ?: 0) > 80) "…" else "",
        summary = "",
        bgBitmap = bgBitmap,
        onClick = onClick
    )
}

@Composable
private fun StoryCardBase(
    title: String,
    subtitle: String,
    summary: String,
    bgBitmap: android.graphics.Bitmap?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            if (bgBitmap != null) {
                Image(bitmap = bgBitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f))
                    )
                ))
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
            }
            Row(modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center) {
                    Text(title.take(1), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = Color.White)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f), maxLines = 2)
                    }
                }
            }
        }
    }
}
