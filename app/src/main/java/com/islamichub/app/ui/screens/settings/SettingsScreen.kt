package com.islamichub.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.data.repo.AutoPauseOption
import com.islamichub.app.data.repo.BackgroundMode
import com.islamichub.app.data.repo.TafsirSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onShowQariSelector: () -> Unit
) {
    val vm = remember { SettingsViewModel(container) }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Premium hero
            item {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                com.islamichub.app.ui.components.PremiumHeroCard(
                    backgroundImage = "topics-premium-bg.webp",
                    context = ctx,
                    height = 120
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("অ্যাপ কনফিগারেশন",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // ─── Quran Appearance ─────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_quran_appearance)) {
                    // Font size slider
                    Text(
                        text = stringResource(R.string.settings_font_size) + ": ${"%.0f%%".format(state.quranFontScale * 100)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = state.quranFontScale,
                        onValueChange = vm::setQuranFontScale,
                        valueRange = 0.7f..2.0f,
                        steps = 12
                    )
                    Spacer(8.dp)

                    // Background mode chips
                    Text(
                        text = stringResource(R.string.settings_background),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackgroundMode.values().forEach { mode ->
                            FilterChip(
                                selected = state.backgroundMode == mode,
                                onClick = { vm.setBackgroundMode(mode) },
                                label = { Text(mode.label) }
                            )
                        }
                    }
                    Spacer(8.dp)

                    // Show toggles
                    ToggleRow(
                        label = stringResource(R.string.settings_show_arabic),
                        checked = state.showArabic,
                        onCheckedChange = vm::setShowArabic
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_show_bangla),
                        checked = state.showBangla,
                        onCheckedChange = vm::setShowBangla
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_show_english),
                        checked = state.showEnglish,
                        onCheckedChange = vm::setShowEnglish
                    )
                }
            }

            // ─── Audio ────────────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_audio)) {
                    // Reciter selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onShowQariSelector),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎧",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_reciter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = AudioController.availableRecitersStatic
                                        .firstOrNull { it.editionId == state.selectedReciter }?.displayName
                                        ?: state.selectedReciter,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(8.dp)

                    // Auto pause
                    Text(
                        text = stringResource(R.string.settings_auto_pause),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoPauseOption.values().forEach { opt ->
                            FilterChip(
                                selected = state.autoPause == opt,
                                onClick = { vm.setAutoPause(opt) },
                                label = { Text(opt.label) }
                            )
                        }
                    }
                    Spacer(8.dp)

                    ToggleRow(
                        label = stringResource(R.string.settings_bn_audio),
                        checked = state.banglaAudioEnabled,
                        onCheckedChange = vm::setBanglaAudioEnabled
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_word_audio),
                        checked = state.wordByWordAudioEnabled,
                        onCheckedChange = vm::setWordByWordAudioEnabled
                    )
                }
            }

            // ─── Tafsir ───────────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_tafsir)) {
                    Text(
                        text = stringResource(R.string.settings_tafsir_source),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TafsirSource.values().forEach { src ->
                            FilterChip(
                                selected = state.tafsirSource == src,
                                onClick = { vm.setTafsirSource(src) },
                                label = { Text(src.displayNameBn) }
                            )
                        }
                    }
                }
            }

            // ─── AI Scholar ───────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_ai_scholar)) {
                    // Provider selector
                    Text(
                        text = "AI Provider নির্বাচন করুন",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("gemini" to "Gemini", "openrouter" to "OpenRouter", "openai" to "OpenAI").forEach { (id, label) ->
                            FilterChip(
                                selected = state.aiProvider == id,
                                onClick = { vm.setAiProvider(id) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(8.dp)

                    // API Key
                    OutlinedTextField(
                        value = state.aiApiKey,
                        onValueChange = vm::setAiApiKey,
                        label = { Text(stringResource(R.string.settings_ai_api_key)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(8.dp)
                    // Base URL
                    OutlinedTextField(
                        value = state.aiBaseUrl,
                        onValueChange = vm::setAiBaseUrl,
                        label = { Text(stringResource(R.string.settings_ai_base_url)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(8.dp)
                    // Model
                    OutlinedTextField(
                        value = state.aiModel,
                        onValueChange = vm::setAiModel,
                        label = { Text(stringResource(R.string.settings_ai_model)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(8.dp)
                    Text(
                        text = if (state.aiApiKey.isNotBlank())
                            "✓ " + stringResource(R.string.settings_ai_configured)
                            else stringResource(R.string.settings_ai_not_configured),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.aiApiKey.isNotBlank())
                            MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                    )
                    Spacer(8.dp)
                    val helpText = when (state.aiProvider) {
                        "gemini" -> "Gemini API key নিন: https://aistudio.google.com/apikey (ফ্রি)। Model: gemini-2.5-flash বা gemini-2.0-flash।"
                        "openrouter" -> "OpenRouter key নিন: https://openrouter.ai/keys (ফ্রি tier আছে)। Model উদাহরণ: stepfun/step-3.5-flash:free।"
                        else -> "OpenAI key নিন: https://platform.openai.com/api-keys। Model উদাহরণ: gpt-4o-mini।"
                    }
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ─── Firebase ─────────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_firebase)) {
                    ToggleRow(
                        label = stringResource(R.string.settings_firebase_enabled),
                        checked = state.firebaseEnabled,
                        onCheckedChange = vm::setFirebaseEnabled
                    )
                    Spacer(8.dp)
                    Text(
                        text = "Firebase চালু করলে আপনার app-এ analytics, crash reporting এবং remote config কাজ করবে।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ─── Cache ────────────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_cache)) {
                    Text(
                        text = formatCacheSize(state.cacheSizeBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(8.dp)
                    OutlinedButton(
                        onClick = vm::clearCache,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Cached, contentDescription = null)
                        Text("  " + stringResource(R.string.settings_clear_cache))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    body: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            body()
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun Spacer(height: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(
        modifier = Modifier.size(height)
    )
}

private fun formatCacheSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes / 1024.0 / 1024.0)} MB"
    }
}
