package com.islamichub.app.ui.screens.stories

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.StoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { StoriesViewModel(container) }
    val state by vm.state.collectAsState()
    var selectedStory by remember { mutableStateOf<StoryItem?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ইসলামিক গল্প" + " / Stories") },
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
            // Prophets section
            data.prophets?.let { prophets ->
                if (prophets.isNotEmpty()) {
                    item {
                        Text(
                            text = "নবী ও রাসূলগণের জীবনী",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(prophets, key = { it.id }) { story ->
                        StoryCard(story) { selectedStory = story }
                    }
                }
            }

            // Khalifas section
            data.khalifas?.let { khalifas ->
                if (khalifas.isNotEmpty()) {
                    item {
                        Text(
                            text = "খুলাফায়ে রাশেদীন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    items(khalifas, key = { it.id }) { story ->
                        StoryCard(story) { selectedStory = story }
                    }
                }
            }

            // Miraj
            data.miraj?.let { miraj ->
                item {
                    Text(
                        text = "মে'রাজের ঘটনা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                item { StoryCard(miraj) { selectedStory = miraj } }
            }

            // Sirat
            data.sirat?.let { sirat ->
                item {
                    Text(
                        text = "সীরাত সারসংক্ষেপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                item { StoryCard(sirat) { selectedStory = sirat } }
            }
        }
    }

    // Story detail dialog
    selectedStory?.let { story ->
        AlertDialog(
            onDismissRequest = { selectedStory = null },
            title = { Text(story.titleBn ?: story.title ?: story.nameBn ?: story.name ?: "") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    story.period?.let { p ->
                        if (p.isNotBlank()) {
                            Text(
                                text = "কাল: $p",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = story.content ?: story.story ?: story.description ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedStory = null }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@Composable
private fun StoryCard(story: StoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (story.nameBn ?: story.titleBn ?: "?").take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = story.titleBn ?: story.nameBn ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                story.period?.let { p ->
                    if (p.isNotBlank()) {
                        Text(
                            text = p,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
