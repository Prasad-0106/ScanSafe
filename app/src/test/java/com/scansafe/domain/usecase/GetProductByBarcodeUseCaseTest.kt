package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.*
import com.scansafe.domain.repository.ProductRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetProductByBarcodeUseCaseTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var useCase: GetProductByBarcodeUseCase

    private val mockProduct = Product(
        barcode = "1234567890",
        name = "Test Chips",
        brand = "TestBrand",
        aiAnalysis = AiAnalysis(
            healthScore = 3,
            scoreLabel = "Dangerous",
            summary = "High in sodium and artificial additives.",
            harmfulIngredients = listOf(HarmfulIngredient("MSG", "Artificial flavor enhancer", "Medium")),
            safeIngredients = listOf("Salt"),
            neutralIngredients = listOf("Potato"),
            allergensDetected = listOf("Gluten"),
            additives = listOf(Additive("E621", "Monosodium Glutamate", "Moderate", "Flavor enhancer")),
            recommendation = "Consume in moderation."
        )
    )

    @Before
    fun setUp() {
        productRepository = mockk()
        useCase = GetProductByBarcodeUseCase(productRepository)
    }

    @Test
    fun `returns error when barcode is empty`() = runTest {
        val result = useCase("")
        assertTrue(result is NetworkResult.Error)
        assertEquals("Barcode cannot be empty", (result as NetworkResult.Error).message)
    }

    @Test
    fun `returns cached product when available`() = runTest {
        coEvery { productRepository.getCachedProduct("1234567890") } returns mockProduct
        val result = useCase("1234567890")
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockProduct, (result as NetworkResult.Success).data)
        coVerify(exactly = 0) { productRepository.getProductByBarcode(any()) }
    }

    @Test
    fun `fetches from network when cache is empty`() = runTest {
        coEvery { productRepository.getCachedProduct("1234567890") } returns null
        coEvery { productRepository.getProductByBarcode("1234567890") } returns NetworkResult.Success(mockProduct)
        val result = useCase("1234567890")
        assertTrue(result is NetworkResult.Success)
        coVerify { productRepository.getProductByBarcode("1234567890") }
    }

    @Test
    fun `returns network error when fetch fails`() = runTest {
        coEvery { productRepository.getCachedProduct(any()) } returns null
        coEvery { productRepository.getProductByBarcode(any()) } returns NetworkResult.Error("Product not found", 404)
        val result = useCase("9999999999")
        assertTrue(result is NetworkResult.Error)
        assertEquals(404, (result as NetworkResult.Error).code)
    }
}
