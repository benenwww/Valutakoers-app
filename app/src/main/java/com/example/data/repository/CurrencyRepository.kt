package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FavoritePairEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.RateEntity
import com.example.data.model.CurrencyCatalog
import com.example.data.remote.CurrencyApiService
import com.example.data.remote.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CurrencyRepository(
    private val database: AppDatabase,
    private val apiService: CurrencyApiService,
    val networkMonitor: NetworkMonitor
) {
    private val rateDao = database.rateDao()
    private val favoritePairDao = database.favoritePairDao()
    private val holdingDao = database.holdingDao()

    val ratesMapFlow: Flow<Map<String, Double>> = rateDao.getAllRates().map { rateList ->
        if (rateList.isEmpty()) {
            CurrencyCatalog.defaultSeedRatesToEur
        } else {
            rateList.associate { it.currencyCode to it.rateToEur }
        }
    }

    val latestTimestampFlow: Flow<Long?> = rateDao.getLatestTimestamp()

    val favoritesFlow: Flow<List<FavoritePairEntity>> = favoritePairDao.getAllFavorites()

    val holdingsFlow: Flow<List<HoldingEntity>> = holdingDao.getAllHoldings()

    val isOnlineFlow: Flow<Boolean> = networkMonitor.isOnlineFlow

    suspend fun syncRates(): Result<Unit> {
        return try {
            val response = apiService.getLatestRatesForEur()
            if (response.result == "success" && response.rates.isNotEmpty()) {
                val now = System.currentTimeMillis()
                val entities = response.rates.map { (code, rate) ->
                    RateEntity(
                        currencyCode = code.uppercase(),
                        rateToEur = rate,
                        lastUpdatedMillis = now
                    )
                }
                rateDao.insertRates(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ongeldige response van valutaserver"))
            }
        } catch (e: Exception) {
            Log.e("CurrencyRepository", "Error syncing rates, staying with cached data", e)
            // Ensure we at least have seed data cached if completely empty
            ensureSeedDataExists()
            Result.failure(e)
        }
    }

    suspend fun ensureSeedDataExists() {
        ensureUpToDateRates()
    }

    suspend fun ensureUpToDateRates() {
        val currentRates = rateDao.getAllRatesSync()
        val tryRate = currentRates.firstOrNull { it.currencyCode == "TRY" }
        if (currentRates.isEmpty() || (tryRate != null && tryRate.rateToEur < 50.0)) {
            val now = System.currentTimeMillis()
            val updated = CurrencyCatalog.defaultSeedRatesToEur.map { (code, rate) ->
                RateEntity(
                    currencyCode = code,
                    rateToEur = rate,
                    lastUpdatedMillis = now
                )
            }
            rateDao.insertRates(updated)
        }
    }

    suspend fun updateCustomRate(currencyCode: String, newRateToEur: Double) {
        val now = System.currentTimeMillis()
        rateDao.insertRates(
            listOf(
                RateEntity(
                    currencyCode = currencyCode.uppercase(),
                    rateToEur = newRateToEur,
                    lastUpdatedMillis = now
                )
            )
        )
    }

    suspend fun resetRateToDefault(currencyCode: String) {
        val defaultRate = CurrencyCatalog.defaultSeedRatesToEur[currencyCode.uppercase()] ?: return
        updateCustomRate(currencyCode, defaultRate)
    }

    fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        ratesMap: Map<String, Double>
    ): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount
        val fromRate = ratesMap[fromCurrency.uppercase()] ?: CurrencyCatalog.defaultSeedRatesToEur[fromCurrency.uppercase()] ?: 1.0
        val toRate = ratesMap[toCurrency.uppercase()] ?: CurrencyCatalog.defaultSeedRatesToEur[toCurrency.uppercase()] ?: 1.0

        if (fromRate <= 0.0) return 0.0
        // (amount / fromRate) converts to EUR, then * toRate converts to target
        return (amount / fromRate) * toRate
    }

    suspend fun isFavoritePair(fromCurrency: String, toCurrency: String): Boolean {
        return favoritePairDao.getFavorite(fromCurrency.uppercase(), toCurrency.uppercase()) != null
    }

    suspend fun toggleFavorite(fromCurrency: String, toCurrency: String) {
        val existing = favoritePairDao.getFavorite(fromCurrency.uppercase(), toCurrency.uppercase())
        if (existing != null) {
            favoritePairDao.delete(existing)
        } else {
            favoritePairDao.insertFavorite(
                FavoritePairEntity(
                    fromCurrency = fromCurrency.uppercase(),
                    toCurrency = toCurrency.uppercase()
                )
            )
        }
    }

    suspend fun deleteFavorite(fromCurrency: String, toCurrency: String) {
        favoritePairDao.deleteFavorite(fromCurrency.uppercase(), toCurrency.uppercase())
    }

    suspend fun saveHolding(
        title: String,
        sourceCurrency: String,
        amount: Double,
        targetCurrency: String,
        note: String
    ): Long {
        return holdingDao.insertHolding(
            HoldingEntity(
                title = title.ifBlank { "Mijn Spaarpot" },
                sourceCurrency = sourceCurrency.uppercase(),
                amount = amount,
                targetCurrency = targetCurrency.uppercase(),
                note = note.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateHolding(
        id: Long,
        title: String,
        sourceCurrency: String,
        amount: Double,
        targetCurrency: String,
        note: String
    ) {
        holdingDao.updateHolding(
            HoldingEntity(
                id = id,
                title = title.ifBlank { "Mijn Spaarpot" },
                sourceCurrency = sourceCurrency.uppercase(),
                amount = amount,
                targetCurrency = targetCurrency.uppercase(),
                note = note.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHolding(id: Long) {
        holdingDao.deleteById(id)
    }
}
