package com.scansafe.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scansafe.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object Scanner : SplashDestination()
    object Idle : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Idle)
    val destination: StateFlow<SplashDestination> = _destination

    init {
        viewModelScope.launch {
            combine(dataStore.isLoggedIn, dataStore.isOnboardingDone) { loggedIn, onboardingDone ->
                when {
                    loggedIn -> SplashDestination.Scanner
                    onboardingDone -> SplashDestination.Onboarding
                    else -> SplashDestination.Onboarding
                }
            }.collect { _destination.value = it }
        }
    }
}
