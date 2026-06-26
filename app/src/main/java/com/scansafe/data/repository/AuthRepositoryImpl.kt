package com.scansafe.data.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.data.local.ScanSafeDatabase
import com.scansafe.data.local.datastore.UserPreferencesDataStore
import com.scansafe.data.mapper.toDomain
import com.scansafe.data.remote.ApiService
import com.scansafe.data.remote.dto.LoginRequestDto
import com.scansafe.data.remote.dto.RegisterRequestDto
import com.scansafe.domain.model.User
import com.scansafe.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: UserPreferencesDataStore,
    private val database: ScanSafeDatabase
) : AuthRepository {

    override suspend fun login(email: String, password: String): NetworkResult<User> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                dataStore.saveAuthToken(data.token, data.refreshToken)
                dataStore.saveUserInfo(data.user.id, data.user.name, data.user.email)
                NetworkResult.Success(data.user.toDomain())
            } else {
                NetworkResult.Error(
                    response.body()?.message ?: "Login failed",
                    response.code()
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun register(name: String, email: String, password: String): NetworkResult<User> {
        return try {
            val response = apiService.register(RegisterRequestDto(name, email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                dataStore.saveAuthToken(data.token, data.refreshToken)
                dataStore.saveUserInfo(data.user.id, data.user.name, data.user.email)
                NetworkResult.Success(data.user.toDomain())
            } else {
                NetworkResult.Error(
                    response.body()?.message ?: "Registration failed",
                    response.code()
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Register error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun logout(): NetworkResult<Unit> {
        return try {
            apiService.logout()
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            dataStore.clearAuth()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            dataStore.clearAuth()
            NetworkResult.Success(Unit)
        }
    }

    override suspend fun getMe(): NetworkResult<User> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body()?.success == true) {
                NetworkResult.Success(response.body()!!.data!!.toDomain())
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to get user", response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "GetMe error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): NetworkResult<User> {
        return try {
            val response = apiService.googleSignIn(mapOf("idToken" to idToken))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                dataStore.saveAuthToken(data.token, data.refreshToken)
                dataStore.saveUserInfo(data.user.id, data.user.name, data.user.email)
                NetworkResult.Success(data.user.toDomain())
            } else {
                NetworkResult.Error(response.body()?.message ?: "Google sign-in failed", response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "Google sign-in error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = dataStore.isLoggedIn
}
