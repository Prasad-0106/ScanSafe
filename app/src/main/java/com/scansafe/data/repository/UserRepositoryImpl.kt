package com.scansafe.data.repository

import com.scansafe.core.network.NetworkResult
import com.scansafe.data.mapper.toDomain
import com.scansafe.data.mapper.toDto
import com.scansafe.data.remote.ApiService
import com.scansafe.data.remote.dto.UpdatePreferencesRequestDto
import com.scansafe.domain.model.User
import com.scansafe.domain.model.UserPreferences
import com.scansafe.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getUserProfile(): NetworkResult<User> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                NetworkResult.Success(response.body()!!.data!!.toDomain())
            } else {
                NetworkResult.Error(response.body()?.message ?: "Failed to get profile", response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "getUserProfile error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateProfile(name: String): NetworkResult<User> {
        return try {
            val response = apiService.updateProfile(mapOf("name" to name))
            if (response.isSuccessful && response.body()?.success == true) {
                NetworkResult.Success(response.body()!!.data!!.toDomain())
            } else {
                NetworkResult.Error(response.body()?.message ?: "Update failed", response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "updateProfile error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updatePreferences(preferences: UserPreferences): NetworkResult<User> {
        return try {
            val response = apiService.updatePreferences(UpdatePreferencesRequestDto(preferences.toDto()))
            if (response.isSuccessful && response.body()?.success == true) {
                NetworkResult.Success(response.body()!!.data!!.toDomain())
            } else {
                NetworkResult.Error(response.body()?.message ?: "Update failed", response.code())
            }
        } catch (e: Exception) {
            Timber.e(e, "updatePreferences error")
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}
