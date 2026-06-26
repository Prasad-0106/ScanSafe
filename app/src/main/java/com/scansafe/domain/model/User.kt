package com.scansafe.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val totalScans: Int = 0,
    val averageScore: Double = 0.0,
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Long = System.currentTimeMillis()
)

data class UserPreferences(
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isDiabeticFriendly: Boolean = false,
    val isKeto: Boolean = false,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)
