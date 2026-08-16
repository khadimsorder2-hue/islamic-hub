package com.islamichub.app.ui.screens.namaz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.islamichub.app.data.local.NamazStep
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamazShikkhaScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { NamazShikkhaViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("নামাজ শিক্ষা" + " / Namaz Learning") },
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

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Madhab selector
            item {
                Text(
                    text = "মাযহাব (Madhab)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

            // Gender selector
            item {
                Text(
                    text = "লিঙ্গ (Gender)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

            // Prayer selector
            item {
                Text(
                    text = "নামাজ (Prayer)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "fajr" to "ফজর",
                        "dhuhr" to "যোহর",
                        "asr" to "আসর",
                        "maghrib" to "মাগরিব",
                        "isha" to "এশা"
                    ).forEach { (id, label) ->
                        FilterChip(
                            selected = state.selectedPrayer == id,
                            onClick = { vm.setPrayer(id) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Prayer info card
            val prayer = data.prayers?.get(state.selectedPrayer)
            if (prayer != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = prayer.nameBn ?: prayer.nameEn ?: state.selectedPrayer,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            prayer.rakat?.let { rakat ->
                                Text(
                                    text = "$rakat রাকাআত • ${state.selectedMadhhab} • ${state.selectedGender}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Steps list with audio
            val steps = prayer?.steps ?: data.commonSteps?.keys?.toList() ?: emptyList()
            items(steps) { stepId ->
                val step = data.commonSteps?.get(stepId) ?: return@items
                NamazStepCard(
                    step = step,
                    onPlayAudio = { audioFile ->
                        try {
                            // Play audio from assets folder
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                            // asset:/// URI scheme works with DefaultDataSource
                            val mediaItem = MediaItem.fromUri("asset:///namaz_audio/$audioFile")
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } catch (_: Exception) { /* ignore — audio missing */ }
                    }
                )
            }

            // Show extra info (fard, wajib, sunnah)
            if (prayer?.fard?.isNotEmpty() == true) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ফরয (Fard elements)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            prayer.fard.forEach { f ->
                                Text("• $f", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            if (prayer?.wajib?.isNotEmpty() == true) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ওয়াজিব (Wajib elements)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            prayer.wajib.forEach { w ->
                                Text("• $w", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NamazStepCard(
    step: NamazStep,
    onPlayAudio: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.nameBn ?: step.nameEn ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!step.audio.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .clickable { onPlayAudio(step.audio) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play audio",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            step.content?.arabic?.let { arabic ->
                if (arabic.isNotBlank()) {
                    Text(
                        text = arabic,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
            step.content?.transliteration?.let { tr ->
                if (tr.isNotBlank()) {
                    Text(
                        text = tr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            step.content?.translation?.let { tr ->
                if (tr.isNotBlank()) {
                    Text(
                        text = tr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            step.content?.bangla?.let { bn ->
                if (bn.isNotBlank()) {
                    Text(
                        text = bn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
