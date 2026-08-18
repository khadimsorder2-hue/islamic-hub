package com.islamichub.app.ui.screens.hadith

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.HadithTopic
import com.islamichub.app.data.local.TopicHadith
import com.islamichub.app.ui.components.PremiumHeroCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithTopicsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { HadithTopicsViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var selectedTopic by remember { mutableStateOf<HadithTopic?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("হাদিস বিষয়াদি") },
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
                    backgroundImage = "hadith-premium-bg.webp",
                    context = context,
                    height = 160
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("হাদিস বিষয়াদি", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        val total = state.topics.sumOf { it.hadiths?.size ?: 0 }
                        Text("${state.topics.size} বিষয় • $total টি হাদিস",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // Group by category
            val grouped = state.topics.groupBy { it.category ?: "other" }
            grouped.forEach { (category, topics) ->
                val catName = when (category) {
                    "ibadah" -> "ইবাদত"
                    "muamalat" -> "লেনদেন"
                    "akhlaq" -> "আখলাক"
                    else -> "অন্যান্য"
                }
                val catColor = when (category) {
                    "ibadah" -> Color(0xFFC9A34E)
                    "muamalat" -> Color(0xFF1565C0)
                    "akhlaq" -> Color(0xFF8D6E63)
                    else -> Color(0xFF6D45C7)
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(catColor, catColor.copy(alpha = 0.7f))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📚 $catName",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("${topics.sumOf { it.hadiths?.size ?: 0 }} হাদিস",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                // 2-column grid of topic cards
                val chunked = topics.chunked(2)
                chunked.forEach { rowTopics ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTopics.forEach { topic ->
                                Box(modifier = Modifier.weight(1f)) {
                                    HadithTopicCard(topic, accent = catColor) { selectedTopic = topic }
                                }
                            }
                            if (rowTopics.size == 1) {
                                Box(modifier = Modifier.weight(1f)) {}
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail dialog
    selectedTopic?.let { topic ->
        AlertDialog(
            onDismissRequest = { selectedTopic = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(topic.arabicName ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(topic.name ?: "", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topic.hadiths?.forEach { hadith ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                hadith.arabic?.let {
                                    Text(it, style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End,
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                                hadith.bangla?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                                hadith.explanation?.let {
                                    Text("📌 $it", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                hadith.reference?.let {
                                    Text("সূত্র: $it", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                hadith.grade?.let {
                                    Text("মান: $it", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedTopic = null }) { Text("বন্ধ করুন") } }
        )
    }
}

@Composable
private fun HadithTopicCard(topic: HadithTopic, accent: Color = Color(0xFF6D45C7), onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.15f), accent.copy(alpha = 0.05f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top: icon + hadith count badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(topic.arabicName?.take(1) ?: "📖",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${topic.hadiths?.size ?: 0}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = accent)
                    }
                }
                // Topic name
                Text(topic.name ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2)
                // Description
                if (!topic.description.isNullOrBlank()) {
                    Text(topic.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2)
                }
                // Arabic name (footer)
                if (!topic.arabicName.isNullOrBlank()) {
                    Text(topic.arabicName ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
