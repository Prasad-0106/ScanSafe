package com.scansafe.domain.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): NetworkResult<User>
    suspend fun register(name: String, email: String, password: String): NetworkResult<User>
    suspend fun logout(): NetworkResult<Unit>
    suspend fun getMe(): NetworkResult<User>
    suspend fun loginWithGoogle(idToken: String): NetworkResult<User>
    fun isLoggedIn(): Flow<Boolean>
}
