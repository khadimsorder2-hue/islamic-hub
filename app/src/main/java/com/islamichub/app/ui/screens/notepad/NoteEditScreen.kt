package com.islamichub.app.ui.screens.notepad

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.Note
import com.islamichub.app.ui.components.PremiumDialogIcon
import com.islamichub.app.ui.theme.premiumTap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * v5.12.0 — Note editor (create / edit).
 *
 *  - Auto-saves when leaving the screen (ColorNote behaviour); an emptied
 *    existing note is discarded, a brand-new empty note is never saved.
 *  - Category picker (8 categories) + 8-color ColorNote palette.
 *  - Pin toggle, share as text, delete with confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditScreen(
    container: AppContainer,
    noteId: String?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Load (or create) the note once.
    var loaded by remember { mutableStateOf<Note?>(null) }
    LaunchedEffect(noteId) {
        loaded = if (noteId == null) {
            Note(id = UUID.randomUUID().toString())
        } else {
            container.noteRepository.get(noteId)
                ?: Note(id = UUID.randomUUID().toString())
        }
    }

    val note = loaded
    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        }
        return
    }

    var title by remember(note.id) { mutableStateOf(note.title) }
    var content by remember(note.id) { mutableStateOf(note.content) }
    var categoryId by remember(note.id) { mutableStateOf(note.categoryId) }
    var colorIndex by remember(note.id) { mutableStateOf(note.colorIndex) }
    var pinned by remember(note.id) { mutableStateOf(note.pinned) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isNew = noteId == null

    fun currentNote(): Note = note.copy(
        title = title,
        content = content,
        categoryId = categoryId,
        colorIndex = colorIndex,
        pinned = pinned,
        updatedAt = System.currentTimeMillis()
    )

    fun persistAndExit() {
        scope.launch {
            val hasText = title.isNotBlank() || content.isNotBlank()
            if (hasText) {
                container.noteRepository.upsert(currentNote())
            } else if (!isNew) {
                // Emptied an existing note → discard it (ColorNote behaviour)
                container.noteRepository.delete(note.id)
            }
            onBack()
        }
    }

    fun shareNote() {
        try {
            val text = buildString {
                if (title.isNotBlank()) append(title)
                if (content.isNotBlank()) {
                    if (isNotEmpty()) append("\n\n")
                    append(content)
                }
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "নোট শেয়ার করুন"))
        } catch (_: Exception) {
            // No share target available — silently ignore
        }
    }

    fun deleteNote() {
        scope.launch {
            container.noteRepository.delete(note.id)
            onBack()
        }
    }

    // Hardware/system back also auto-saves
    BackHandler { persistAndExit() }

    // Auto-focus the title for brand-new notes
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(note.id) {
        if (isNew) {
            try {
                delay(350)
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isNew) "নতুন নোট" else "নোট সম্পাদনা",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { persistAndExit() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { pinned = !pinned }) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = if (pinned) "Unpin" else "Pin",
                            tint = if (pinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { shareNote() }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    if (!isNew) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text("শিরোনাম", style = MaterialTheme.typography.titleMedium)
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("এখানে লিখুন...\n\nযেমন: পছন্দের আয়াত, হাদিসের উদ্ধৃতি, দুআ, বা দৈনন্দিন করণীয় তালিকা")
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 10,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category picker ───
            Text(
                text = "ক্যাটাগরি",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteCategories.ALL.forEach { category ->
                    val selected = category.id == categoryId
                    CategoryPickChip(
                        label = category.label,
                        emoji = category.emoji,
                        color = category.color,
                        selected = selected,
                        onClick = { categoryId = category.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Color picker ───
            Text(
                text = "রং",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoteColors.palette(isDark).forEachIndexed { index, color ->
                    ColorPickCircle(
                        color = color,
                        selected = index == colorIndex,
                        isDark = isDark,
                        onClick = { colorIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "সর্বশেষ সম্পাদনা: ${formatNoteDate(note.updatedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                PremiumDialogIcon(
                    icon = Icons.Filled.Delete,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("নোট ডিলিট করবেন?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    if (title.isNotBlank()) "\"$title\" নোটটি স্থায়ীভাবে মুছে যাবে।"
                    else "এই নোটটি স্থায়ীভাবে মুছে যাবে।"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteNote()
                }) {
                    Text("ডিলিট", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun CategoryPickChip(
    label: String,
    emoji: String,
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
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun ColorPickCircle(
    color: Color,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF37474F)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) {
                    Modifier.border(3.dp, ringColor, CircleShape)
                } else {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        CircleShape
                    )
                }
            )
            .premiumTap(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = ringColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
