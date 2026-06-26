package com.scansafe.domain.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.Product

interface ProductRepository {
    suspend fun getProductByBarcode(barcode: String): NetworkResult<Product>
    suspend fun getCachedProduct(barcode: String): Product?
}
