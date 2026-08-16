package com.islamichub.app.ui.screens.more

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.R
import com.islamichub.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        MoreItem(R.string.more_prayer_times, Icons.Filled.MenuBook, Screen.Prayer.route),
        MoreItem(R.string.more_qibla, Icons.Filled.CompassCalibration, Screen.Qibla.route),
        MoreItem(R.string.more_tasbih, Icons.Filled.Spa, Screen.Tasbih.route),
        MoreItem(R.string.more_99_names, Icons.Filled.Favorite, Screen.Names.route),
        MoreItem(R.string.more_duas, Icons.Filled.Book, Screen.Duas.route),
        MoreItem(R.string.more_calendar, Icons.Filled.DateRange, Screen.Calendar.route),
        MoreItem(R.string.more_hadith, Icons.Filled.Book, Screen.Hadith.route),
        MoreItem(R.string.more_qada, Icons.Filled.History, Screen.Qada.route),
        MoreItem(R.string.more_tracker, Icons.Filled.MenuBook, Screen.Tracker.route),
        MoreItem(R.string.more_bookmarks, Icons.Filled.Bookmark, Screen.Bookmarks.route),
        MoreItem(R.string.more_khatam, Icons.Filled.MenuBook, Screen.Khatam.route),
        MoreItem(R.string.more_profile, Icons.Filled.Person, Screen.Profile.route),
        MoreItem(R.string.more_settings, Icons.Filled.Settings, Screen.Settings.route)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.more_title)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.labelRes }) { item ->
                MoreCard(item) { onNavigate(item.route) }
            }
        }
    }
}

@Composable
private fun MoreCard(item: MoreItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = stringResource(item.labelRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private data class MoreItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String
)
