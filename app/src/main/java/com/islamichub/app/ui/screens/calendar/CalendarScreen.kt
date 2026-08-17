package com.islamichub.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.components.loadAssetImage
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(container: AppContainer) {
    val vm = remember { CalendarViewModel(container) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("হিজরি ক্যালেন্ডার") },
                navigationIcon = {
                    IconButton(onClick = { /* no back — it's a tab */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Premium hero
            PremiumHeroCard(
                backgroundImage = "quran-premium-bg.webp",
                context = context,
                height = 120
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("ইসলামিক ক্যালেন্ডার",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${state.hijriMonthName} ${state.hijriYear}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f))
                }
            }

            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.prevMonth() }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
                }
                Text(state.monthTitle, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = { vm.nextMonth() }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
                }
            }

            // Weekday header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র").forEach { day ->
                    Text(day, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }

            // Calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.days) { day ->
                    if (day.gregorianDay == 0) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        DayCell(day)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: HijriDayItem) {
    val isEvent = day.islamicEvent != null
    val bgBrush = when {
        day.isToday -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        )
        isEvent -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        )
        else -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
    }
    val textColor = if (day.isToday) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgBrush)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.gregorianDay.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            Text(
                text = day.hijriDay,
                style = MaterialTheme.typography.labelSmall,
                color = if (day.isToday) Color.White.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (isEvent) {
                Text(
                    text = day.islamicEvent!!,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
