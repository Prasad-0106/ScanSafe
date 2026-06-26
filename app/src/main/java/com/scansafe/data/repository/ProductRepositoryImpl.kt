package com.scansafe.data.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.core.utils.Constants
import com.scansafe.data.local.dao.ProductDao
import com.scansafe.data.mapper.toDomain
import com.scansafe.data.mapper.toEntity
import com.scansafe.data.remote.ApiService
import com.scansafe.domain.model.Product
import com.scansafe.domain.repository.ProductRepository
import timber.log.Timber
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getProductByBarcode(barcode: String): NetworkResult<Product> {
        return try {
            val response = apiService.getProduct(barcode)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data!!
                // Cache in Room
                productDao.insertProduct(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                // Try local cache as fallback
                val cached = productDao.getProductByBarcode(barcode)
                if (cached != null) {
                    NetworkResult.Success(cached.toDomain())
                } else {
                    NetworkResult.Error(
                        response.body()?.message ?: "Product not found",
                        response.code()
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "getProductByBarcode error")
            // Return cached data on network failure
            val cached = productDao.getProductByBarcode(barcode)
            if (cached != null) {
                NetworkResult.Success(cached.toDomain())
            } else {
                NetworkResult.Error(e.message ?: "Network error")
            }
        }
    }

    override suspend fun getCachedProduct(barcode: String): Product? {
        val entity = productDao.getProductByBarcode(barcode) ?: return null
        val age = System.currentTimeMillis() - entity.cachedAt
        return if (age < Constants.PRODUCT_CACHE_TTL_MS) entity.toDomain() else null
    }
}
