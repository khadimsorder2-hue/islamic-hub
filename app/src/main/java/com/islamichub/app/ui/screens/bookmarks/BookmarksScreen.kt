package com.islamichub.app.ui.screens.bookmarks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.repo.Bookmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onBookmarkClick: (Int) -> Unit
) {
    val bookmarks by remember { container.bookmarkRepository.bookmarks }.collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.bookmarks_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp))
                Text(stringResource(R.string.bookmarks_empty), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Premium hero showing count
                item {
                    com.islamichub.app.ui.components.PremiumHeroCard(
                        backgroundImage = "quran-premium-bg.webp",
                        context = context,
                        height = 120
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Bookmark, contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(32.dp))
                            Column {
                                Text("${bookmarks.size}টি বুকমার্ক", style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                                Text("সংরক্ষিত আয়াতসমূহ", style = MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }
                }

                // Group bookmarks by surah
                val grouped = bookmarks.groupBy { it.surahNumber }
                grouped.forEach { (surahNum, surahBookmarks) ->
                    item {
                        com.islamichub.app.ui.components.PremiumSectionHeader(
                            title = surahBookmarks.firstOrNull()?.surahName ?: "Surah $surahNum"
                        )
                    }
                    items(surahBookmarks, key = { "${it.surahNumber}:${it.ayahNumber}" }) { bm ->
                        BookmarkCard(bm, onClick = { onBookmarkClick(bm.surahNumber) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(bookmark: Bookmark, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${bookmark.surahName} • Ayah ${bookmark.ayahNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = bookmark.arabicSnippet,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 2
            )
            Text(
                text = bookmark.surahNameBn,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
