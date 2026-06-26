package com.scansafe.domain.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.ScanHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<List<ScanHistory>>
    suspend fun saveToHistory(barcode: String): NetworkResult<ScanHistory>
    suspend fun deleteHistory(id: String): NetworkResult<Unit>
    suspend fun syncHistory(): NetworkResult<List<ScanHistory>>
}
