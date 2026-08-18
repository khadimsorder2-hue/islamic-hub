package com.islamichub.app.ui.screens.settings

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
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Brush
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
                SettingsSection(title = "থিম মোড") {
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
                                else MaterialTheme.colorScheme.surfaceVariant
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

            // ─── Firebase ─────────────────────────────────────────────
            item {
                SettingsSection(title = stringResource(R.string.settings_firebase)) {
                    // Premium Firebase status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = if (state.firebaseEnabled)
                                            listOf(Color(0xFFFF6F00), Color(0xFFFF8F00))
                                        else
                                            listOf(Color(0xFF9E9E9E), Color(0xFF757575))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (state.firebaseEnabled) "🔥" else "💤",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    Spacer(12.dp)
                                    Column {
                                        Text(
                                            text = if (state.firebaseEnabled) "সক্রিয়" else "নিষ্ক্রিয়",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (state.firebaseEnabled)
                                                "অ্যানালিটিক্স ও ক্র্যাশ রিপোর্ট চলছে"
                                            else "Firebase বন্ধ আছে",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.95f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = state.firebaseEnabled,
                                    onCheckedChange = vm::setFirebaseEnabled,
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color.White.copy(alpha = 0.4f),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                    Spacer(12.dp)

                    // Firebase features grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FirebaseFeatureCard(
                            icon = "📊",
                            title = "Analytics",
                            subtitle = "ব্যবহার পরিসংখ্যান",
                            color = Color(0xFF2E7D32),
                            enabled = state.firebaseEnabled,
                            modifier = Modifier.weight(1f)
                        )
                        FirebaseFeatureCard(
                            icon = "🐛",
                            title = "Crashlytics",
                            subtitle = "ক্র্যাশ রিপোর্ট",
                            color = Color(0xFFC62828),
                            enabled = state.firebaseEnabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(8.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FirebaseFeatureCard(
                            icon = "☁️",
                            title = "Cloud Sync",
                            subtitle = "ক্লাউড ব্যাকআপ",
                            color = Color(0xFF1565C0),
                            enabled = state.firebaseEnabled,
                            modifier = Modifier.weight(1f)
                        )
                        FirebaseFeatureCard(
                            icon = "🔔",
                            title = "Push",
                            subtitle = "নোটিফিকেশন",
                            color = Color(0xFF6D45C7),
                            enabled = state.firebaseEnabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(12.dp)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📋 Firebase সেটআপ গাইড:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(4.dp)
                            Text(
                                text = "১. Firebase Console এ প্রজেক্ট তৈরি করুন\n" +
                                       "২. google-services.json ডাউনলোড করুন\n" +
                                       "৩. অ্যাপ প্যাকেজ: com.islamichub.app\n" +
                                       "৪. Analytics ও Crashlytics চালু করুন\n" +
                                       "৫. উপরের টগল চালু করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

@Composable
private fun androidx.compose.foundation.layout.RowScope.FirebaseFeatureCard(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) color.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (enabled) color else Color(0xFF9E9E9E)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
            }
            Text(title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) color else Color(0xFF9E9E9E))
            Text(subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
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
