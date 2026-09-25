package com.islamichub.app.ui.screens.namaz_extras

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamazExtrasScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { NamazExtrasViewModel(container) }
    val state by vm.state.collectAsState()
    var selectedSurah by remember { mutableStateOf<com.islamichub.app.data.local.NamazSurah?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("অতিরিক্ত নামাজ" + " / Extra Prayers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val data = state.data
        if (state.isLoading || data == null) {
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { androidx.compose.material3.CircularProgressIndicator() }
            return@Scaffold
        }

        val extraCount = data.extraPrayers?.size ?: 0
        val duaCount = data.namazImportantDuas?.size ?: 0
        val hadithCount = data.koumiHadiths?.size ?: 0
        // v5.10.0 — the two remaining orphaned sections
        val namazDuaCount = data.namazDuas?.size ?: 0
        val allDuaItemCount = data.allDuas?.sumOf { it.items?.size ?: 0 } ?: 0
        // Stagger index bookkeeping — hero=0, then every card gets the next slot
        val duaHeaderIndex = extraCount + 1
        val hadithHeaderIndex = duaHeaderIndex + duaCount + 1
        val namazDuaHeaderIndex = hadithHeaderIndex + hadithCount + 1
        val allDuaHeaderIndex = namazDuaHeaderIndex + namazDuaCount + 1
        val surahHeaderIndex = allDuaHeaderIndex + allDuaItemCount + 1
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().staggerEntrance(0),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "বিশেষ নামাজসমূহ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "জুমআ, জানাযা, ঈদ, নফল, মুসাফির, রুগি — সব ধরনের নামাজের নিয়ম",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Extra prayers list
            data.extraPrayers?.entries?.forEachIndexed { idx, (id, prayer) ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(idx + 1),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = prayer.nameBn ?: prayer.nameEn ?: id,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            prayer.rakat?.let { rakat ->
                                Text(
                                    text = "$rakat রাকাআত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            prayer.time?.let { time ->
                                if (time.isNotBlank()) {
                                    Text(
                                        text = "সময়: $time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            prayer.description?.let { desc ->
                                if (desc.isNotBlank()) {
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            prayer.method?.let { method ->
                                if (method.isNotBlank()) {
                                    Text(
                                        text = "পদ্ধতি: $method",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // v5.9.0 — these duas existed in the JSON since the beginning but no section
            // ever rendered them. Namaz-only duas (সানা, তাশাহহুদ, কুনুত) in one place.
            if (duaCount > 0) {
                item {
                    PremiumSectionHeader(
                        title = "নামাজের গুরুত্বপূর্ণ দোয়া",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).staggerEntrance(duaHeaderIndex)
                    )
                }
            }
            data.namazImportantDuas?.forEachIndexed { idx, dua ->
                item(key = "dua_${dua.id}") {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(duaHeaderIndex + 1 + idx),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = dua.nameBn ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            dua.content?.arabic?.let { arabic ->
                                if (arabic.isNotBlank()) {
                                    Text(
                                        text = arabic,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = arabicSp(MaterialTheme.typography.titleLarge.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                            dua.content?.transliteration?.let { tr ->
                                if (tr.isNotBlank()) {
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            dua.content?.translation?.let { tr ->
                                if (tr.isNotBlank()) {
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // v5.9.0 — কওমি হাদিস সেকশন: the 10 hadiths shipped in the JSON but
            // never surfaced anywhere in the app.
            if (hadithCount > 0) {
                item {
                    PremiumSectionHeader(
                        title = "কওমি হাদিস",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).staggerEntrance(hadithHeaderIndex)
                    )
                }
            }
            data.koumiHadiths?.forEachIndexed { idx, hadith ->
                item(key = "hadith_${hadith.id}") {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(hadithHeaderIndex + 1 + idx),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                hadith.source?.let { src ->
                                    Text(
                                        "  $src",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            hadith.arabic?.let { ar ->
                                if (ar.isNotBlank()) {
                                    Text(
                                        text = ar,
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = arabicSp(MaterialTheme.typography.titleMedium.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                            hadith.transliteration?.let { tr ->
                                if (tr.isNotBlank()) {
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            hadith.translation?.let { tr ->
                                if (tr.isNotBlank()) {
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // v5.10.0 — the 13 namaz duas bundled in the JSON but never rendered
            if (namazDuaCount > 0) {
                item {
                    PremiumSectionHeader(
                        title = "নামাজের দোয়াসমূহ (তাওউয, সানা, কুনুত)",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).staggerEntrance(namazDuaHeaderIndex)
                    )
                }
            }
            data.namazDuas?.forEachIndexed { idx, dua ->
                item(key = "ndua_${dua.id}") {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(namazDuaHeaderIndex + 1 + idx),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = dua.nameBn ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            dua.content?.arabic?.let { arabic ->
                                if (arabic.isNotBlank()) {
                                    Text(
                                        text = arabic,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = arabicSp(MaterialTheme.typography.titleLarge.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                            dua.content?.transliteration?.let { tr ->
                                if (tr.isNotBlank()) {
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            dua.content?.translation?.let { tr ->
                                if (tr.isNotBlank()) {
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // v5.10.0 — categorized daily duas (ঘুম/রাস্তা/খাবার…) bundled but never rendered
            data.allDuas?.forEach { group ->
                val groupItems = group.items ?: emptyList()
                if (groupItems.isEmpty()) return@forEach
                item(key = "agroup_${group.category}") {
                    PremiumSectionHeader(
                        title = group.category ?: "দৈনন্দিন দোয়া",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).staggerEntrance(allDuaHeaderIndex)
                    )
                }
                groupItems.forEachIndexed { idx, item ->
                    item(key = "adua_${group.category}_$idx") {
                        Card(
                            modifier = Modifier.fillMaxWidth().staggerEntrance(allDuaHeaderIndex + 1 + idx),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.nameBn ?: "",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                item.content?.arabic?.let { arabic ->
                                    if (arabic.isNotBlank()) {
                                        Text(
                                            text = arabic,
                                            style = MaterialTheme.typography.titleLarge.copy(fontSize = arabicSp(MaterialTheme.typography.titleLarge.fontSize)),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                                item.content?.transliteration?.let { tr ->
                                    if (tr.isNotBlank()) {
                                        Text(
                                            text = tr,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                item.content?.translation?.let { tr ->
                                    if (tr.isNotBlank()) {
                                        Text(
                                            text = tr,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = banglaSp(MaterialTheme.typography.bodySmall.fontSize)),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Short surahs for namaz
            item {
                PremiumSectionHeader(
                    title = "নামাজের ছোট সূরা সমূহ",
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).staggerEntrance(surahHeaderIndex)
                )
            }
            data.namazSurahs?.forEachIndexed { idx, surah ->
                item(key = "surah_${surah.id}") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggerEntrance(surahHeaderIndex + 1 + idx)
                            .clip(RoundedCornerShape(16.dp))
                            .premiumTap { selectedSurah = surah },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📖", style = MaterialTheme.typography.titleMedium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = surah.nameBn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                surah.ayatCount?.let { count ->
                                    Text(
                                        text = "$count আয়াত • ট্যাপ করে বিস্তারিত দেখুন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ─── Full screen surah detail popup ───
    selectedSurah?.let { surah ->
        NamazSurahFullScreen(
            container = container,
            surah = surah,
            onClose = { selectedSurah = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NamazSurahFullScreen(
    container: AppContainer,
    surah: com.islamichub.app.data.local.NamazSurah,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surah.nameBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        surah.ayatCount?.let { count ->
                            Text(
                                text = "$count আয়াত",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arabic text
            surah.content?.arabic?.let { arabic ->
                if (arabic.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(0),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📖 আরবি",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = arabic,
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = arabicSp(MaterialTheme.typography.displaySmall.fontSize)),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            // Bangla pronunciation
            surah.pronunciationBn?.let { pron ->
                if (pron.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(1),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔊 বাংলা উচ্চারণ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(pron,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)),
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Transliteration (English)
            surah.content?.transliteration?.let { tr ->
                if (tr.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(2),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔤 ইংরেজি উচ্চারণ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(tr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Bangla translation (meaning)
            surah.content?.bangla?.let { bn ->
                if (bn.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().staggerEntrance(3),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📖 বাংলা অর্থ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                            Text(bn,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = banglaSp(MaterialTheme.typography.bodyLarge.fontSize)),
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }

            // Audio play button — v5.9.0 actually works now: the JSON points at remote
            // CDN recordings, so we stream the surah through the reciter-based player
            // (falls back to the raw URL via playUrl if the id is unknown).
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerEntrance(4)
                    .clip(RoundedCornerShape(16.dp))
                    .premiumTap {
                        val surahNumber = surah.id.filter { it.isDigit() }.toIntOrNull()
                        if (surahNumber != null && surahNumber in 1..114) {
                            // Full reciter system (selected qari, CDN streaming, cache)
                            container.audioController.playSurah(surahNumber)
                        } else {
                            surah.audioUrl?.let { audioUrl ->
                                container.audioController.playUrl(
                                    url = audioUrl,
                                    title = surah.nameBn,
                                    subtitle = "নামাজের সূরা"
                                )
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White)
                    Text("  সম্পূর্ণ সূরা শুনুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
