package com.scansafe.presentation.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.domain.model.Favourite
import com.scansafe.domain.usecase.GetFavouritesUseCase
import com.scansafe.domain.usecase.RemoveFavouriteUseCase
import com.scansafe.domain.usecase.SyncFavouritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val getFavouritesUseCase: GetFavouritesUseCase,
    private val removeFavouriteUseCase: RemoveFavouriteUseCase,
    private val syncFavouritesUseCase: SyncFavouritesUseCase
) : ViewModel() {

    val favourites: StateFlow<List<Favourite>> = getFavouritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncFavourites()
    }

    fun syncFavourites() {
        viewModelScope.launch {
            syncFavouritesUseCase()
        }
    }

    fun removeFavourite(id: String) {
        viewModelScope.launch { removeFavouriteUseCase(id) }
    }
}
