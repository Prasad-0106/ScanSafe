package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.ScanHistory
import com.scansafe.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveToHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(barcode: String): NetworkResult<ScanHistory> =
        historyRepository.saveToHistory(barcode)
}

class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<ScanHistory>> = historyRepository.getHistory()
}

class SyncHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(): NetworkResult<List<ScanHistory>> =
        historyRepository.syncHistory()
}

class DeleteHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(id: String): NetworkResult<Unit> =
        historyRepository.deleteHistory(id)
}
