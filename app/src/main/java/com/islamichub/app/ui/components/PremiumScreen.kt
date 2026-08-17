package com.islamichub.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Standard premium screen scaffold:
 *  - Premium hero with background image + title + subtitle
 *  - LazyColumn content below
 *
 * Usage:
 *   PremiumScreen(
 *       title = "হাদিস",
 *       subtitle = "২৪,৪২৪টি হাদিস",
 *       backgroundImage = "hadith-premium-bg.webp"
 *   ) {
 *       item { ... }
 *   }
 */
@Composable
fun PremiumScreen(
    title: String,
    subtitle: String,
    backgroundImage: String?,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(12.dp),
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            PremiumHeroCard(
                backgroundImage = backgroundImage,
                context = context,
                height = 160
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        content()
    }
}
