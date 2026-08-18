package com.islamichub.app.ui.screens.namaz

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.islamichub.app.data.local.ExtendedNamazItem
import com.islamichub.app.data.local.FullNamazCategory
import com.islamichub.app.data.local.FullNamazPrayer
import com.islamichub.app.data.local.FullNamazStep
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.components.loadAssetImage
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamazShikkhaScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { NamazShikkhaViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMistakes by remember { mutableStateOf(false) }
    var selectedPrayer by remember { mutableStateOf<FullNamazPrayer?>(null) }
    var selectedExtended by remember { mutableStateOf<ExtendedNamazItem?>(null) }
    var aiAnswer by remember { mutableStateOf<String?>(null) }
    var aiLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("নামাজ শিক্ষা") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMistakes = true }) {
                        Icon(Icons.Filled.Warning, contentDescription = "ভুলসমূহ")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { androidx.compose.material3.CircularProgressIndicator() }
            return@Scaffold
        }

        val data = state.fullData
        if (data == null) {
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ডেটা লোড করা যায়নি", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Premium Hero ───
            item {
                PremiumHeroCard(
                    backgroundImage = "namaz-premium-bg.webp",
                    context = context,
                    height = 200
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "নামাজ শিক্ষা",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "সম্পূর্ণ নামাজের নিয়ম ${state.selectedMadhhab} মাযহাব অনুযায়ী",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "৫ ওয়াক্ত ফরজ + সুন্নত + বিতর + জুমআ + ঈদ + জানাজা",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // ─── Madhab selector ───
            item {
                PremiumSectionHeader(title = "মাযহাব")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (data.madhhabOptions ?: listOf("হানাফী", "শাফেয়ী")).forEach { m ->
                        FilterChip(
                            selected = state.selectedMadhhab == m,
                            onClick = { vm.setMadhhab(m) },
                            label = { Text(m) }
                        )
                    }
                }
            }

            // ─── Gender selector ───
            item {
                PremiumSectionHeader(title = "লিঙ্গ")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (data.genderOptions ?: listOf("পুরুষ", "মহিলা")).forEach { g ->
                        FilterChip(
                            selected = state.selectedGender == g,
                            onClick = { vm.setGender(g) },
                            label = { Text(g) }
                        )
                    }
                }
            }

            // ─── 5 Daily Prayers (categories) ───
            val dailyCategories = data.categories?.filter { it.id in listOf("fajr", "dhuhr", "asr", "maghrib", "isha") } ?: emptyList()
            if (dailyCategories.isNotEmpty()) {
                item {
                    PremiumSectionHeader(title = "৫ ওয়াক্ত নামাজ")
                }
                items(dailyCategories.size, key = { idx -> dailyCategories[idx].id ?: idx }) { idx ->
                    val category = dailyCategories[idx]
                    NamazCategoryCard(category, context) { prayer ->
                        selectedPrayer = prayer
                    }
                }
            }

            // ─── Additional prayers (extended) ───
            if (state.extendedNamaz.isNotEmpty()) {
                item {
                    PremiumSectionHeader(title = "অতিরিক্ত নামাজ")
                }
                items(state.extendedNamaz.size) { idx ->
                    val item = state.extendedNamaz[idx]
                    ExtendedNamazCard(item, context) {
                        selectedExtended = item
                    }
                }
            }

            // ─── AI Help ───
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (state.apiKeyConfigured) {
                                aiLoading = true
                                aiAnswer = null
                                scope.launch {
                                    val prompt = "নামাজে সাধারণ ভুলগুলো কী কী এবং কীভাবে ঠিক করা যায়? ${state.selectedMadhhab} মাযহাব অনুযায়ী বলুন।"
                                    val result = container.aiService.ask(prompt)
                                    aiLoading = false
                                    if (result.error == null) aiAnswer = result.answer
                                    else aiAnswer = "ত্রুটি: ${result.error}"
                                }
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.apiKeyConfigured)
                            MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null,
                            tint = if (state.apiKeyConfigured) MaterialTheme.colorScheme.onSecondaryContainer
                                   else MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI দিয়ে নামাজের ভুল জিজ্ঞাসা করুন",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.apiKeyConfigured) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.apiKeyConfigured) "নামাজে ভুল হলে কী করবেন — AI জিজ্ঞেস করুন"
                                      else "Settings এ AI API key কনফিগার করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.apiKeyConfigured) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (aiLoading) {
                item { androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            }
            aiAnswer?.let { answer ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(answer, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }
    }

    // ─── Prayer detail FULL SCREEN popup (replaces AlertDialog) ───
    selectedPrayer?.let { prayer ->
        val steps = vm.getStepsForPrayer(prayer)
        androidx.compose.material3.Scaffold(
            topBar = {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = prayer.nameBn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${steps.size}টি ধাপ • অডিও সহ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedPrayer = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                steps.forEachIndexed { idx, step ->
                    NamazStepRow(step, state.selectedGender, idx + 1) { audioFile ->
                        // Use shared AudioController → FloatingAudioPlayer shows automatically
                        container.audioController.playAssetAudio(
                            assetPath = "namaz_audio/$audioFile",
                            title = "${prayer.nameBn} — ধাপ ${idx + 1}",
                            subtitle = "নামাজ শিক্ষা"
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ─── Extended namaz detail dialog ───
    selectedExtended?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedExtended = null },
            title = { Text(item.name) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    item.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 12.dp)) }
                    item.importance?.let { Text("গুরুত্ব: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp)) }
                    item.steps?.forEach { step ->
                        Text("${step.step}. ${step.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                        step.arabic?.let { Text(it, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) }
                        step.pronunciation?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        step.meaning?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedExtended = null }) { Text("বন্ধ করুন") } }
        )
    }

    // ─── Mistakes dialog ───
    if (showMistakes) {
        AlertDialog(
            onDismissRequest = { showMistakes = false },
            title = { Text("নামাজের সাধারণ ভুলসমূহ") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    NamazStepsData.commonMistakes.forEachIndexed { idx, mistake ->
                        Text("${idx + 1}. $mistake", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMistakes = false }) { Text("বন্ধ করুন") } }
        )
    }
}

@Composable
private fun NamazCategoryCard(
    category: FullNamazCategory,
    context: android.content.Context,
    onPrayerClick: (FullNamazPrayer) -> Unit
) {
    val bgImage = when (category.id) {
        "fajr" -> "namaz-fajr-bg.webp"
        "dhuhr" -> "namaz-dhuhr-bg.webp"
        "asr" -> "namaz-asr-bg.webp"
        "maghrib" -> "namaz-maghrib-bg.webp"
        "isha" -> "namaz-isha-bg.webp"
        else -> "namaz-premium-bg.webp"
    }
    val bgBitmap = remember(category.id) { loadAssetImage(context, "img/$bgImage") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Premium header with bg image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                if (bgBitmap != null) {
                    Image(
                        bitmap = bgBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
                }
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(category.icon ?: "🕌", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = category.nameBn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Prayer chips
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                category.prayers?.forEach { prayer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPrayerClick(prayer) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(prayer.totalRakats?.toString() ?: "•",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = prayer.nameBn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = prayer.type ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(Icons.Filled.PlayArrow, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtendedNamazCard(
    item: ExtendedNamazItem,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val bgBitmap = remember(item.id) { loadAssetImage(context, "img/namaz-premium-bg.webp") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🕌", style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                item.rakats?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary) }
                item.time?.let { Text("সময়: $it", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Icon(Icons.Filled.PlayArrow, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun NamazStepRow(
    step: FullNamazStep,
    gender: String,
    index: Int,
    onPlayAudio: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("$index.", style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                text = step.nameBn ?: "",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            step.audioUrl?.let { audioUrl ->
                val fileName = audioUrl.replace("namaz-audio/", "")
                if (fileName.isNotBlank()) {
                    IconButton(onClick = { onPlayAudio(fileName) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        step.content?.arabic?.let {
            if (it.isNotBlank()) {
                Text(it, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
        }
        step.content?.transliteration?.let {
            if (it.isNotBlank()) {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        step.content?.translation?.let {
            if (it.isNotBlank()) {
                Text(it, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
        // Gender-specific notes
        step.genderNotes?.get(gender)?.let { note ->
            if (note.isNotBlank()) {
                Text("📋 $note", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
