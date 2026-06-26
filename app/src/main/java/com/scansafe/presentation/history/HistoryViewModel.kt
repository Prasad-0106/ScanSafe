package com.scansafe.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.domain.model.ScanHistory
import com.scansafe.domain.usecase.DeleteHistoryUseCase
import com.scansafe.domain.usecase.GetHistoryUseCase
import com.scansafe.domain.usecase.SyncHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteHistoryUseCase: DeleteHistoryUseCase,
    private val syncHistoryUseCase: SyncHistoryUseCase
) : ViewModel() {

    init {
        syncHistory()
    }

    fun syncHistory() {
        viewModelScope.launch {
            syncHistoryUseCase()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val history: StateFlow<List<ScanHistory>> = combine(
        getHistoryUseCase().map { list -> list.distinctBy { it.product.barcode } },
        _searchQuery
    ) { historyList, query ->
        if (query.isBlank()) historyList
        else historyList.filter {
            it.product.name.contains(query, ignoreCase = true) ||
            it.product.brand.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    fun deleteHistory(id: String) {
        viewModelScope.launch { deleteHistoryUseCase(id) }
    }
}
