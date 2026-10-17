package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.HoldingEntity
import com.example.data.model.CurrencyCatalog
import com.example.data.model.CurrencyInfo
import com.example.ui.components.CurrencyPickerDialog
import com.example.ui.viewmodel.CurrencyUiState
import com.example.ui.viewmodel.CurrencyViewModel
import com.example.ui.viewmodel.HoldingItemUi
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaarpotScreen(
    viewModel: CurrencyViewModel,
    uiState: CurrencyUiState,
    rates: Map<String, Double>,
    holdings: List<HoldingEntity>
) {
    // Quick interactive calculator state for "Hoeveel heb ik gespaard?"
    var quickSavedAmountText by remember { mutableStateOf("300") }
    var quickSourceCurrency by remember { mutableStateOf("EUR") }
    var quickTargetCurrency by remember { mutableStateOf("TRY") }

    var pickerTargetMode by remember { mutableStateOf<String?>(null) } // "QUICK_SRC", "QUICK_TGT", "DIALOG_SRC", "DIALOG_TGT"
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAddDialog by remember { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingEntity?>(null) }
    var holdingToDelete by remember { mutableStateOf<HoldingEntity?>(null) }

    val decimalFormatter = remember {
        DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("nl", "NL")))
    }
    val precisionFormatter = remember {
        DecimalFormat("#,##0.0000", DecimalFormatSymbols(Locale("nl", "NL")))
    }

    val quickAmountNum = quickSavedAmountText.toDoubleOrNull() ?: 0.0
    val quickConvertedResult = viewModel.calculateConversion(
        amount = quickAmountNum,
        from = quickSourceCurrency,
        to = quickTargetCurrency,
        ratesMap = rates
    )
    val quickSingleRate = viewModel.getSingleUnitRate(
        from = quickSourceCurrency,
        to = quickTargetCurrency,
        ratesMap = rates
    )

    val quickSourceInfo = CurrencyCatalog.getCurrencyInfo(quickSourceCurrency)
    val quickTargetInfo = CurrencyCatalog.getCurrencyInfo(quickTargetCurrency)

    // Map holdings to detailed UI items
    val holdingItems by remember(holdings, rates) {
        derivedStateOf {
            holdings.map { h ->
                val converted = viewModel.calculateConversion(
                    amount = h.amount,
                    from = h.sourceCurrency,
                    to = h.targetCurrency,
                    ratesMap = rates
                )
                val rate = viewModel.getSingleUnitRate(
                    from = h.sourceCurrency,
                    to = h.targetCurrency,
                    ratesMap = rates
                )
                HoldingItemUi(
                    entity = h,
                    sourceInfo = CurrencyCatalog.getCurrencyInfo(h.sourceCurrency),
                    targetInfo = CurrencyCatalog.getCurrencyInfo(h.targetCurrency),
                    convertedAmountInTarget = converted,
                    singleRateText = "1 ${h.sourceCurrency} = ${precisionFormatter.format(rate)} ${h.targetCurrency}"
                )
            }
        }
    }

    // Popular world target currencies to show quick breakdown of the saved 300 EUR
    val comparisonCurrencies = listOf("TRY", "USD", "GBP", "CHF", "JPY", "SAR")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Hero Artwork & Title Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spaarpot_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.spaarpot_vault_1791842273666),
                                contentDescription = "Spaarpot illustratie",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            )
                        }

                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Spaarpot Koerswaarde",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Voer in hoeveel je hebt gespaard en zie meteen waar je staat in een andere valuta!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // PRIMARY INTERACTIVE CARD: "Hoeveel heb je gespaard?"
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spaarpot_interactive_calculator"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Jouw Gespaarde Bedrag",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Amount Input Row + Currency Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Source Currency Selector Button (e.g. EUR)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { pickerTargetMode = "QUICK_SRC" }
                                    .testTag("quick_source_currency_button"),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = quickSourceInfo.flagEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = quickSourceInfo.code,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Large Numeric Input Field
                            OutlinedTextField(
                                value = quickSavedAmountText,
                                onValueChange = { input ->
                                    val sanitized = input.replace(',', '.')
                                    if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                        quickSavedAmountText = sanitized
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_amount_input"),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                placeholder = { Text("bijv. 300") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                        }

                        // Quick Increment Pills (+25, +50, +100, +250)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(25.0, 50.0, 100.0, 250.0).forEach { delta ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val current = quickSavedAmountText.toDoubleOrNull() ?: 0.0
                                            val updated = (current + delta).coerceAtLeast(0.0)
                                            quickSavedAmountText = if (updated % 1.0 == 0.0) updated.toLong().toString() else updated.toString()
                                        },
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+${delta.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Target Currency Selection: "Kies doelvaluta om te meten"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gekozen valuta om te meten:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { pickerTargetMode = "QUICK_TGT" }
                                    .testTag("quick_target_currency_button"),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = quickTargetInfo.flagEmoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${quickTargetInfo.code} (${quickTargetInfo.symbol})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // LARGE RESULT DISPLAY: "Ah, dit is waar ik sta met sparen in een andere valuta!"
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.background,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Hier sta je nu met sparen in ${quickTargetInfo.name}:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = quickTargetInfo.symbol,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(
                                        text = "${decimalFormatter.format(quickConvertedResult)} ${quickTargetInfo.code}",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 28.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("quick_converted_result_text")
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Jouw ${quickSourceInfo.symbol} ${decimalFormatter.format(quickAmountNum)} ${quickSourceInfo.code} is op dit moment ${quickTargetInfo.symbol} ${decimalFormatter.format(quickConvertedResult)} waard (1 ${quickSourceInfo.code} = ${precisionFormatter.format(quickSingleRate)} ${quickTargetInfo.code}).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save Button to permanently persist this savings goal for next sessions!
                        Button(
                            onClick = {
                                if (quickAmountNum > 0.0) {
                                    viewModel.saveHolding(
                                        title = "Spaarpot ${quickTargetInfo.name}",
                                        sourceCurrency = quickSourceCurrency,
                                        amount = quickAmountNum,
                                        targetCurrency = quickTargetCurrency,
                                        note = "Aangemaakt vanuit snelcalculator"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_quick_as_holding_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bewaar dit spaarbedrag in mijn spaarpot")
                        }
                    }
                }
            }

            // MULTI-CURRENCY COMPARISON ROW: "Hoeveel is jouw spaargeld wereldwijd waard?"
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Jouw ${quickSourceInfo.symbol} ${decimalFormatter.format(quickAmountNum)} ${quickSourceInfo.code} in andere valuta's:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(comparisonCurrencies.filter { it != quickSourceCurrency }) { targetCode ->
                            val targetItemInfo = CurrencyCatalog.getCurrencyInfo(targetCode)
                            val converted = viewModel.calculateConversion(
                                amount = quickAmountNum,
                                from = quickSourceCurrency,
                                to = targetCode,
                                ratesMap = rates
                            )

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        quickTargetCurrency = targetCode
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (quickTargetCurrency == targetCode) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = targetItemInfo.flagEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = targetItemInfo.code,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${targetItemInfo.symbol} ${decimalFormatter.format(converted)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (quickTargetCurrency == targetCode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SAVED HOLDINGS SECTION (Persisted across sessions!)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Opgeslagen Spaarpotten (${holdingItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Bewaard voor volgende sessies",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (holdingItems.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "Nog geen opgeslagen spaarpotten. Gebruik de calculator hierboven of de knop onderaan om een spaarpot op te slaan!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(holdingItems, key = { it.entity.id }) { item ->
                    SavedHoldingCard(
                        item = item,
                        decimalFormatter = decimalFormatter,
                        onEdit = { editingHolding = item.entity },
                        onDelete = { holdingToDelete = item.entity }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to add a new custom spaarpot
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("spaarpot_add_fab"),
            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
            text = { Text("Nieuwe Spaarpot") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    // Modal Sheet for Currency Picking
    if (pickerTargetMode != null) {
        val currentCode = when (pickerTargetMode) {
            "QUICK_SRC" -> quickSourceCurrency
            "QUICK_TGT" -> quickTargetCurrency
            else -> "EUR"
        }

        CurrencyPickerDialog(
            selectedCode = currentCode,
            onSelect = { selectedCode ->
                when (pickerTargetMode) {
                    "QUICK_SRC" -> quickSourceCurrency = selectedCode
                    "QUICK_TGT" -> quickTargetCurrency = selectedCode
                }
            },
            onDismiss = { pickerTargetMode = null },
            sheetState = pickerSheetState
        )
    }

    // Dialog for adding a new holding
    if (showAddDialog) {
        HoldingDialog(
            dialogTitle = "Nieuwe spaarpot toevoegen",
            initialTitle = "Mijn Spaardoel",
            initialSourceCurrency = quickSourceCurrency,
            initialAmount = quickSavedAmountText,
            initialTargetCurrency = quickTargetCurrency,
            initialNote = "",
            onConfirm = { title, source, amount, target, note ->
                viewModel.saveHolding(title, source, amount, target, note)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Dialog for editing an existing holding (Amount is editable at any time!)
    editingHolding?.let { holding ->
        HoldingDialog(
            dialogTitle = "Spaarpot aanpassen",
            initialTitle = holding.title,
            initialSourceCurrency = holding.sourceCurrency,
            initialAmount = if (holding.amount % 1.0 == 0.0) holding.amount.toLong().toString() else holding.amount.toString(),
            initialTargetCurrency = holding.targetCurrency,
            initialNote = holding.note,
            onConfirm = { title, source, amount, target, note ->
                viewModel.updateHolding(holding.id, title, source, amount, target, note)
                editingHolding = null
            },
            onDismiss = { editingHolding = null }
        )
    }

    // Delete Confirmation Dialog
    holdingToDelete?.let { holding ->
        AlertDialog(
            onDismissRequest = { holdingToDelete = null },
            title = { Text("Spaarpot verwijderen?") },
            text = {
                Text("Weet je zeker dat je '${holding.title}' (${holding.amount} ${holding.sourceCurrency}) wilt verwijderen?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHolding(holding.id)
                        holdingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Verwijderen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { holdingToDelete = null }) {
                    Text("Annuleren")
                }
            }
        )
    }
}

@Composable
private fun SavedHoldingCard(
    item: HoldingItemUi,
    decimalFormatter: DecimalFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holding_item_${item.entity.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Visual Pair Avatars (From -> To)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.sourceInfo.flagEmoji, fontSize = 18.sp)
                    }
                    Text(text = " → ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.targetInfo.flagEmoji, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.entity.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.entity.note.isNotEmpty()) {
                        Text(
                            text = item.entity.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Edit Button (Aanpassen)
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Pas bedrag aan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete Button
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Verwijderen",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Values Row: Gespaard vs Waar je nu staat in doelvaluta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Gespaard bedrag:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.sourceInfo.symbol} ${decimalFormatter.format(item.entity.amount)} ${item.sourceInfo.code}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Waar je nu staat in ${item.targetInfo.code}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${item.targetInfo.symbol} ${decimalFormatter.format(item.convertedAmountInTarget)} ${item.targetInfo.code}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live rate footer
            Text(
                text = item.singleRateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingDialog(
    dialogTitle: String,
    initialTitle: String,
    initialSourceCurrency: String,
    initialAmount: String,
    initialTargetCurrency: String,
    initialNote: String,
    onConfirm: (title: String, source: String, amount: Double, target: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var sourceCurrency by remember { mutableStateOf(initialSourceCurrency) }
    var amountText by remember { mutableStateOf(initialAmount) }
    var targetCurrency by remember { mutableStateOf(initialTargetCurrency) }
    var note by remember { mutableStateOf(initialNote) }

    var pickerMode by remember { mutableStateOf<String?>(null) } // "SRC" or "TGT"
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val srcInfo = CurrencyCatalog.getCurrencyInfo(sourceCurrency)
    val tgtInfo = CurrencyCatalog.getCurrencyInfo(targetCurrency)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = dialogTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title Field
                Text(text = "Naam van de spaarpot", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Source Currency & Amount
                Text(text = "Gespaard bedrag en valuta", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { pickerMode = "SRC" },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${srcInfo.flagEmoji} ${srcInfo.code}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            val sanitized = it.replace(',', '.')
                            if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                amountText = sanitized
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("bijv. 300") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Currency
                Text(text = "Doelvaluta om in te berekenen", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { pickerMode = "TGT" },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = tgtInfo.flagEmoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${tgtInfo.code} - ${tgtInfo.name} (${tgtInfo.symbol})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note Field
                Text(text = "Notitie (optioneel)", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("bijv. Vakantie Turkije") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull() ?: 0.0
                    if (parsed > 0.0) {
                        onConfirm(title, sourceCurrency, parsed, targetCurrency, note)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuleren")
            }
        }
    )

    if (pickerMode != null) {
        val currentCode = if (pickerMode == "SRC") sourceCurrency else targetCurrency
        CurrencyPickerDialog(
            selectedCode = currentCode,
            onSelect = { selected ->
                if (pickerMode == "SRC") sourceCurrency = selected else targetCurrency = selected
            },
            onDismiss = { pickerMode = null },
            sheetState = sheetState
        )
    }
}
