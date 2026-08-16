package com.islamichub.app.ui.screens.stories

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.StoryItem
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.PremiumSectionHeader
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.islamichub.app.ui.components.loadAssetImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { StoriesViewModel(container) }
    val state by vm.state.collectAsState()
    var selectedStory by remember { mutableStateOf<StoryItem?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ইসলামিক গল্প") },
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
            // Premium hero
            item {
                PremiumHeroCard(
                    backgroundImage = "stories-premium-bg.webp",
                    context = context,
                    height = 180
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ইসলামিক গল্প",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "নবী, রাসূল ও খুলাফায়ে রাশেদীনের জীবনী",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Prophets section
            data.prophets?.let { prophets ->
                if (prophets.isNotEmpty()) {
                    item {
                        PremiumSectionHeader(title = "নবী ও রাসূলগণের জীবনী")
                    }
                    items(prophets, key = { it.id }) { story ->
                        StoryCard(story, context) { selectedStory = story }
                    }
                }
            }

            // Khalifas section
            data.khalifas?.let { khalifas ->
                if (khalifas.isNotEmpty()) {
                    item {
                        PremiumSectionHeader(title = "খুলাফায়ে রাশেদীন")
                    }
                    items(khalifas, key = { it.id }) { story ->
                        StoryCard(story, context) { selectedStory = story }
                    }
                }
            }

            // Miraj
            data.miraj?.let { miraj ->
                item {
                    PremiumSectionHeader(title = "মে'রাজের ঘটনা")
                }
                item { StoryCard(miraj, context) { selectedStory = miraj } }
            }

            // Sirat
            data.sirat?.let { sirat ->
                item {
                    PremiumSectionHeader(title = "সীরাত সারসংক্ষেপ")
                }
                item { StoryCard(sirat, context) { selectedStory = sirat } }
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
private fun StoryCard(story: StoryItem, context: android.content.Context, onClick: () -> Unit) {
    val bgBitmap = remember(story.id) {
        when {
            story.id.contains("prophet", ignoreCase = true) -> loadAssetImage(context, "img/prophets-premium-bg.webp")
            story.id.contains("khalif", ignoreCase = true) -> loadAssetImage(context, "img/khalifas-premium-bg.webp")
            else -> loadAssetImage(context, "img/stories-premium-bg.webp")
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            // Background image
            if (bgBitmap != null) {
                Image(
                    bitmap = bgBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (story.nameBn ?: story.titleBn ?: "?").take(1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.titleBn ?: story.nameBn ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    story.period?.let { p ->
                        if (p.isNotBlank()) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}
