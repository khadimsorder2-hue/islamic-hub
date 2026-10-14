package com.islamichub.app.ui.screens.zikr

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamichub.app.data.AppContainer
import com.islamichub.app.data.local.AdhkarAssetSource
import com.islamichub.app.data.local.AdhkarCategory
import com.islamichub.app.data.local.AdhkarData
import com.islamichub.app.data.local.AdhkarItem
import com.islamichub.app.data.local.AdhkarTopic
import com.islamichub.app.ui.theme.arabicSp
import com.islamichub.app.ui.theme.banglaSp
import com.islamichub.app.ui.theme.premiumTap
import com.islamichub.app.ui.theme.staggerEntrance
import com.islamichub.app.ui.theme.toBanglaDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * v5.18.0 — Zikr & Dua hub, fully adopted from the Muslimeen app:
 * 37 categories → 208 topics → 265 adhkar with Bangla meanings,
 * per-adhkar daily counter, copy + share.
 */
data class ZikrUiState(
    val data: AdhkarData? = null,
    val isLoading: Boolean = true,
    /** Counts are keyed by adhkarId — persisted per day in the zikr counter store. */
    val counts: Map<Int, Int> = emptyMap()
)

class ZikrViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ZikrUiState())
    val state: StateFlow<ZikrUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val data = container.adhkarAssetSource.load()
                _state.value = ZikrUiState(data = data, isLoading = false, counts = container.zikrCounterStore.todays())
            } catch (_: Exception) {
                _state.value = ZikrUiState(isLoading = false)
            }
        }
    }

    fun increment(adhkarId: Int) {
        viewModelScope.launch {
            container.zikrCounterStore.increment(adhkarId)
            container.trackerRepository.addZikr(1)
            _state.value = _state.value.copy(counts = container.zikrCounterStore.todays())
        }
    }

    fun reset(adhkarId: Int) {
        viewModelScope.launch {
            container.zikrCounterStore.reset(adhkarId)
            _state.value = _state.value.copy(counts = container.zikrCounterStore.todays())
        }
    }
}

// ─────────────────────────── Hub: category grid ───────────────────────────

private val zikrAccent = Color(0xFF0E7C5A)

@Composable
fun ZikrHubScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onCategoryClick: (AdhkarCategory) -> Unit
) {
    val vm = remember { ZikrViewModel(container) }
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // v5.18.0 — compact custom header (no fat CenterAlignedTopAppBar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text("যিকর ও দোয়া", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("মুসলিমেন স্টাইল • ২৬৫ যিকর", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Column
        }

        val data = state.data ?: return@Column
        val categories = data.categories
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(categories, key = { _, c -> c.id }) { idx, cat ->
                ZikrCategoryCard(cat, idx) { onCategoryClick(cat) }
            }
        }
    }
}

@Composable
private fun ZikrCategoryCard(cat: AdhkarCategory, idx: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggerEntrance(idx)
            .clip(RoundedCornerShape(20.dp))
            .premiumTap(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(zikrAccent.copy(alpha = 0.16f), zikrAccent.copy(alpha = 0.05f))
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(zikrAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (cat.nameBn ?: cat.name ?: "•").take(1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    cat.nameBn?.takeIf { it.isNotBlank() } ?: cat.name ?: "",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = banglaSp(MaterialTheme.typography.titleSmall.fontSize)),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!cat.name.isNullOrBlank() && cat.nameBn?.isNotBlank() == true) {
                    Text(
                        cat.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─────────────────────────── Topics list ───────────────────────────

@Composable
fun ZikrTopicsScreen(
    container: AppContainer,
    categoryId: Int,
    onBack: () -> Unit,
    onTopicClick: (AdhkarTopic) -> Unit
) {
    val vm = remember { ZikrViewModel(container) }
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                state.data?.categories?.firstOrNull { it.id == categoryId }?.let {
                    it.nameBn?.takeIf { b -> b.isNotBlank() } ?: it.name
                } ?: "বিষয়সমূহ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val data = state.data
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Column
        }
        if (data == null) return@Column

        val topics = data.topicsForCategory(categoryId)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(topics, key = { _, t -> t.id }) { idx, topic ->
                val count = data.topicAdhkar.count { it.topicId == topic.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggerEntrance(idx)
                        .clip(RoundedCornerShape(16.dp))
                        .premiumTap(onClick = { onTopicClick(topic) }),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(zikrAccent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${idx + 1}".toBanglaDigits(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = zikrAccent
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                topic.nameBn?.takeIf { it.isNotBlank() } ?: topic.name ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = banglaSp(MaterialTheme.typography.bodyLarge.fontSize)),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!topic.name.isNullOrBlank() && topic.nameBn?.isNotBlank() == true) {
                                Text(
                                    topic.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            "${count}".toBanglaDigits() + " টি",
                            style = MaterialTheme.typography.labelSmall,
                            color = zikrAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────── Adhkar detail + counter ───────────────────────────

@Composable
fun ZikrDetailScreen(
    container: AppContainer,
    topicId: Int,
    onBack: () -> Unit
) {
    val vm = remember { ZikrViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                state.data?.topics?.firstOrNull { it.id == topicId }?.let {
                    it.nameBn?.takeIf { b -> b.isNotBlank() } ?: it.name
                } ?: "যিকর",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val data = state.data
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Column
        }
        if (data == null) return@Column

        val items = data.adhkarForTopic(topicId)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(items, key = { _, a -> a.id }) { idx, item ->
                ZikrAdhkarCard(
                    item = item,
                    index = idx,
                    count = state.counts[item.id] ?: 0,
                    onIncrement = { vm.increment(item.id) },
                    onReset = { vm.reset(item.id) },
                    onCopy = {
                        val text = buildString {
                            append(item.arabic ?: "")
                            append("\n\n")
                            append(item.displayMeaning())
                            item.reference?.takeIf { it.isNotBlank() }?.let {
                                append("\n\n— ")
                                append(it)
                            }
                        }
                        clipboard.setText(AnnotatedString(text))
                        android.widget.Toast.makeText(context, "কপি হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val text = buildString {
                            append(item.arabic ?: "")
                            append("\n\n")
                            append(item.displayMeaning())
                            item.reference?.takeIf { it.isNotBlank() }?.let {
                                append("\n\n— ")
                                append(it)
                            }
                            append("\n\n— Islamic Hub থেকে শেয়ার করা")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "শেয়ার করুন"))
                    }
                )
            }
        }
    }
}

@Composable
private fun ZikrAdhkarCard(
    item: AdhkarItem,
    index: Int,
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggerEntrance(index),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item.arabic?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = arabicSp(MaterialTheme.typography.titleLarge.fontSize)),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item.transliteration?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item.displayMeaning().takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = banglaSp(MaterialTheme.typography.bodyMedium.fontSize)),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            item.reference?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "— $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = zikrAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Counter + actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Counter pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(zikrAccent.copy(alpha = 0.12f))
                        .clickable(onClick = onIncrement)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "গণনা",
                        tint = zikrAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        count.toString().toBanglaDigits(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = zikrAccent
                    )
                }
                if (count > 0) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onReset, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "রিসেট",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onCopy, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "কপি",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(38.dp)) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "শেয়ার",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
