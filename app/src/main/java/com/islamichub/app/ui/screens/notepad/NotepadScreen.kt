package com.islamichub.app.ui.screens.notepad

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.Note
import com.islamichub.app.ui.components.PremiumEmptyState
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import com.islamichub.app.ui.theme.toBanglaDigits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * v5.12.0 — Notepad list (ColorNote style).
 *
 * A colorful 2-column grid of the user's own notes with:
 *  - 8 categories (Quran / Hadith / Thematic / Dua / Todo / Question / Personal / Inspiration)
 *  - ColorNote-like pastel cards (light + dark variants)
 *  - Pin-to-top, search, per-category counts
 *  - FAB → NoteEditScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onEditNote: (String) -> Unit
) {
    val notes by remember { container.noteRepository.notes }.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val filtered = remember(notes, query, selectedCategory) {
        val q = query.trim()
        notes.filter { note ->
            val inCategory = selectedCategory == "all" || note.categoryId == selectedCategory
            val inQuery = q.isBlank() ||
                note.title.contains(q, ignoreCase = true) ||
                note.content.contains(q, ignoreCase = true)
            inCategory && inQuery
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "নোটপ্যাড",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEditNote("") },
                icon = {
                    Icon(Icons.Filled.Add, contentDescription = null)
                },
                text = {
                    Text(
                        "নতুন নোট",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Stats + search + category chips
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "মোট ${notes.size.toBanglaDigits()}টি নোট",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                NotepadSearchField(
                    query = query,
                    onQueryChange = { query = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryChipsRow(
                    notes = notes,
                    selectedCategory = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (notes.isEmpty()) {
                        PremiumEmptyState(
                            icon = Icons.Filled.EditNote,
                            title = "কোনো নোট নেই",
                            subtitle = "আপনার পছন্দের আয়াত, হাদিস, দুআ বা দৈনন্দিন করণীয় —\nসবকিছু লিখে রাখুন রঙিন নোটে",
                            ctaText = "প্রথম নোট লিখুন",
                            onCta = { onEditNote("") }
                        )
                    } else {
                        PremiumEmptyState(
                            icon = Icons.Filled.Search,
                            title = "কোনো নোট পাওয়া যায়নি",
                            subtitle = "অন্য শব্দ দিয়ে খুঁজুন বা ক্যাটাগরি বদলান",
                            ctaText = "ফিল্টার সরান",
                            onCta = {
                                query = ""
                                selectedCategory = "all"
                            }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(filtered, key = { _, note -> note.id }) { index, note ->
                        NoteCard(
                            note = note,
                            isDark = isDark,
                            index = index,
                            onClick = { onEditNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotepadSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("নোট খুঁজুন...", style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun CategoryChipsRow(
    notes: List<Note>,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    val counts = remember(notes) {
        val map = mutableMapOf<String, Int>()
        notes.forEach { note ->
            map[note.categoryId] = (map[note.categoryId] ?: 0) + 1
        }
        map
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            CategoryChip(
                label = "সব",
                emoji = "🗂️",
                count = notes.size,
                color = MaterialTheme.colorScheme.primary,
                selected = selectedCategory == "all",
                onClick = { onSelect("all") }
            )
        }
        items(NoteCategories.ALL) { category ->
            CategoryChip(
                label = category.label,
                emoji = category.emoji,
                count = counts[category.id] ?: 0,
                color = category.color,
                selected = selectedCategory == category.id,
                onClick = { onSelect(category.id) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    emoji: String,
    count: Int,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) color else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .premiumTap(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = emoji, fontSize = 13.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = fg
        )
        Text(
            text = count.toBanglaDigits(),
            style = MaterialTheme.typography.labelSmall,
            color = fg.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    isDark: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    val cardColor = NoteColors.colorAt(note.colorIndex, isDark)
    val onCard = NoteColors.onCard(isDark)
    val category = NoteCategories.byId(note.categoryId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggerEntrance(index)
            .premiumTap(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = note.title.ifBlank { "শিরোনামহীন নোট" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onCard,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.pinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        tint = onCard.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(15.dp)
                    )
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = onCard.copy(alpha = 0.85f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(onCard.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = category.emoji, fontSize = 11.sp)
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = onCard.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatNoteDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = onCard.copy(alpha = 0.65f)
                )
            }
        }
    }
}

/** "২৫ সেপ্টেম্বর, ২০২৬" — Bangla month names, Bangla digits. */
internal fun formatNoteDate(timestamp: Long): String {
    return try {
        val fmt = SimpleDateFormat("d MMMM, yyyy", Locale("bn"))
        fmt.format(Date(timestamp)).toBanglaDigits()
    } catch (_: Exception) {
        try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        } catch (_: Exception) {
            ""
        }
    }
}
