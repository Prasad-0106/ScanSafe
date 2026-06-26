package com.scansafe.core.utils

object Constants {
    // API Endpoints
    const val ENDPOINT_LOGIN = "api/auth/login"
    const val ENDPOINT_REGISTER = "api/auth/register"
    const val ENDPOINT_LOGOUT = "api/auth/logout"
    const val ENDPOINT_ME = "api/auth/me"

    const val ENDPOINT_PRODUCT = "api/product/{barcode}"
    const val ENDPOINT_HISTORY = "api/history"
    const val ENDPOINT_HISTORY_DELETE = "api/history/{id}"

    const val ENDPOINT_FAVOURITES = "api/favourites"
    const val ENDPOINT_FAVOURITES_DELETE = "api/favourites/{id}"

    const val ENDPOINT_PROFILE = "api/user/profile"
    const val ENDPOINT_PREFERENCES = "api/user/preferences"

    // DataStore keys
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_REFRESH_TOKEN = "refresh_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_NAME = "user_name"
    const val PREF_ONBOARDING_DONE = "onboarding_done"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_PREF_VEGAN = "pref_vegan"
    const val PREF_PREF_GLUTEN_FREE = "pref_gluten_free"
    const val PREF_PREF_DIABETIC = "pref_diabetic"
    const val PREF_PREF_KETO = "pref_keto"
    const val PREF_NOTIFICATIONS = "notifications_enabled"

    // DB
    const val DB_NAME = "scansafe_db"

    // Pagination
    const val PAGE_SIZE = 20

    // Health Score Thresholds
    const val SCORE_DANGEROUS_MAX = 3
    const val SCORE_MODERATE_MAX = 6

    // Cache TTL (ms)
    const val PRODUCT_CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours

    // Network timeouts (seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 30L
}
