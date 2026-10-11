package com.islamichub.app.ui.screens.tajweed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumSectionHeader
import com.islamichub.app.ui.theme.staggerEntrance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TajweedCheckerScreen(
    container: com.islamichub.app.data.AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { TajweedViewModel(container) }
    val state by vm.state.collectAsState()

    // v5.16.0 — two modes: VOICE (default, port of the web TajbeedChecker
    // voice AI) and the classic TYPING flow. Nothing was removed.
    var mode by remember { mutableStateOf("voice") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("তাজবীদ চেকার" + " / Tajweed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            // ─── Mode switcher: ভয়েস / টাইপিং ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = mode == "voice",
                    onClick = { mode = "voice" },
                    label = { Text("🎙 ভয়েস দিয়ে চেক") },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = mode == "typing",
                    onClick = { mode = "typing" },
                    label = { Text("⌨ লিখে চেক") },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            if (mode == "voice") {
                // Voice AI fills the rest of the screen (own dark-green theme)
                TajweedVoiceScreen(
                    container = container,
                    onBack = onBack,
                    embedded = true
                )
            } else {
            Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Description card
            Card(
                modifier = Modifier.fillMaxWidth().staggerEntrance(0),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "তাজবীদ বিশ্লেষণ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "আরবি টেক্সট দিন, AI তাজবীদ নিয়ম যাচাই করবে (AI API key প্রয়োজন)।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            // Input
            OutlinedTextField(
                value = state.inputText,
                onValueChange = vm::onInputChange,
                label = { Text("আরবি টেক্সট লিখুন অথবা paste করুন") },
                modifier = Modifier.fillMaxWidth().staggerEntrance(1),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp)
            )

            Button(
                onClick = vm::analyze,
                modifier = Modifier.fillMaxWidth().staggerEntrance(2),
                enabled = !state.isAnalyzing && state.inputText.isNotBlank() && state.apiKeyConfigured
            ) {
                Text(if (state.isAnalyzing) "বিশ্লেষণ চলছে…" else "তাজবীদ যাচাই করুন")
            }

            if (!state.apiKeyConfigured) {
                Text(
                    text = "AI API key কনফিগার করা নেই। Settings এ গিয়ে কনফিগার করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isAnalyzing) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth().staggerEntrance(3),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            state.result?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth().staggerEntrance(4),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PremiumSectionHeader(title = "বিশ্লেষণ ফলাফল")
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            } // end typing-mode column
            } // end else (typing)
        } // end outer column
    }
}
