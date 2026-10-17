package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FavoritePairEntity
import com.example.data.local.entity.HoldingEntity
import com.example.data.local.entity.RateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateDao {
    @Query("SELECT * FROM rates")
    fun getAllRates(): Flow<List<RateEntity>>

    @Query("SELECT * FROM rates")
    suspend fun getAllRatesSync(): List<RateEntity>

    @Query("SELECT * FROM rates WHERE currencyCode = :code LIMIT 1")
    suspend fun getRate(code: String): RateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<RateEntity>)

    @Query("SELECT MAX(lastUpdatedMillis) FROM rates")
    fun getLatestTimestamp(): Flow<Long?>
}

@Dao
interface FavoritePairDao {
    @Query("SELECT * FROM favorite_pairs ORDER BY id ASC")
    fun getAllFavorites(): Flow<List<FavoritePairEntity>>

    @Query("SELECT * FROM favorite_pairs WHERE fromCurrency = :fromCode AND toCurrency = :toCode LIMIT 1")
    suspend fun getFavorite(fromCode: String, toCode: String): FavoritePairEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(pair: FavoritePairEntity): Long

    @Query("DELETE FROM favorite_pairs WHERE fromCurrency = :fromCode AND toCurrency = :toCode")
    suspend fun deleteFavorite(fromCode: String, toCode: String)

    @Delete
    suspend fun delete(pair: FavoritePairEntity)
}

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings ORDER BY updatedAt DESC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE id = :id LIMIT 1")
    suspend fun getHoldingById(id: Long): HoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity): Long

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Delete
    suspend fun deleteHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
