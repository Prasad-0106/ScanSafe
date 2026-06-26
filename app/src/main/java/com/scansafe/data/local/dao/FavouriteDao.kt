package com.scansafe.data.local.dao

import androidx.room.*
import com.scansafe.data.local.entity.FavouriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites ORDER BY savedAt DESC")
    fun getFavourites(): Flow<List<FavouriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE id = :id")
    suspend fun deleteFavourite(id: String)

    @Query("SELECT COUNT(*) FROM favourites WHERE barcode = :barcode")
    suspend fun isFavourite(barcode: String): Int

    @Query("SELECT * FROM favourites WHERE barcode = :barcode LIMIT 1")
    suspend fun getFavouriteByBarcode(barcode: String): FavouriteEntity?

    @Query("DELETE FROM favourites")
    suspend fun clearFavourites()
}
