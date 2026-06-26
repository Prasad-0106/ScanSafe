package com.scansafe.data.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.data.local.dao.FavouriteDao
import com.scansafe.data.local.dao.ProductDao
import com.scansafe.data.local.dao.ScanHistoryDao
import com.scansafe.data.local.entity.ScanHistoryEntity
import com.scansafe.data.mapper.toDomain
import com.scansafe.data.mapper.toEntity
import com.scansafe.data.mapper.toHistoryWithProduct
import com.scansafe.data.remote.ApiService
import com.scansafe.data.remote.dto.SaveHistoryRequestDto
import com.scansafe.domain.model.ScanHistory
import com.scansafe.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val scanHistoryDao: ScanHistoryDao,
    private val productDao: ProductDao
) : HistoryRepository {

    override fun getHistory(): Flow<List<ScanHistory>> {
        return scanHistoryDao.getHistory().map { historyList ->
            historyList.mapNotNull { historyEntity ->
                val productEntity = productDao.getProductByBarcode(historyEntity.barcode)
                productEntity?.let { historyEntity.toHistoryWithProduct(it.toDomain()) }
            }
        }
    }

    override suspend fun saveToHistory(barcode: String): NetworkResult<ScanHistory> {
        return try {
            val response = apiService.saveHistory(SaveHistoryRequestDto(barcode))
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data!!
                // Also save locally
                scanHistoryDao.insertHistory(
                    ScanHistoryEntity(id = dto.id, barcode = barcode, scannedAt = dto.scannedAt)
                )
                productDao.insertProduct(dto.product.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                // Save locally anyway for offline support
                val localId = UUID.randomUUID().toString()
                scanHistoryDao.insertHistory(
                    ScanHistoryEntity(id = localId, barcode = barcode)
                )
                NetworkResult.Error(response.body()?.message ?: "Failed to save", response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "saveToHistory error")
            val localId = UUID.randomUUID().toString()
            scanHistoryDao.insertHistory(ScanHistoryEntity(id = localId, barcode = barcode))
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun deleteHistory(id: String): NetworkResult<Unit> {
        return try {
            scanHistoryDao.deleteHistory(id)
            apiService.deleteHistory(id)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "deleteHistory error")
            NetworkResult.Error(e.message ?: "Delete failed")
        }
    }

    override suspend fun syncHistory(): NetworkResult<List<ScanHistory>> {
        return try {
            val response = apiService.getHistory()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.data ?: emptyList()
                scanHistoryDao.clearAll()
                
                val validHistory = list.filter { it.product != null && it.product.barcode.isNotEmpty() }
                validHistory.forEach { dto ->
                    scanHistoryDao.insertHistory(
                        ScanHistoryEntity(id = dto.id, barcode = dto.product.barcode, scannedAt = dto.scannedAt)
                    )
                    productDao.insertProduct(dto.product.toEntity())
                }
                NetworkResult.Success(validHistory.map { it.toDomain() })
            } else {
                NetworkResult.Error(response.body()?.message ?: "Sync failed")
            }
        } catch (e: Exception) {
            Timber.e(e, "syncHistory error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}
