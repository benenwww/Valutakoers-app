package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ColorOffline
import com.example.ui.theme.ColorSuccess
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CurrencyViewModel

enum class NavigationTab(val title: String) {
    CONVERTER("Converter"),
    RATES("Koersen"),
    SPAARPOT("Spaarpot")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CurrencyViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rates by viewModel.rates.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTravelSettings by remember { mutableStateOf(false) }

    // Android navigation BackHandler: if not on converter tab, pressing back returns to tab 0
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    // Refresh rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        ),
        label = "spin_angle"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ValutaKoers",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Live / Offline Status Tag
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.isOnline) ColorSuccess.copy(alpha = 0.15f) else ColorOffline.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isOnline) ColorSuccess else ColorOffline)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (uiState.isOnline) "Live" else "Offline",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isOnline) ColorSuccess else ColorOffline
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Travel Mode Button
                    IconButton(
                        onClick = { showTravelSettings = true },
                        modifier = Modifier.testTag("travel_mode_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = "Reismodus instellingen",
                            tint = if (uiState.travelModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.syncRates() },
                        enabled = !uiState.isRefreshing,
                        modifier = Modifier.testTag("sync_rates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Koersen vernieuwen",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = if (uiState.isRefreshing) Modifier.rotate(spinAngle) else Modifier
                        )
                    }

                    // Theme Toggle Button (Dark / Light switch)
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.themeMode == ThemeMode.LIGHT) {
                                Icons.Default.DarkMode
                            } else {
                                Icons.Default.LightMode
                            },
                            contentDescription = "Schakel thema",
                            tint = if (uiState.themeMode == ThemeMode.LIGHT) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.CurrencyExchange else Icons.Outlined.CurrencyExchange,
                            contentDescription = "Converter"
                        )
                    },
                    label = { Text("Converter", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("nav_converter")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.TrendingUp else Icons.Outlined.TrendingUp,
                            contentDescription = "Koersen"
                        )
                    },
                    label = { Text("Koersen", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("nav_rates")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.Savings else Icons.Outlined.Savings,
                            contentDescription = "Spaarpot"
                        )
                    },
                    label = { Text("Spaarpot", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("nav_spaarpot")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Travel Mode active banner (if active and country detected)
            if (uiState.travelModeEnabled && uiState.detectedCountry != null) {
                val detected = uiState.detectedCountry!!
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTravelSettings = true },
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reismodus: ${detected.flagEmoji} ${detected.countryNameNl} (${detected.currencyCode})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Instellingen",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> ConverterScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        rates = rates,
                        favorites = favorites
                    )
                    1 -> RatesListScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        rates = rates,
                        favorites = favorites,
                        onSelectForConversion = { from, to ->
                            viewModel.selectPair(from, to)
                            selectedTab = 0 // Switch to converter tab with selected pair
                        }
                    )
                    2 -> SpaarpotScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        rates = rates,
                        holdings = holdings
                    )
                }
            }
        }
    }

    if (showTravelSettings) {
        com.example.ui.components.TravelModeSettingsDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { showTravelSettings = false }
        )
    }

    uiState.pendingTravelArrival?.let { arrival ->
        com.example.ui.components.TravelArrivalDialog(
            arrival = arrival,
            baseCurrency = uiState.fromCurrency,
            onAccept = {
                viewModel.acceptTravelCurrency(arrival.currencyCode)
            },
            onDismiss = {
                viewModel.dismissTravelPrompt()
            }
        )
    }
}
