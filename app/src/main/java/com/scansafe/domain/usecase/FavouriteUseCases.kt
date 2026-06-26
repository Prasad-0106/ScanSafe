package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.Favourite
import com.scansafe.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavouritesUseCase @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) {
    operator fun invoke(): Flow<List<Favourite>> = favouriteRepository.getFavourites()
}

class AddFavouriteUseCase @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) {
    suspend operator fun invoke(barcode: String): NetworkResult<Favourite> =
        favouriteRepository.addFavourite(barcode)
}

class RemoveFavouriteUseCase @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) {
    suspend operator fun invoke(id: String): NetworkResult<Unit> =
        favouriteRepository.removeFavourite(id)
}

class CheckIsFavouriteUseCase @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) {
    suspend operator fun invoke(barcode: String): Boolean =
        favouriteRepository.isFavourite(barcode)
}

class SyncFavouritesUseCase @Inject constructor(
    private val favouriteRepository: FavouriteRepository
) {
    suspend operator fun invoke(): NetworkResult<List<Favourite>> =
        favouriteRepository.syncFavourites()
}
