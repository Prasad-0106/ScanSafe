package com.scansafe.core.di

import com.scansafe.data.repository.AuthRepositoryImpl
import com.scansafe.data.repository.FavouriteRepositoryImpl
import com.scansafe.data.repository.HistoryRepositoryImpl
import com.scansafe.data.repository.ProductRepositoryImpl
import com.scansafe.data.repository.UserRepositoryImpl
import com.scansafe.domain.repository.AuthRepository
import com.scansafe.domain.repository.FavouriteRepository
import com.scansafe.domain.repository.HistoryRepository
import com.scansafe.domain.repository.ProductRepository
import com.scansafe.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
