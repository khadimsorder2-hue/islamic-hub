package com.islamichub.app.ui.screens.hadith

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.HadithTopic
import com.islamichub.app.data.local.TopicHadith
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.staggerEntrance
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.toBanglaDigits

/**
 * v5.18.0 — 1-10 trusted score for a thematic hadith (user: "1-10 trusted
 * score"). Grade first, then the collection in the reference line.
 * 10 = Sahih Bukhari/Muslim (muttafaq alayh), 8-9 = other sahih-grade
 * collections, 7-8 = hasan, ≤5 = weak/fabricated signals.
 */
internal fun hadithTrustScore(grade: String?, reference: String?): Int {
    val g = (grade ?: "").lowercase()
    val r = reference ?: ""
    return when {
        g.contains("সহিহ") || g.contains("sahih") -> 10
        g.contains("হাসান") || g.contains("hasan") -> 8
        g.contains("মাউদূ") || g.contains("mawdu") || g.contains("মাওদূ") -> 1
        g.contains("যঈফ") || g.contains("দুর্বল") || g.contains("daif") || g.contains("zaif") -> 3
        else -> when {
            r.contains("বুখারী") || r.contains("বুখারি") || r.contains("Bukhari", true) -> 10
            r.contains("মুসলিম") || r.contains("Muslim", true) -> 10
            r.contains("তিরমিজি") || r.contains("Tirmidhi", true) -> 8
            r.contains("আবু দাউদ") || r.contains("Abu Dawud", true) -> 8
            r.contains("নাসাঈ") || r.contains("Nasai", true) -> 8
            r.contains("আহমাদ") || r.contains("মুসনাদ") || r.contains("Ahmad", true) -> 8
            r.contains("বায়হাক্বী") || r.contains("Bayhaqi", true) -> 7
            r.contains("ইবনে মাজাহ") || r.contains("Ibn Majah", true) -> 7
            else -> 6
        }
    }
}

/** Badge color for a trust score. */
private fun trustColor(score: Int): Color = when {
    score >= 9 -> Color(0xFF0E7C5A)   // deep green — highest authenticity
    score >= 7 -> Color(0xFF2E7D32)   // green
    score >= 5 -> Color(0xFFF9A825)   // amber
    score >= 3 -> Color(0xFFEF6C00)   // orange
    else -> Color(0xFFC62828)         // red — weak/fabricated
}

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
            grouped.entries.forEachIndexed { groupIdx, (category, topics) ->
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
                        modifier = Modifier.fillMaxWidth().staggerEntrance(groupIdx),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                chunked.forEachIndexed { rowIdx, rowTopics ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().staggerEntrance(groupIdx + rowIdx),
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
        // v5.18.0 — per-hadith AI explanations (cached in-memory per topic open)
        val aiMap = remember(topic.id) {
            mutableStateOf<Map<Int, String>>(emptyMap())
        }
        val aiLoading = remember(topic.id) {
            mutableStateOf<Set<Int>>(emptySet())
        }
        val aiFailed = remember(topic.id) {
            mutableStateOf<Set<Int>>(emptySet())
        }
        AlertDialog(
            onDismissRequest = { selectedTopic = null },
            icon = {
                com.islamichub.app.ui.components.PremiumDialogIcon(
                    icon = Icons.Filled.Topic,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Column {
                    Text(topic.name ?: "", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    (topic.arabicName ?: "").let {
                        if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.titleSmall)
                    }
                }
            },
            text = {
                val clipboard = LocalClipboardManager.current
                val shareContext = LocalContext.current
                Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topic.hadiths?.forEach { hadith ->
                        val score = hadithTrustScore(hadith.grade, hadith.reference)
                        var aiOpen by remember(hadith.id) { mutableStateOf(false) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // ─── Trusted score badge (1-10) ───
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = trustColor(score),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "বিশ্বাসযোগ্যতা: ${score.toString().toBanglaDigits()}/১০",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = banglaSp(MaterialTheme.typography.labelSmall.fontSize)
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = trustColor(score)
                                    )
                                    hadith.grade?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "মান: $it",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                hadith.arabic?.let {
                                    Text(it, style = MaterialTheme.typography.titleMedium.copy(fontSize = arabicSp(MaterialTheme.typography.titleMedium.fontSize)),
                                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End,
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                                hadith.bangla?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                                hadith.explanation?.let {
                                    Text("📌 $it", style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                hadith.reference?.let {
                                    Text("সূত্র: $it", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }

                                // ─── v5.18.0 — copy / AI / share action row ───
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(onClick = {
                                        val text = buildString {
                                            append(hadith.arabic ?: "")
                                            append("\n\n")
                                            append(hadith.bangla ?: "")
                                            hadith.reference?.takeIf { it.isNotBlank() }?.let {
                                                append("\n\n— ")
                                                append(it)
                                            }
                                        }
                                        clipboard.setText(AnnotatedString(text))
                                        android.widget.Toast.makeText(
                                            shareContext, "হাদিস কপি হয়েছে",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "কপি",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp))
                                    }
                                    IconButton(onClick = {
                                        val text = buildString {
                                            append(hadith.arabic ?: "")
                                            append("\n\n")
                                            append(hadith.bangla ?: "")
                                            hadith.reference?.takeIf { it.isNotBlank() }?.let {
                                                append("\n\n— ")
                                                append(it)
                                            }
                                            append("\n\n— Islamic Hub থেকে শেয়ার করা")
                                        }
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        shareContext.startActivity(
                                            Intent.createChooser(intent, "হাদিস শেয়ার করুন")
                                        )
                                    }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Filled.Share, contentDescription = "শেয়ার",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp))
                                    }
                                    // AI explanation toggle
                                    IconButton(onClick = {
                                        aiOpen = !aiOpen
                                        if (aiOpen &&
                                            aiMap.value[hadith.id] == null &&
                                            hadith.id !in aiLoading.value
                                        ) {
                                            aiLoading.value = aiLoading.value + hadith.id
                                        }
                                    }, modifier = Modifier.size(34.dp)) {
                                        Icon(
                                            if (aiOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = "AI ব্যাখ্যা",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Text(
                                        "AI ব্যাখ্যা",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            aiOpen = !aiOpen
                                            if (aiOpen &&
                                                aiMap.value[hadith.id] == null &&
                                                hadith.id !in aiLoading.value
                                            ) {
                                                aiLoading.value = aiLoading.value + hadith.id
                                            }
                                        }
                                    )
                                }

                                // ─── AI explanation panel ───
                                if (aiOpen) {
                                    val explanation = aiMap.value[hadith.id]
                                    if (explanation != null) {
                                        Text(
                                            explanation,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                                                .padding(10.dp)
                                        )
                                    } else if (hadith.id in aiLoading.value) {
                                        // v5.18.0 — fire the AI request once while open
                                        LaunchedEffect(hadith.id, topic.id) {
                                            try {
                                                val prompt = """
এই হাদিসটি সহজ বাংলায় ব্যাখ্যা করুন:

আরবি: ${hadith.arabic ?: ""}
বাংলা অনুবাদ: ${hadith.bangla ?: ""}
সূত্র: ${hadith.reference ?: ""}
মান: ${hadith.grade ?: "উল্লেখ নেই"}

কাঠামো:
📖 মূল কথা (১-২ লাইন)
🕰️ প্রেক্ষাপট (কখন/কেন বলা হয়েছিল)
🌟 আমাদের জীবনে শিক্ষা (২-৩ পয়েন্ট)
🎯 এক লাইনে সারকথা

সহজ ভাষায় লিখুন যেন গ্রামের সাধারণ মানুষও বুঝতে পারে।""".trimIndent()
                                                val result = container.aiService.ask(
                                                    prompt, useCache = true, cacheType = "hadith_ai"
                                                )
                                                if (result.error == null) {
                                                    aiMap.value = aiMap.value + (hadith.id to result.answer)
                                                } else {
                                                    aiFailed.value = aiFailed.value + hadith.id
                                                }
                                            } catch (_: Exception) {
                                                aiFailed.value = aiFailed.value + hadith.id
                                            } finally {
                                                aiLoading.value = aiLoading.value - hadith.id
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "AI ব্যাখ্যা তৈরি হচ্ছে…",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else if (hadith.id in aiFailed.value) {
                                        Text(
                                            "AI ব্যাখ্যা পাওয়া যায়নি — ইন্টারনেট চেক করে আবার চেষ্টা করুন।",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).premiumTap(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
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
