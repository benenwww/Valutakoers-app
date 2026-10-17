package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FavoritePairEntity
import com.example.data.model.CurrencyCatalog
import com.example.data.model.CurrencyInfo
import com.example.ui.components.CurrencyPickerDialog
import com.example.ui.viewmodel.CurrencyUiState
import com.example.ui.viewmodel.CurrencyViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesListScreen(
    viewModel: CurrencyViewModel,
    uiState: CurrencyUiState,
    rates: Map<String, Double>,
    favorites: List<FavoritePairEntity>,
    onSelectForConversion: (from: String, to: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL" or "FAVORITES"
    var baseCurrency by remember { mutableStateOf("EUR") }
    var showBasePicker by remember { mutableStateOf(false) }
    val baseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val baseInfo = CurrencyCatalog.getCurrencyInfo(baseCurrency)

    val precisionFormatter = remember {
        DecimalFormat("#,##0.0000", DecimalFormatSymbols(Locale("nl", "NL")))
    }

    val filteredList by remember(searchQuery, selectedFilter, favorites, rates, baseCurrency) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase()
            val baseList = CurrencyCatalog.allCurrencies.filter { it.code != baseCurrency }
            
            val matchingList = if (q.isEmpty()) {
                baseList
            } else {
                baseList.filter {
                    it.code.lowercase().contains(q) ||
                            it.name.lowercase().contains(q) ||
                            it.symbol.lowercase().contains(q)
                }
            }

            if (selectedFilter == "FAVORITES") {
                matchingList.filter { currency ->
                    favorites.any { fav ->
                        (fav.fromCurrency == baseCurrency && fav.toCurrency == currency.code) ||
                                (fav.fromCurrency == currency.code && fav.toCurrency == baseCurrency)
                    }
                }
            } else {
                matchingList
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Intuitive Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rates_search_bar"),
            placeholder = { Text("Zoek valuta (bijv. TRY, dollar, yen)...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Zoeken",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Wissen")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Row with Base Currency Switcher and Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Base Currency Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showBasePicker = true }
                    .testTag("change_base_currency_button"),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Basis: ${baseInfo.flagEmoji} ${baseInfo.code}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Filter Chips (Alle vs Favorieten)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("Alle (${CurrencyCatalog.allCurrencies.size - 1})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = selectedFilter == "FAVORITES",
                    onClick = { selectedFilter = "FAVORITES" },
                    label = { Text("Favorieten (${favorites.size})") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rates List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedFilter == "FAVORITES") "Geen favoriete valuta gevonden voor ${baseInfo.code}." else "Geen valuta gevonden voor '$searchQuery'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("rates_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.code }) { currency ->
                    val rate = viewModel.getSingleUnitRate(
                        from = baseCurrency,
                        to = currency.code,
                        ratesMap = rates
                    )
                    val inverseRate = viewModel.getSingleUnitRate(
                        from = currency.code,
                        to = baseCurrency,
                        ratesMap = rates
                    )

                    val isFav = favorites.any {
                        (it.fromCurrency.equals(baseCurrency, true) && it.toCurrency.equals(currency.code, true)) ||
                                (it.fromCurrency.equals(currency.code, true) && it.toCurrency.equals(baseCurrency, true))
                    }

                    CurrencyRateCard(
                        currency = currency,
                        baseCode = baseCurrency,
                        rate = rate,
                        inverseRate = inverseRate,
                        isFavorite = isFav,
                        formatter = precisionFormatter,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(baseCurrency, currency.code)
                        },
                        onClick = {
                            onSelectForConversion(baseCurrency, currency.code)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    if (showBasePicker) {
        CurrencyPickerDialog(
            selectedCode = baseCurrency,
            onSelect = { baseCurrency = it },
            onDismiss = { showBasePicker = false },
            sheetState = baseSheetState
        )
    }
}

@Composable
private fun CurrencyRateCard(
    currency: CurrencyInfo,
    baseCode: String,
    rate: Double,
    inverseRate: Double,
    isFavorite: Boolean,
    formatter: DecimalFormat,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("rate_card_${currency.code}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = currency.flagEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Code & Name
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency.code,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currency.symbol,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Rates Information
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatter.format(rate)} ${currency.code}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "1 ${currency.code} = ${formatter.format(inverseRate)} $baseCode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favoriet",
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
