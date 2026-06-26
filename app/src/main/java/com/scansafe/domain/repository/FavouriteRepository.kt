package com.scansafe.domain.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.Favourite
import kotlinx.coroutines.flow.Flow

interface FavouriteRepository {
    fun getFavourites(): Flow<List<Favourite>>
    suspend fun addFavourite(barcode: String): NetworkResult<Favourite>
    suspend fun removeFavourite(id: String): NetworkResult<Unit>
    suspend fun isFavourite(barcode: String): Boolean
    suspend fun syncFavourites(): NetworkResult<List<Favourite>>
}
