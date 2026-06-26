package com.scansafe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.scansafe.data.local.dao.FavouriteDao
import com.scansafe.data.local.dao.ProductDao
import com.scansafe.data.local.dao.ScanHistoryDao
import com.scansafe.data.local.entity.FavouriteEntity
import com.scansafe.data.local.entity.ProductEntity
import com.scansafe.data.local.entity.ScanHistoryEntity

@Database(
    entities = [ProductEntity::class, ScanHistoryEntity::class, FavouriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScanSafeDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun favouriteDao(): FavouriteDao
}
