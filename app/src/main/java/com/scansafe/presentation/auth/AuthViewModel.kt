package com.scansafe.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.core.network.NetworkResult
import com.scansafe.data.remote.ApiService
import com.scansafe.domain.model.User
import com.scansafe.domain.usecase.LoginUseCase
import com.scansafe.domain.usecase.LoginWithGoogleUseCase
import com.scansafe.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object PasswordReset : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class OtpSent(val email: String) : AuthUiState()
    data class OtpVerified(val email: String, val otp: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = loginUseCase(email, password)) {
                is NetworkResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    _navigationEvent.emit(Unit)
                }
                is NetworkResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = registerUseCase(name, email, password, confirmPassword)) {
                is NetworkResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    _navigationEvent.emit(Unit)
                }
                is NetworkResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = loginWithGoogleUseCase(idToken)) {
                is NetworkResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                    _navigationEvent.emit(Unit)
                }
                is NetworkResult.Error -> _uiState.value = AuthUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val body = com.scansafe.data.remote.dto.ForgotPasswordRequestDto(email.trim())
                val response = apiService.forgotPassword(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = AuthUiState.OtpSent(email.trim())
                } else {
                    _uiState.value = AuthUiState.Error(response.body()?.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val body = com.scansafe.data.remote.dto.VerifyOtpRequestDto(email.trim(), otp.trim())
                val response = apiService.verifyOtp(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = AuthUiState.OtpVerified(email.trim(), otp.trim())
                } else {
                    _uiState.value = AuthUiState.Error(response.body()?.message ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val body = com.scansafe.data.remote.dto.ResetPasswordRequestDto(email.trim(), otp.trim(), newPassword)
                val response = apiService.resetPassword(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = AuthUiState.PasswordReset
                } else {
                    _uiState.value = AuthUiState.Error(response.body()?.message ?: "Reset failed")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}
