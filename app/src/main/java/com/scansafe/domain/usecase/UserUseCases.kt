package com.scansafe.domain.usecase

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import com.scansafe.domain.model.UserPreferences
import com.scansafe.domain.repository.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): NetworkResult<User> = userRepository.getUserProfile()
}

class UpdateUserPreferencesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(preferences: UserPreferences): NetworkResult<User> =
        userRepository.updatePreferences(preferences)
}
