package com.islamichub.app.ui.screens.zakat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.components.PremiumHeroCard
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val vm = remember { ZakatViewModel(container) }
    val state by vm.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("জাকাত ক্যালকুলেটর", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Premium hero
            item {
                PremiumHeroCard(
                    backgroundImage = "salah-premium-bg.webp",
                    context = context,
                    height = 140
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("জাকাত ক্যালকুলেটর",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("২.৫% আনুপাতিক হারে সঠিক হিসাব",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // Info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("নিসাব সীমা: ৮৫ গ্রাম স্বর্ণ অথবা ৫৯৫ গ্রাম রৌপ্য। এর নিচে জাকাত ফরজ নয়।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            // Prices section
            item {
                SectionHeader("আজকের দাম (BDT)")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.input.goldPricePerGram,
                            onValueChange = { vm.updateInput { it.copy(goldPricePerGram = it.goldPricePerGram.filter { c -> c.isDigit() }) } },
                            label = { Text("স্বর্ণ দাম/গ্রাম (২৪ক্যারেট)") },
                            placeholder = { Text("অটো: ৮,৫০০") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.input.silverPricePerGram,
                            onValueChange = { vm.updateInput { it.copy(silverPricePerGram = it.silverPricePerGram.filter { c -> c.isDigit() }) } },
                            label = { Text("রৌপ্য দাম/গ্রাম") },
                            placeholder = { Text("অটো: ৯৫") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }

            // Gold & Silver section
            item { SectionHeader("স্বর্ণ ও রৌপ্য") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.input.goldGrams,
                            onValueChange = { v -> vm.updateInput { it.copy(goldGrams = v.filter { c -> c.isDigit() || c == '.' }) } },
                            label = { Text("স্বর্ণ (গ্রাম)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.input.goldTola,
                            onValueChange = { v -> vm.updateInput { it.copy(goldTola = v.filter { c -> c.isDigit() || c == '.' }) } },
                            label = { Text("স্বর্ণ (ভরি/তোলা)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.input.silverGrams,
                            onValueChange = { v -> vm.updateInput { it.copy(silverGrams = v.filter { c -> c.isDigit() || c == '.' }) } },
                            label = { Text("রৌপ্য (গ্রাম)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }
            }

            // Cash & Bank
            item { SectionHeader("নগদ ও ব্যাংক") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NumField("নগদ (BDT)", state.input.cashBDT) { v -> vm.updateInput { it.copy(cashBDT = v) } }
                        NumField("ব্যাংক ব্যালেন্স", state.input.bankBalance) { v -> vm.updateInput { it.copy(bankBalance = v) } }
                        NumField("বিদেশী মুদ্রা (BDT সমমান)", state.input.cashForeign) { v -> vm.updateInput { it.copy(cashForeign = v) } }
                    }
                }
            }

            // Business & Investments
            item { SectionHeader("ব্যবসা ও বিনিয়োগ") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NumField("ব্যবসার পণ্য (মূল্য)", state.input.businessInventoryValue) { v -> vm.updateInput { it.copy(businessInventoryValue = v) } }
                        NumField("পাওয়া দাবিদার (Receivable)", state.input.receivableDebts) { v -> vm.updateInput { it.copy(receivableDebts = v) } }
                        NumField("বিনিয়োগ (শেয়ার/প্রোভিডেন্ট ফান্ড)", state.input.investmentsValue) { v -> vm.updateInput { it.copy(investmentsValue = v) } }
                    }
                }
            }

            // Liabilities
            item { SectionHeader("দায় (Liabilities)") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        NumField("মোট দেনা", state.input.liabilities) { v -> vm.updateInput { it.copy(liabilities = v) } }
                    }
                }
            }

            // Calculate button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { vm.calculate() },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.Calculate, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("হিসাব করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    OutlinedButton(
                        onClick = { vm.reset() },
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("রিসেট")
                    }
                }
            }

            // Result
            if (state.result.isCalculated) {
                item { ResultCard(state.result) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 18.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = "  $title",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NumField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { c -> c.isDigit() || c == ',' || c == '.' }) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}

@Composable
private fun ResultCard(result: ZakatResult) {
    val nf = NumberFormat.getNumberInstance(Locale("bn", "BD"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (result.isAboveNisab)
                            listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                        else
                            listOf(Color(0xFFEF6C00), Color(0xFFFF9800))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                    Text("  ফলাফল",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }

                RowLine("স্বর্ণের মূল্য", "৳ ${nf.format(result.goldValue.toInt())}")
                RowLine("রৌপ্যের মূল্য", "৳ ${nf.format(result.silverValue.toInt())}")
                RowLine("নগদ ও ব্যাংক", "৳ ${nf.format(result.cashTotal.toInt())}")
                RowLine("ব্যবসা ও বিনিয়োগ", "৳ ${nf.format((result.businessAssets + result.investments + result.receivables).toInt())}")
                RowLine("মোট সম্পদ", "৳ ${nf.format(result.totalAssets.toInt())}")
                RowLine("মোট দায়", "- ৳ ${nf.format(result.totalLiabilities.toInt())}")
                RowLine("নিসাব (রৌপ্য ভিত্তিক)", "৳ ${nf.format(result.silverNisab.toInt())}")

                Spacer(Modifier.height(4.dp))

                // Final Zakat Due
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("প্রদেয় জাকাত",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f))
                    Text(
                        text = if (result.isAboveNisab) "৳ ${nf.format(result.zakatDue.toInt())}" else "জাকাত ফরজ নয়",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (result.isAboveNisab) "মোট সম্পদের ২.৫%" else "নিসাব সীমার নিচে",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
