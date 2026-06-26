package com.scansafe.data.local.dao

import androidx.room.*
import com.scansafe.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE barcode = :barcode")
    suspend fun deleteProduct(barcode: String)

    @Query("SELECT * FROM products ORDER BY cachedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("DELETE FROM products WHERE cachedAt < :expiryTime")
    suspend fun deleteExpiredCache(expiryTime: Long)
}
