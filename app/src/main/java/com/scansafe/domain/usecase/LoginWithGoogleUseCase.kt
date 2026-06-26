package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import com.scansafe.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): NetworkResult<User> {
        if (idToken.isBlank()) return NetworkResult.Error("Google ID Token is missing")
        return authRepository.loginWithGoogle(idToken)
    }
}
