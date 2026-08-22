package com.islamichub.app.ui.screens.tafsir

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import androidx.compose.ui.text.input.TextFieldValue
import android.widget.Toast
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirFullScreen(
    container: AppContainer,
    surah: Int,
    ayah: Int,
    onClose: () -> Unit
) {
    val vm = remember { TafsirViewModel(container, surah, ayah) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showNoteEditor by remember { mutableStateOf(false) }
    val stripHtml = { s: String -> s.replace(Regex("<[^>]*>"), "").trim() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(
                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Bolt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.size(8.dp))
                        Column {
                            Text("তাফসীর", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${state.surahName} • আয়াত ${state.ayahNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    // Copy button
                    IconButton(onClick = {
                        val text = "${state.arabicText}\n\nবাংলা: ${state.banglaText}\n\nEnglish: ${state.englishText}"
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "কপি হয়েছে", Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Filled.ContentCopy, contentDescription = "কপি") }
                    // Share button
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT,
                                "${state.surahName} (আয়াত ${state.ayahNumber}):\n\n${state.arabicText}\n\nবাংলা: ${state.banglaText}\n\nEnglish: ${state.englishText}\n\n— Islamic Hub")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার"))
                    }) { Icon(Icons.Filled.Share, contentDescription = "শেয়ার") }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "বন্ধ করুন")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showNoteEditor) {
                FloatingActionButton(onClick = { showNoteEditor = true }) {
                    Icon(Icons.Filled.Save, contentDescription = "নোট")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Ayah Preview ───
            if (state.arabicText.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.arabicText, style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        if (state.banglaText.isNotBlank()) {
                            Text("বাংলা: ${state.banglaText}", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                        }
                        if (state.englishText.isNotBlank()) {
                            Text("English: ${state.englishText}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // ─── Transliteration ───
            state.transliteration?.let { translit ->
                if (translit.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔊 উচ্চারণ (Transliteration)", style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(translit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // ─── All Translations (Bangla + English) ───
            if (state.allTranslations.isNotEmpty()) {
                Text("📖 অনুবাদ (${state.allTranslations.count { it.language == "bn" }} বাংলা + ${state.allTranslations.count { it.language == "en" }} ইংরেজি)",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.allTranslations.size) { idx ->
                        val t = state.allTranslations[idx]
                        FilterChip(
                            selected = state.selectedTranslationIndex == idx,
                            onClick = { vm.selectTranslation(idx) },
                            label = { Text(t.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                if (state.selectedTranslationIndex < state.allTranslations.size) {
                    val sel = state.allTranslations[state.selectedTranslationIndex]
                    val bgColor = if (sel.language == "en") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    val txtColor = if (sel.language == "en") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    Surface(
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = bgColor
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(sel.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                                color = txtColor.copy(alpha = 0.8f))
                            Text(stripHtml(sel.text), style = MaterialTheme.typography.bodyLarge, color = txtColor)
                        }
                    }
                }
            }

            // ─── All Tafsirs (Bangla + English) ───
            if (state.allTafsirs.isNotEmpty()) {
                Text("📚 তাফসীর (${state.allTafsirs.count { it.language == "bn" }} বাংলা + ${state.allTafsirs.count { it.language == "en" }} ইংরেজি)",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.allTafsirs.size) { idx ->
                        val t = state.allTafsirs[idx]
                        val isEn = t.language == "en"
                        FilterChip(
                            selected = state.selectedTafsirIndex == idx,
                            onClick = { vm.selectTafsir(idx) },
                            label = { Text(t.name, style = MaterialTheme.typography.labelSmall) },
                            colors = if (isEn) androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            ) else androidx.compose.material3.FilterChipDefaults.filterChipColors()
                        )
                    }
                }
                if (state.selectedTafsirIndex < state.allTafsirs.size) {
                    val sel = state.allTafsirs[state.selectedTafsirIndex]
                    if (sel.text.isNotBlank()) {
                        val bgColor = if (sel.language == "en") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        val txtColor = if (sel.language == "en") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                        Surface(
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = bgColor
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                                            .background(if (sel.language == "en") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center) {
                                        Text(if (sel.language == "en") "En" else "বাং", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                    Text("  ${sel.name}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = txtColor)
                                }
                                Text(stripHtml(sel.text), style = MaterialTheme.typography.bodyMedium, color = txtColor)
                            }
                        }
                    } else if (sel.language == "en") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text("English tafsir লোড হচ্ছে…", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            // ─── Offline Tafsir ───
            if (state.tafsirText != null) {
                Text("তাফসীর উৎস (অফলাইন)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (state.isCached) {
                            Text("✓ অফলাইনে সংরক্ষিত", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(state.tafsirText!!, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // ─── AI Explanation ───
            Text("🤲 AI তাফসীর (খতিবের ভাষায়)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (state.isAILoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator()
                        Text("AI তাফসীর তৈরি করছে…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            state.aiExplanation?.let { explanation ->
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center) { Text("🤲", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                            Text("  AI স্কলার", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            if (state.aiExplanation == null && !state.isAILoading) {
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text("AI তাফসীর ব্যবহার করতে Settings এ API key যোগ করুন",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }

            // ─── Notes Section ───
            if (showNoteEditor) {
                Text("📝 নোট", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.noteText,
                            onValueChange = vm::updateNote,
                            placeholder = { Text("এই আয়াত সম্পর্কে নোট লিখুন…") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 8, shape = RoundedCornerShape(12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (state.isNoteSaved) {
                                Text("✓ সংরক্ষিত", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    if (state.isNoteSaving) "সংরক্ষণ হচ্ছে…" else "সংরক্ষণ করুন",
                                    modifier = Modifier.clickable(enabled = !state.isNoteSaving) { vm.saveNote() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White
                                )
                            }
                        }
                    }
                }
            } else if (state.noteText.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📝 আপনার নোট", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(state.noteText, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}