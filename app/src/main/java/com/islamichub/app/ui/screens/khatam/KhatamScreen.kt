package com.islamichub.app.ui.screens.khatam

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.KhatamHistoryEntry
import com.islamichub.app.data.repo.KhatamPeriodStat
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.theme.AppElevation
import com.islamichub.app.ui.theme.AppRadius
import com.islamichub.app.ui.theme.PremiumCountUpText
import com.islamichub.app.ui.theme.PremiumProgressBar
import com.islamichub.app.ui.theme.premiumPulseHighlight
import com.islamichub.app.ui.theme.staggerEntrance

/** A khatam finished in ≤30 days is "Ramadan pace" (a full Quran in one month). */
private const val RAMADAN_PACE_DAYS = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatamScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onSurahClick: (Int) -> Unit
) {
    val vm = remember { KhatamViewModel(container) }
    val state by vm.state.collectAsState()
    val khatam = state.currentKhatam
    val progressPercent = state.progressPercent
    val completedCount = state.completedSurahCount
    val context = LocalContext.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val khatamShareText = stringResource(R.string.khatam_share_text)
    val khatamTitle = stringResource(R.string.khatam_title)
    val khatamStart = stringResource(R.string.khatam_start)
    val khatamShare = stringResource(R.string.khatam_share)

    // Pre-compute para data + colors (these can be at @Composable scope)
    val surahsByPara = remember { ParaSurahMap.surahsByPara() }
    val paraColors = listOf(
        Color(0xFF6D45C7), Color(0xFF1B5E20), Color(0xFFC9A34E),
        Color(0xFF1565C0), Color(0xFFD84315), Color(0xFF00897B),
        Color(0xFF8D6E63), Color(0xFFEF6C00)
    )
    // Most recently completed surah (highest completed number) gets the pulse highlight.
    val lastCompletedSurah = khatam?.completedSurahs?.maxOrNull()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(khatamTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (khatam != null) {
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT,
                                    khatamShareText + "\n\n" +
                                    "Progress: ${"%.1f".format(progressPercent * 100)}% ($completedCount/114 surahs)"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Khatam"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Premium Hero Progress Card ───
            item {
                PremiumHeroCard(
                    backgroundImage = "premium-quran-bg.webp",
                    context = context,
                    height = 260
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top: icon + title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "কুরআন খতম",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Middle: percentage
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${"%.1f".format(progressPercent * 100)}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "সম্পন্ন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Bottom: stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            KhatamStatItem(
                                value = "$completedCount",
                                label = "সূরা সম্পন্ন",
                                total = "/ 114"
                            )
                            KhatamStatItem(
                                value = "${(progressPercent * 6236).toInt()}",
                                label = "আয়াত পড়া",
                                total = "/ 6236"
                            )
                        }

                        // Progress bar (animated fill)
                        PremiumProgressBar(
                            progress = progressPercent,
                            modifier = Modifier.fillMaxWidth(),
                            fillColor = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            barHeight = 6.dp
                        )
                    }
                }
            }

            // ─── Start / Reset / Khatam Player ───
            if (khatam == null) {
                item {
                    Button(
                        onClick = { vm.startNew() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.AutoStories, contentDescription = null)
                        Text("  $khatamStart", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                item {
                    Text(
                        text = "নতুন খতম শুরু করে আপনার কুরআন পড়ার অগ্রগতি ট্র্যাক করুন। সূরা একের পর এক পড়ুন এবং ১১৪ সূরা সম্পন্ন করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                // Khatam Player button — also available before khatam starts (auto-creates a khatam)
                item {
                    OutlinedButton(
                        onClick = {
                            // Initialize khatam if not exists, then start player
                            if (khatam == null) {
                                vm.startNew()
                            }
                            container.audioController.startKhatamPlayer(1)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Text("  খতম প্লেয়ার শুরু করুন (অটো-ইনিশিয়ালাইজ)",
                            modifier = Modifier.padding(start = 8.dp))
                    }
                }
            } else {
                // Khatam Player button
                item {
                    Button(
                        onClick = {
                            container.audioController.startKhatamPlayer(1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("  খতম প্লেয়ার শুরু করুন", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                // Reset button (preserves history — only clears in-progress khatam)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.reset() },
                            modifier = Modifier.weight(1f)
                        ) { Text("রিসেট") }
                        if (khatam.isComplete) {
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, khatamShareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Khatam"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null)
                                Text("  $khatamShare", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                // ─── Para-wise surah list (3-column grid, no image) ───
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text("  পারা অনুযায়ী তালিকা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                }

                // Group surahs by para (1-30)
                surahsByPara.forEach { (paraNum, surahsInPara) ->
                    // Para header card
                    item {
                        val paraCompletedSurahs = surahsInPara.count { khatam.completedSurahs.contains(it) }
                        val paraProgress = if (surahsInPara.isNotEmpty()) paraCompletedSurahs.toFloat() / surahsInPara.size else 0f
                        val paraColor = paraColors[(paraNum - 1) % paraColors.size]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(paraColor, paraColor.copy(alpha = 0.7f))
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("পারা $paraNum",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White)
                                        }
                                        Spacer(Modifier.size(12.dp))
                                        Column {
                                            Text(
                                                if (paraProgress >= 1f) "✓ সম্পূর্ণ" else "${surahsInPara.size} সূরা",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text("$paraCompletedSurahs / ${surahsInPara.size} সম্পন্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.9f))
                                        }
                                    }
                                    // Progress percentage badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.25f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("${(paraProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // 3-column surah grid for this para
                    val chunkedSurahs = surahsInPara.chunked(3)
                    chunkedSurahs.forEach { rowSurahs ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSurahs.forEach { surahNum ->
                                    val isCompleted = khatam.completedSurahs.contains(surahNum)
                                    KhatamSurahGridCard(
                                        surahNumber = surahNum,
                                        isCompleted = isCompleted,
                                        isLastCompleted = isCompleted && surahNum == lastCompletedSurah,
                                        paraColor = paraColors[(paraNum - 1) % paraColors.size],
                                        modifier = Modifier.weight(1f),
                                        onClick = { onSurahClick(surahNum) }
                                    )
                                }
                                repeat(3 - rowSurahs.size) {
                                    Box(modifier = Modifier.weight(1f)) {}
                                }
                            }
                        }
                    }
                }
            }

            // ─── আমার খতম ইতিহাস (permanent history — shown regardless of in-progress state) ───
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text("  আমার খতম ইতিহাস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
            }

            // Hero stat: total completed + average days taken
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggerEntrance(0),
                    shape = RoundedCornerShape(AppRadius.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.low)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PremiumCountUpText(
                                targetValue = state.totalKhatamsCompleted.toFloat(),
                                format = { it.toInt().toString() },
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "সম্পন্ন খতম",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.averageDaysTaken?.let {
                                    String.format(java.util.Locale.US, "%.1f", it)
                                } ?: "—",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "গড় সময় (দিন)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (state.history.isEmpty()) {
                item {
                    Text(
                        text = "এখনও কোনো খতম সম্পন্ন হয়নি। একটি খতম সম্পূর্ণ করলে এখানে স্থায়ীভাবে সংরক্ষিত হবে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            } else {
                // Timeline: one card per completed khatam, most recent first
                state.history.forEachIndexed { index, entry ->
                    item(key = "khatam_history_${entry.id}_$index") {
                        KhatamHistoryCard(entry = entry, index = index)
                    }
                }

                // Year-in-review: bars per year, driven by repository statsFor(YEAR)
                if (state.yearStats.isNotEmpty()) {
                    item {
                        KhatamYearReviewCard(yearStats = state.yearStats)
                    }
                }

                // Destructive: clear the permanent history (confirmation below)
                item {
                    TextButton(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("ইতিহাস মুছুন")
                    }
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("ইতিহাস মুছুন") },
            text = { Text("সত্যিই সব খতম ইতিহাস মুছে ফেলবেন? এটা ফেরানো যাবে না।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        vm.clearHistory()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("মুছে ফেলুন") }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

/** Formats an ISO "yyyy-MM-dd" date as "d MMM yyyy" in Bangla, with a safe fallback. */
private fun formatKhatamDate(dateStr: String): String {
    return try {
        java.time.LocalDate.parse(dateStr)
            .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale("bn")))
    } catch (t: Throwable) {
        // Old devices without java.time (< API 26) or malformed dates: show the raw value.
        dateStr
    }
}

@Composable
private fun KhatamHistoryCard(entry: KhatamHistoryEntry, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggerEntrance(index),
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.low)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✓",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${formatKhatamDate(entry.startDate)} → ${formatKhatamDate(entry.completionDate)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${entry.daysTaken} দিনে সম্পন্ন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (entry.daysTaken <= RAMADAN_PACE_DAYS) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.xs))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "রমজান গতি",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun KhatamYearReviewCard(yearStats: List<KhatamPeriodStat>) {
    val maxCount = yearStats.maxOfOrNull { it.khatamsCompleted }?.coerceAtLeast(1) ?: 1
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.low)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "বছরভিত্তিক সারসংক্ষেপ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            yearStats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(56.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stat.khatamsCompleted / maxCount.toFloat())
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "${stat.khatamsCompleted}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun KhatamStatItem(
    value: String,
    label: String,
    total: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = total,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.KhatamSurahGridCard(
    surahNumber: Int,
    isCompleted: Boolean,
    isLastCompleted: Boolean,
    paraColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .premiumPulseHighlight(
                active = isLastCompleted,
                color = paraColor,
                cornerRadius = 16.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 2.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isCompleted)
                            listOf(paraColor.copy(alpha = 0.25f), paraColor.copy(alpha = 0.1f))
                        else
                            listOf(paraColor.copy(alpha = 0.1f), paraColor.copy(alpha = 0.03f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Number badge (circular)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) paraColor else paraColor.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else surahNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "সূরা $surahNumber",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCompleted) paraColor
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
