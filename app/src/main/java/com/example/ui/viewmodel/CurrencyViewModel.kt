package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FavoritePairEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.model.CurrencyCatalog
import com.example.data.model.CurrencyInfo
import com.example.data.repository.CurrencyRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class HoldingItemUi(
    val entity: HoldingEntity,
    val sourceInfo: CurrencyInfo,
    val targetInfo: CurrencyInfo,
    val convertedAmountInTarget: Double,
    val singleRateText: String
)

data class CurrencyUiState(
    val amountInput: String = "100",
    val fromCurrency: String = "TRY",
    val toCurrency: String = "EUR",
    val searchQuery: String = "",
    val isOnline: Boolean = true,
    val isRefreshing: Boolean = false,
    val lastSyncTimestamp: Long? = null,
    val targetHoldingCurrency: String = "EUR",
    val themeMode: ThemeMode = ThemeMode.DARK,
    val errorMessage: String? = null,
    val travelModeEnabled: Boolean = false,
    val detectedCountry: com.example.data.location.CountryCurrencyInfo? = null,
    val pendingTravelArrival: com.example.data.location.CountryCurrencyInfo? = null,
    val isDetectingLocation: Boolean = false,
    val hasLocationPermission: Boolean = false
)

class CurrencyViewModel(
    private val repository: CurrencyRepository,
    private val locationDetector: com.example.data.location.LocationDetector? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    val rates: StateFlow<Map<String, Double>> = repository.ratesMapFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CurrencyCatalog.defaultSeedRatesToEur
        )

    val favorites: StateFlow<List<FavoritePairEntity>> = repository.favoritesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val holdings: StateFlow<List<HoldingEntity>> = repository.holdingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.ensureUpToDateRates()
        }

        // Collect network state
        viewModelScope.launch {
            repository.isOnlineFlow.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online) {
                    syncRates()
                }
            }
        }

        // Collect latest update timestamp
        viewModelScope.launch {
            repository.latestTimestampFlow.collect { timestamp ->
                _uiState.update { it.copy(lastSyncTimestamp = timestamp) }
            }
        }

        // Initial sync attempt
        syncRates()
    }

    fun setCustomDirectRate(from: String, to: String, directRate: Double) {
        viewModelScope.launch {
            if (from.equals("EUR", ignoreCase = true)) {
                repository.updateCustomRate(to, directRate)
            } else if (to.equals("EUR", ignoreCase = true)) {
                if (directRate > 0.0) {
                    repository.updateCustomRate(from, 1.0 / directRate)
                }
            } else {
                // If neither is EUR, adjust target relative to from's current EUR rate
                val currentRates = rates.value
                val fromRate = currentRates[from.uppercase()] ?: 1.0
                repository.updateCustomRate(to, fromRate * directRate)
            }
        }
    }

    fun resetRateToDefault(code: String) {
        viewModelScope.launch {
            repository.resetRateToDefault(code)
        }
    }

    fun syncRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = repository.syncRates()
            _uiState.update { current ->
                current.copy(
                    isRefreshing = false,
                    errorMessage = if (result.isFailure && !current.isOnline) {
                        "Offline: actuele koersen zijn niet bereikbaar. Lokale gegevens worden gebruikt."
                    } else if (result.isFailure) {
                        result.exceptionOrNull()?.localizedMessage ?: "Fout bij ophalen koersen"
                    } else null
                )
            }
        }
    }

    fun onAmountChange(input: String) {
        val sanitized = input.replace(',', '.')
        // Allow empty or valid number format
        if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*(\\.\\d{0,4})?$"))) {
            _uiState.update { it.copy(amountInput = sanitized) }
        }
    }

    fun quickAddAmount(delta: Double) {
        val current = _uiState.value.amountInput.toDoubleOrNull() ?: 0.0
        val next = (current + delta).coerceAtLeast(0.0)
        val formatted = if (next % 1.0 == 0.0) next.toLong().toString() else String.format(Locale.US, "%.2f", next)
        _uiState.update { it.copy(amountInput = formatted) }
    }

    fun clearAmount() {
        _uiState.update { it.copy(amountInput = "0") }
    }

    fun setAmount(amount: Double) {
        val formatted = if (amount % 1.0 == 0.0) amount.toLong().toString() else String.format(Locale.US, "%.2f", amount)
        _uiState.update { it.copy(amountInput = formatted) }
    }

    fun swapCurrencies() {
        _uiState.update { current ->
            current.copy(
                fromCurrency = current.toCurrency,
                toCurrency = current.fromCurrency
            )
        }
    }

    fun setFromCurrency(code: String) {
        _uiState.update { current ->
            if (code == current.toCurrency) {
                current.copy(fromCurrency = code, toCurrency = current.fromCurrency)
            } else {
                current.copy(fromCurrency = code)
            }
        }
    }

    fun setToCurrency(code: String) {
        _uiState.update { current ->
            if (code == current.fromCurrency) {
                current.copy(toCurrency = code, fromCurrency = current.toCurrency)
            } else {
                current.copy(toCurrency = code)
            }
        }
    }

    fun selectPair(from: String, to: String) {
        _uiState.update { it.copy(fromCurrency = from, toCurrency = to) }
    }

    fun toggleFavorite(from: String, to: String) {
        viewModelScope.launch {
            repository.toggleFavorite(from, to)
        }
    }

    fun deleteFavorite(from: String, to: String) {
        viewModelScope.launch {
            repository.deleteFavorite(from, to)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleTheme() {
        _uiState.update { current ->
            val nextMode = when (current.themeMode) {
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.SYSTEM -> ThemeMode.DARK
            }
            current.copy(themeMode = nextMode)
        }
    }

    fun setTargetHoldingCurrency(code: String) {
        _uiState.update { it.copy(targetHoldingCurrency = code) }
    }

    fun saveHolding(
        title: String,
        sourceCurrency: String,
        amount: Double,
        targetCurrency: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.saveHolding(title, sourceCurrency, amount, targetCurrency, note)
        }
    }

    fun updateHolding(
        id: Long,
        title: String,
        sourceCurrency: String,
        amount: Double,
        targetCurrency: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.updateHolding(id, title, sourceCurrency, amount, targetCurrency, note)
        }
    }

    fun deleteHolding(id: Long) {
        viewModelScope.launch {
            repository.deleteHolding(id)
        }
    }

    fun calculateConversion(
        amount: Double,
        from: String,
        to: String,
        ratesMap: Map<String, Double>
    ): Double {
        return repository.convertAmount(amount, from, to, ratesMap)
    }

    fun getSingleUnitRate(from: String, to: String, ratesMap: Map<String, Double>): Double {
        return repository.convertAmount(1.0, from, to, ratesMap)
    }

    fun setTravelModeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(travelModeEnabled = enabled) }
        if (enabled) {
            detectLocation()
        } else {
            _uiState.update { it.copy(pendingTravelArrival = null) }
        }
    }

    fun updateLocationPermission(hasPermission: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }
        if (hasPermission && _uiState.value.travelModeEnabled) {
            detectLocation()
        }
    }

    fun detectLocation() {
        val detector = locationDetector ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true) }
            val detected = detector.detectCurrentCountry()
            _uiState.update { current ->
                val shouldPrompt = detected != null &&
                        !detected.currencyCode.equals(current.toCurrency, ignoreCase = true) &&
                        !detected.currencyCode.equals(current.fromCurrency, ignoreCase = true)
                current.copy(
                    isDetectingLocation = false,
                    detectedCountry = detected,
                    pendingTravelArrival = if (shouldPrompt) detected else current.pendingTravelArrival
                )
            }
        }
    }

    fun simulateTravelArrival(countryCode: String) {
        val simulated = com.example.data.location.CountryCurrencyCatalog.getByCountryCode(countryCode)
        if (simulated != null) {
            _uiState.update {
                it.copy(
                    travelModeEnabled = true,
                    detectedCountry = simulated,
                    pendingTravelArrival = simulated
                )
            }
        }
    }

    fun acceptTravelCurrency(currencyCode: String) {
        _uiState.update { current ->
            current.copy(
                toCurrency = currencyCode,
                pendingTravelArrival = null
            )
        }
        syncRates()
    }

    fun dismissTravelPrompt() {
        _uiState.update { it.copy(pendingTravelArrival = null) }
    }

    class Factory(
        private val repository: CurrencyRepository,
        private val locationDetector: com.example.data.location.LocationDetector? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CurrencyViewModel::class.java)) {
                return CurrencyViewModel(repository, locationDetector) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
