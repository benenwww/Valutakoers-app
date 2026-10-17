package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FavoritePairDao
import com.example.data.local.dao.HoldingDao
import com.example.data.local.dao.RateDao
import com.example.data.local.entity.FavoritePairEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.RateEntity
import com.example.data.model.CurrencyCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RateEntity::class,
        FavoritePairEntity::class,
        HoldingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rateDao(): RateDao
    abstract fun favoritePairDao(): FavoritePairDao
    abstract fun holdingDao(): HoldingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "valuta_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            
            // Seed initial exchange rates from catalog
            val initialRates = CurrencyCatalog.defaultSeedRatesToEur.map { (code, rate) ->
                RateEntity(
                    currencyCode = code,
                    rateToEur = rate,
                    lastUpdatedMillis = now
                )
            }
            database.rateDao().insertRates(initialRates)

            // Seed initial favorite pairs
            database.favoritePairDao().insertFavorite(
                FavoritePairEntity(fromCurrency = "EUR", toCurrency = "TRY")
            )
            database.favoritePairDao().insertFavorite(
                FavoritePairEntity(fromCurrency = "TRY", toCurrency = "EUR")
            )
            database.favoritePairDao().insertFavorite(
                FavoritePairEntity(fromCurrency = "EUR", toCurrency = "USD")
            )
            database.favoritePairDao().insertFavorite(
                FavoritePairEntity(fromCurrency = "USD", toCurrency = "EUR")
            )

            // Seed 300 EUR saved -> calculated in TRY
            database.holdingDao().insertHolding(
                HoldingEntity(
                    title = "Vakantie Turkije",
                    sourceCurrency = "EUR",
                    amount = 300.0,
                    targetCurrency = "TRY",
                    note = "Gespaard vakantiepotje",
                    updatedAt = now
                )
            )
            database.holdingDao().insertHolding(
                HoldingEntity(
                    title = "Spaarbuffer",
                    sourceCurrency = "EUR",
                    amount = 1250.0,
                    targetCurrency = "USD",
                    note = "Voor noodgevallen",
                    updatedAt = now
                )
            )
        }
    }
}
