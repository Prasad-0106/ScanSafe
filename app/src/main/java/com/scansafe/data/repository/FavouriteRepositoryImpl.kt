package com.scansafe.data.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.data.local.dao.FavouriteDao
import com.scansafe.data.local.dao.ProductDao
import com.scansafe.data.local.entity.FavouriteEntity
import com.scansafe.data.mapper.toDomain
import com.scansafe.data.mapper.toEntity
import com.scansafe.data.remote.ApiService
import com.scansafe.data.remote.dto.SaveFavouriteRequestDto
import com.scansafe.domain.model.Favourite
import com.scansafe.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val favouriteDao: FavouriteDao,
    private val productDao: ProductDao
) : FavouriteRepository {

    override fun getFavourites(): Flow<List<Favourite>> {
        return favouriteDao.getFavourites().map { favList ->
            favList.mapNotNull { favEntity ->
                val product = productDao.getProductByBarcode(favEntity.barcode)
                product?.let {
                    Favourite(
                        id = favEntity.id,
                        product = it.toDomain(),
                        savedAt = favEntity.savedAt
                    )
                }
            }
        }
    }

    override suspend fun addFavourite(barcode: String): NetworkResult<Favourite> {
        return try {
            val response = apiService.addFavourite(SaveFavouriteRequestDto(barcode))
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data!!
                favouriteDao.insertFavourite(
                    FavouriteEntity(id = dto.id, barcode = barcode, savedAt = dto.savedAt)
                )
                productDao.insertProduct(dto.product.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to save favourite")
            }
        } catch (e: Exception) {
            Timber.e(e, "addFavourite error")
            // Save locally
            val localId = UUID.randomUUID().toString()
            favouriteDao.insertFavourite(FavouriteEntity(id = localId, barcode = barcode))
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun removeFavourite(id: String): NetworkResult<Unit> {
        return try {
            favouriteDao.deleteFavourite(id)
            apiService.removeFavourite(id)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "removeFavourite error")
            NetworkResult.Error(e.message ?: "Delete failed")
        }
    }

    override suspend fun isFavourite(barcode: String): Boolean =
        favouriteDao.isFavourite(barcode) > 0

    override suspend fun syncFavourites(): NetworkResult<List<Favourite>> {
        return try {
            val response = apiService.getFavourites()
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                favouriteDao.clearFavourites()
                
                val validFavourites = dtos.filter { it.product != null && it.product.barcode.isNotEmpty() }
                val favourites = validFavourites.map { dto ->
                    productDao.insertProduct(dto.product.toEntity())
                    favouriteDao.insertFavourite(
                        FavouriteEntity(id = dto.id, barcode = dto.product.barcode, savedAt = dto.savedAt)
                    )
                    dto.toDomain()
                }
                NetworkResult.Success(favourites)
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to sync favourites")
            }
        } catch (e: Exception) {
            Timber.e(e, "syncFavourites error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}
