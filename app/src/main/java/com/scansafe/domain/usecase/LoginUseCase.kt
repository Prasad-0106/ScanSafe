package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import com.scansafe.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<User> {
        if (email.isBlank()) return NetworkResult.Error("Email cannot be empty")
        if (password.isBlank()) return NetworkResult.Error("Password cannot be empty")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return NetworkResult.Error("Invalid email format")
        }
        return authRepository.login(email.trim(), password)
    }
}
