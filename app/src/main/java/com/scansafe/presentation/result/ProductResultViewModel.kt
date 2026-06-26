package com.scansafe.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.Product
import com.scansafe.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val product: Product, val isFavourite: Boolean) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

@HiltViewModel
class ProductResultViewModel @Inject constructor(
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val saveToHistoryUseCase: SaveToHistoryUseCase,
    private val addFavouriteUseCase: AddFavouriteUseCase,
    private val removeFavouriteUseCase: RemoveFavouriteUseCase,
    private val checkIsFavouriteUseCase: CheckIsFavouriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun loadProduct(barcode: String) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = getProductByBarcodeUseCase(barcode)) {
                is NetworkResult.Success -> {
                    val isFav = checkIsFavouriteUseCase(barcode)
                    _uiState.value = ProductUiState.Success(result.data, isFav)
                    // Automatically save to history
                    saveToHistoryUseCase(barcode)
                }
                is NetworkResult.Error -> _uiState.value = ProductUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun toggleFavourite(barcode: String) {
        val current = _uiState.value as? ProductUiState.Success ?: return
        viewModelScope.launch {
            if (current.isFavourite) {
                val fav = removeFavouriteUseCase(current.product.id)
                _uiState.value = current.copy(isFavourite = false)
                _snackbarMessage.emit("Removed from favourites")
            } else {
                addFavouriteUseCase(barcode)
                _uiState.value = current.copy(isFavourite = true)
                _snackbarMessage.emit("Added to favourites ❤️")
            }
        }
    }

    fun saveToHistory(barcode: String) {
        viewModelScope.launch {
            saveToHistoryUseCase(barcode)
            _snackbarMessage.emit("Saved to history ✅")
        }
    }
}
