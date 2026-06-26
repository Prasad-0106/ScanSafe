package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import com.scansafe.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): NetworkResult<User> {
        if (name.isBlank()) return NetworkResult.Error("Name cannot be empty")
        if (email.isBlank()) return NetworkResult.Error("Email cannot be empty")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return NetworkResult.Error("Invalid email format")
        }
        if (password.length < 8) return NetworkResult.Error("Password must be at least 8 characters")
        if (password != confirmPassword) return NetworkResult.Error("Passwords do not match")
        return authRepository.register(name.trim(), email.trim(), password)
    }
}
