package com.scansafe.domain.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import com.scansafe.domain.model.UserPreferences

interface UserRepository {
    suspend fun getUserProfile(): NetworkResult<User>
    suspend fun updateProfile(name: String): NetworkResult<User>
    suspend fun updatePreferences(preferences: UserPreferences): NetworkResult<User>
}
