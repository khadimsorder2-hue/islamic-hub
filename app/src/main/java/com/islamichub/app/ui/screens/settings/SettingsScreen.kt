package com.islamichub.app.ui.screens.settings

import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.AudioController
import com.islamichub.app.data.repo.AutoPauseOption
import com.islamichub.app.data.repo.BackgroundMode
import com.islamichub.app.data.repo.TafsirSource
import com.islamichub.app.ui.theme.AppColors
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.englishSp
import com.islamichub.app.ui.theme.staggerEntrance

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

            // ─── Theme Mode ───────────────────────────────────────────
            item {
                SettingsSection(index = 0, title = "থিম মোড", icon = Icons.Filled.Palette, accent = Color(0xFF607D8B)) {
                    Text("অ্যাপের রঙ নির্বাচন করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeModeChip(
                            label = "অটো",
                            selected = state.themeMode == "auto",
                            color = Color(0xFF607D8B),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setThemeMode("auto") }
                        )
                        ThemeModeChip(
                            label = "লাইট",
                            selected = state.themeMode == "light",
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setThemeMode("light") }
                        )
                        ThemeModeChip(
                            label = "ডার্ক",
                            selected = state.themeMode == "dark",
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setThemeMode("dark") }
                        )
                        ThemeModeChip(
                            label = "ওয়ার্ম",
                            selected = state.themeMode == "warm_light",
                            color = Color(0xFFEF6C00),
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setThemeMode("warm_light") }
                        )
                    }
                    Spacer(8.dp)
                    Text(
                        text = when (state.themeMode) {
                            "light" -> "সাদা ব্যাকগ্রাউন্ড (স্ট্যান্ডার্ড লাইট মোড)"
                            "dark" -> "ডার্ক মোড (রাতের জন্য আরামদায়ক)"
                            "warm_light" -> "ওয়ার্ম হোয়াইট ব্যাকগ্রাউন্ড (চোখের জন্য নরম)"
                            else -> "সিস্টেম সেটিং অনুসরণ করবে"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ─── Quran Appearance ─────────────────────────────────────
            item {
                SettingsSection(index = 1, 
                    title = stringResource(R.string.settings_quran_appearance),
                    icon = Icons.Filled.MenuBook,
                    accent = Color(0xFF2E7D32)
                ) {
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

            // ─── Text Size (all pages) — v5.5 ─────────────────────────
            item {
                SettingsSection(index = 2, title = "লেখার সাইজ (সব পেজ)", icon = Icons.Filled.FormatSize, accent = Color(0xFF1565C0)) {
                    Text(
                        text = "বাংলা, ইংরেজি ও আরবি লেখার সাইজ কমিয়ে/বাড়িয়ে নিন — পরিবর্তন সব পেজে সাথে সাথে প্রয়োগ হবে।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(8.dp)

                    // Arabic size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕌 আরবি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.0f%%".format(state.arabicFontScale * 100),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = state.arabicFontScale,
                        onValueChange = vm::setArabicFontScale,
                        valueRange = 0.7f..1.8f,
                        steps = 10
                    )

                    // Bangla size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বাংলা",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.0f%%".format(state.banglaFontScale * 100),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = state.banglaFontScale,
                        onValueChange = vm::setBanglaFontScale,
                        valueRange = 0.7f..1.8f,
                        steps = 10
                    )

                    // English size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.0f%%".format(state.englishFontScale * 100),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = state.englishFontScale,
                        onValueChange = vm::setEnglishFontScale,
                        valueRange = 0.7f..1.8f,
                        steps = 10
                    )
                    Spacer(8.dp)

                    // Bangla uccaron visibility toggle
                    ToggleRow(
                        label = "বাংলা উচ্চারণ দেখাও (কুরআনের আয়াতের নিচে)",
                        checked = state.showTransliteration,
                        onCheckedChange = vm::setShowTransliteration
                    )
                    Spacer(8.dp)

                    // Live preview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "প্রিভিউ:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(6.dp)
                            Text(
                                text = "بِسْمِ ٱللَّهِ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = arabicSp(MaterialTheme.typography.titleLarge.fontSize)
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                            Text(
                                text = "বিসমিল্লাহির রাহমানির রাহীম",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "In the name of Allah.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = englishSp(MaterialTheme.typography.bodySmall.fontSize)
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ─── Audio ────────────────────────────────────────────────
            item {
                SettingsSection(index = 3, title = stringResource(R.string.settings_audio), icon = Icons.Filled.Headphones, accent = Color(0xFFEF6C00)) {
                    // Reciter selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onShowQariSelector),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
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

                    // Honesty fix (v5.3.1): the Bangla meaning audio CDN/AI pipeline is
                    // not implemented yet — the old toggle silently did nothing and reset
                    // itself, confusing users. It now shows an explicit "coming soon" note
                    // instead of pretending to work.
                    ToggleRow(
                        label = stringResource(R.string.settings_bn_audio),
                        sublabel = stringResource(R.string.settings_bn_audio_coming_soon),
                        checked = state.banglaAudioEnabled,
                        onCheckedChange = vm::setBanglaAudioEnabled
                    )
                    ToggleRow(
                        label = stringResource(R.string.settings_word_audio),
                        sublabel = stringResource(R.string.settings_word_audio_coming_soon),
                        checked = state.wordByWordAudioEnabled,
                        onCheckedChange = vm::setWordByWordAudioEnabled
                    )
                }
            }

            // ─── Tafsir ───────────────────────────────────────────────
            item {
                SettingsSection(index = 4, title = stringResource(R.string.settings_tafsir), icon = Icons.Filled.AutoStories, accent = Color(0xFF00695C)) {
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
                SettingsSection(index = 5, title = stringResource(R.string.settings_ai_scholar), icon = Icons.Filled.AutoAwesome, accent = Color(0xFF6D45C7)) {
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
                                onClick = {
                                    vm.setAiProvider(id)
                                    // Auto-fill recommended free model for provider
                                    com.islamichub.app.data.repo.AIModelPresets.recommended(id)?.let { preset ->
                                        vm.setAiModel(preset.modelName)
                                        vm.setAiBaseUrl(preset.baseUrl)
                                    }
                                },
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
                    Spacer(12.dp)

                    // Model preset chips (free models pre-filled)
                    Text(
                        text = "প্রসেট ফ্রি মডেল (ট্যাপ করে সিলেক্ট করুন)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(4.dp)
                    val presets = com.islamichub.app.data.repo.AIModelPresets.forProvider(state.aiProvider)
                    presets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    vm.setAiModel(preset.modelName)
                                    vm.setAiBaseUrl(preset.baseUrl)
                                }
                                .border(
                                    width = if (state.aiModel == preset.modelName) 2.dp else 0.dp,
                                    color = if (state.aiModel == preset.modelName)
                                        MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.aiModel == preset.modelName)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(preset.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold)
                                            if (preset.recommended) {
                                                Spacer(8.dp)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("সুপারিশকৃত",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(preset.modelName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    }
                                    if (preset.isFree) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ফ্রি",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(4.dp)
                                Text(preset.descriptionBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Context: ${preset.contextWindow}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                    }
                    Spacer(12.dp)

                    // Custom model input (if user wants to use non-preset model)
                    Text(
                        text = "কাস্টম মডেল নাম (প্রসেট ব্যতীত)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(4.dp)
                    OutlinedTextField(
                        value = state.aiModel,
                        onValueChange = vm::setAiModel,
                        label = { Text(stringResource(R.string.settings_ai_model)) },
                        placeholder = { Text("যেমন: gemini-2.5-flash") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(8.dp)

                    // Status
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
                    Spacer(12.dp)

                    // AI Cache section
                    Text(
                        text = "AI ক্যাশ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(4.dp)
                    Text(
                        text = "সব AI উত্তর স্বয়ংক্রিয়ভাবে ক্যাশে সংরক্ষিত হয়। পরবর্তী একই প্রশ্নে তাৎক্ষণিক উত্তর পাবেন।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(8.dp)
                    OutlinedButton(
                        onClick = vm::clearAICache,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error)
                        Text("  AI ক্যাশ মুছুন (${state.cacheCount})",
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ─── Security / App Lock (v5.9.0) ─────────────────────────
            item {
                SettingsSection(index = 8, title = stringResource(R.string.settings_security), icon = Icons.Filled.Lock, accent = Color(0xFF37474F)) {
                    // v5.9.0 — the App Lock feature is now fully wired: the toggle
                    // persists via SettingsRepository and MainActivity gates the whole
                    // app behind BiometricPrompt on the next launch.
                    ToggleRow(
                        label = stringResource(R.string.settings_app_lock),
                        sublabel = stringResource(R.string.settings_app_lock_sub),
                        checked = state.appLockEnabled,
                        onCheckedChange = vm::setAppLockEnabled
                    )
                }
            }

            // ─── Notifications (v5.10.0) ──────────────────────────────
            item {
                val ctx = LocalContext.current
                SettingsSection(index = 9, title = "নোটিফিকেশন", icon = Icons.Filled.Notifications, accent = Color(0xFF455A64)) {
                    ToggleRow(
                        label = "নামাজের সময় নোটিফিকেশন",
                        sublabel = "পাঁচ ওয়াক্ত নামাজের সময় হলে নোটিফিকেশন পাবেন",
                        checked = state.prayerNotificationsEnabled,
                        onCheckedChange = { enabled ->
                            vm.setPrayerNotifications(enabled) { on ->
                                if (!on) {
                                    Toast.makeText(ctx, "নামাজের নোটিফিকেশন বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(ctx, "নামাজের নোটিফিকেশন চালু হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    ToggleRow(
                        label = "দৈনিক আয়াত নোটিফিকেশন",
                        sublabel = "প্রতিদিন সকালে একটি আয়াত ও অর্থ পাবেন",
                        checked = state.dailyAyahEnabled,
                        onCheckedChange = { enabled ->
                            vm.setDailyAyah(enabled) { on ->
                                Toast.makeText(
                                    ctx,
                                    if (on) "দৈনিক আয়াত চালু হয়েছে" else "দৈনিক আয়াত বন্ধ করা হয়েছে",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    ToggleRow(
                        label = "পড়ার সময় স্ক্রিন চালু রাখুন",
                        sublabel = "কুরআন/তাফসীর পড়ার সময় স্ক্রিন নিভে যাবে না",
                        checked = state.keepScreenOnReading,
                        onCheckedChange = vm::setKeepScreenOnReading
                    )
                }
            }

            // ─── App Update (v5.6.0) ──────────────────────────────────
            item {
                SettingsSection(index = 6, title = stringResource(R.string.settings_update), icon = Icons.Filled.SystemUpdate, accent = Color(0xFF2E7D32)) {
                    // Current version + manual check button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বর্তমান ভার্সন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v${state.currentVersion}".ifBlank { "v—" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val checking = state.updateStatus is UpdateStatus.Checking
                        OutlinedButton(
                            onClick = vm::checkForUpdate,
                            enabled = !checking
                        ) {
                            if (checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Cached, contentDescription = null)
                            }
                            Spacer(4.dp)
                            Text("চেক করুন")
                        }
                    }
                    Spacer(12.dp)

                    // Status area
                    when (val st = state.updateStatus) {
                        is UpdateStatus.Checking -> {
                            Text(
                                text = "সর্বশেষ ভার্সন খোঁজা হচ্ছে…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is UpdateStatus.UpToDate -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.success.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "✓ আপনার অ্যাপ সর্বশেষ ভার্সনে আছে",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.success,
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                )
                            }
                        }
                        is UpdateStatus.Available -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "🎉 নতুন ভার্সন পাওয়া গেছে — v${st.update.latestVersion}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (st.update.releaseNotes.isNotBlank()) {
                                        Text(
                                            text = st.update.releaseNotes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 6
                                        )
                                    }
                                    androidx.compose.material3.Button(
                                        onClick = { vm.openUpdateDownload(st.update.downloadUrl) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("⬇  আপডেট ডাউনলোড করুন")
                                    }
                                    TextButton(
                                        onClick = vm::dismissUpdateStatus,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "এই ভার্সনটাই থাকবে",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        is UpdateStatus.Error -> {
                            Column {
                                Text(
                                    text = "⚠ আপডেট চেক করা যায়নি (${st.message})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(onClick = vm::checkForUpdate) {
                                    Text("আবার চেষ্টা করুন")
                                }
                            }
                        }
                        is UpdateStatus.Idle -> { /* nothing yet */ }
                    }

                    Spacer(8.dp)
                    Text(
                        text = "অ্যাপ সরাসরি GitHub Release থেকে আপডেট হয় — ডাউনলোড শেষে ফাইলটি ওপেন করে ইনস্টল করুন। অ্যাপ চালু হলেও মাঝে মাঝে স্বয়ংক্রিয়ভাবে চেক করা হয়।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ─── Cache ────────────────────────────────────────────────
            item {
                SettingsSection(index = 7, title = stringResource(R.string.settings_cache), icon = Icons.Filled.CleaningServices, accent = Color(0xFF5D4037)) {
                    Text(
                        text = formatCacheSize(state.cacheSizeBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(8.dp)
                    OutlinedButton(
                        onClick = {
                            vm.clearCache()
                            Toast.makeText(
                                LocalContext.current,
                                "✓ ক্যাশ পরিষ্কার হয়েছে",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    index: Int = 0,
    body: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().staggerEntrance(index)) {
        // v5.8.0 — More-section style header: circular gradient icon badge +
        // title with accent underline, sitting on an invisible (borderless,
        // shadowless) container so only the icon gives the section identity.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(accent, accent.copy(alpha = 0.65f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(12.dp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(width = 36.dp, height = 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    accent,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }
        }
        Spacer(12.dp)
        // Invisible container: same white as canvas, zero elevation, no border
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                body()
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    sublabel: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

@Composable
private fun androidx.compose.foundation.layout.RowScope.ThemeModeChip(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (selected) color else Color(0xFF9E9E9E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (label) {
                        "অটো" -> "🌓"
                        "লাইট" -> "☀️"
                        "ডার্ক" -> "🌙"
                        "ওয়ার্ম" -> "🕯️"
                        else -> "🎨"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
