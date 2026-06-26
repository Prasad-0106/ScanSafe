package com.scansafe.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.core.network.NetworkResult
import com.scansafe.data.local.datastore.UserPreferencesDataStore
import com.scansafe.domain.model.User
import com.scansafe.domain.model.UserPreferences
import com.scansafe.domain.repository.AuthRepository
import com.scansafe.domain.usecase.GetUserProfileUseCase
import com.scansafe.domain.usecase.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val authRepository: AuthRepository,
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    val isDarkMode: StateFlow<Boolean> = dataStore.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = getUserProfileUseCase()) {
                is NetworkResult.Success -> _uiState.value = ProfileUiState.Success(result.data)
                is NetworkResult.Error -> _uiState.value = ProfileUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun updatePreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            updateUserPreferencesUseCase(preferences)
            dataStore.setDarkMode(preferences.isDarkMode)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDarkMode(enabled) }
        val current = (_uiState.value as? ProfileUiState.Success) ?: return
        updatePreferences(current.user.preferences.copy(isDarkMode = enabled))
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
