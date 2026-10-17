package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rates")
data class RateEntity(
    @PrimaryKey
    val currencyCode: String,
    val rateToEur: Double,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "favorite_pairs")
data class FavoritePairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromCurrency: String,
    val toCurrency: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Mijn Spaarpot",
    val sourceCurrency: String = "EUR", // De valuta waarin je spaart (bijv. EUR)
    val amount: Double = 300.0, // Het gespaarde bedrag (bijv. 300 euro)
    val targetCurrency: String = "TRY", // De gekozen valuta om te zien waar je staat (bijv. TRY)
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
