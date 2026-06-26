package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.Product
import com.scansafe.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): NetworkResult<Product> {
        if (barcode.isBlank()) return NetworkResult.Error("Barcode cannot be empty")
        // Check local cache first for fast response
        val cached = productRepository.getCachedProduct(barcode)
        if (cached != null) return NetworkResult.Success(cached)
        return productRepository.getProductByBarcode(barcode)
    }
}
