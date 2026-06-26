package com.scansafe.core.di

import android.content.Context
import androidx.room.Room
import com.scansafe.core.utils.Constants
import com.scansafe.data.local.ScanSafeDatabase
import com.scansafe.data.local.dao.FavouriteDao
import com.scansafe.data.local.dao.ProductDao
import com.scansafe.data.local.dao.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScanSafeDatabase =
        Room.databaseBuilder(
            context,
            ScanSafeDatabase::class.java,
            Constants.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideProductDao(db: ScanSafeDatabase): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun provideScanHistoryDao(db: ScanSafeDatabase): ScanHistoryDao = db.scanHistoryDao()

    @Provides
    @Singleton
    fun provideFavouriteDao(db: ScanSafeDatabase): FavouriteDao = db.favouriteDao()
}
