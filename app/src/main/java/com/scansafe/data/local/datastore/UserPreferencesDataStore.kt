package com.scansafe.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scansafe.core.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scansafe_prefs")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private val AUTH_TOKEN = stringPreferencesKey(Constants.PREF_AUTH_TOKEN)
    private val REFRESH_TOKEN = stringPreferencesKey(Constants.PREF_REFRESH_TOKEN)
    private val USER_ID = stringPreferencesKey(Constants.PREF_USER_ID)
    private val USER_EMAIL = stringPreferencesKey(Constants.PREF_USER_EMAIL)
    private val USER_NAME = stringPreferencesKey(Constants.PREF_USER_NAME)
    private val ONBOARDING_DONE = booleanPreferencesKey(Constants.PREF_ONBOARDING_DONE)
    private val DARK_MODE = booleanPreferencesKey(Constants.PREF_DARK_MODE)
    private val PREF_VEGAN = booleanPreferencesKey(Constants.PREF_PREF_VEGAN)
    private val PREF_GLUTEN_FREE = booleanPreferencesKey(Constants.PREF_PREF_GLUTEN_FREE)
    private val PREF_DIABETIC = booleanPreferencesKey(Constants.PREF_PREF_DIABETIC)
    private val PREF_KETO = booleanPreferencesKey(Constants.PREF_PREF_KETO)
    private val NOTIFICATIONS = booleanPreferencesKey(Constants.PREF_NOTIFICATIONS)

    // Auth token flow
    val authToken: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[AUTH_TOKEN] }

    val isLoggedIn: Flow<Boolean> = authToken.map { !it.isNullOrEmpty() }

    val isOnboardingDone: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ONBOARDING_DONE] ?: false }

    val isDarkMode: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[DARK_MODE] ?: false }

    val userId: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[USER_ID] }

    val userName: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[USER_NAME] }

    val userEmail: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[USER_EMAIL] }

    suspend fun saveAuthToken(token: String, refreshToken: String = "") {
        dataStore.edit {
            it[AUTH_TOKEN] = token
            it[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUserInfo(id: String, name: String, email: String) {
        dataStore.edit {
            it[USER_ID] = id
            it[USER_NAME] = name
            it[USER_EMAIL] = email
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[ONBOARDING_DONE] = done }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun clearAuth() {
        dataStore.edit {
            it.remove(AUTH_TOKEN)
            it.remove(REFRESH_TOKEN)
            it.remove(USER_ID)
            it.remove(USER_EMAIL)
            it.remove(USER_NAME)
        }
    }
}
