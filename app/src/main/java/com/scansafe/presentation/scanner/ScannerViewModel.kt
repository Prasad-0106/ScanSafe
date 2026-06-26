package com.scansafe.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.ScanHistory
import com.scansafe.domain.usecase.GetHistoryUseCase
import com.scansafe.domain.usecase.SaveToHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerUiEvent {
    data class ProductFound(val barcode: String) : ScannerUiEvent()
    data class Error(val message: String) : ScannerUiEvent()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val saveToHistoryUseCase: SaveToHistoryUseCase
) : ViewModel() {

    val recentHistory: StateFlow<List<ScanHistory>> = getHistoryUseCase()
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanEvent = MutableSharedFlow<ScannerUiEvent>()
    val scanEvent = _scanEvent.asSharedFlow()

    private var lastScannedBarcode = ""
    private var isProcessing = false

    fun onBarcodeDetected(barcode: String) {
        if (barcode == lastScannedBarcode || isProcessing) return
        isProcessing = true
        lastScannedBarcode = barcode

        viewModelScope.launch {
            _scanEvent.emit(ScannerUiEvent.ProductFound(barcode))
            saveToHistoryUseCase(barcode)
            isProcessing = false
        }
    }

    fun onManualBarcodeEntered(barcode: String) {
        if (barcode.isBlank()) return
        viewModelScope.launch {
            _scanEvent.emit(ScannerUiEvent.ProductFound(barcode.trim()))
            saveToHistoryUseCase(barcode.trim())
        }
    }

    fun resetScanner() {
        lastScannedBarcode = ""
        isProcessing = false
    }
}
