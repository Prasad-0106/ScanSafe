package com.scansafe.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── Auth DTOs ───────────────────────────────────────────

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    @SerializedName("refreshToken") val refreshToken: String = "",
    val user: UserDto
)

data class ForgotPasswordRequestDto(val email: String)

data class VerifyOtpRequestDto(val email: String, val otp: String)

data class ResetPasswordRequestDto(
    val email: String,
    val otp: String,
    @SerializedName("newPassword") val newPassword: String
)

// ─── User DTO ────────────────────────────────────────────

data class UserDto(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val email: String = "",
    @SerializedName("totalScans") val totalScans: Int = 0,
    @SerializedName("averageScore") val averageScore: Double = 0.0,
    val preferences: UserPreferencesDto = UserPreferencesDto()
)

data class UserPreferencesDto(
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val isDiabeticFriendly: Boolean = false,
    val isKeto: Boolean = false,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

// ─── Product DTOs ─────────────────────────────────────────

data class ProductResponseDto(
    @SerializedName("_id") val id: String = "",
    val barcode: String = "",
    val name: String = "",
    val brand: String = "",
    val quantity: String = "",
    @SerializedName("image") val imageUrl: String = "",
    val ingredients: String = "",
    @SerializedName("nutriScore") val nutriScore: String = "",
    val nutrition: NutritionDto = NutritionDto(),
    @SerializedName("aiAnalysis") val aiAnalysis: AiAnalysisDto? = null,
    val cachedAt: Long = 0L
)

data class NutritionDto(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val sugar: Double = 0.0,
    val fiber: Double = 0.0,
    val sodium: Double = 0.0
)

data class AiAnalysisDto(
    @SerializedName("health_score") val healthScore: Int = 0,
    @SerializedName("score_label") val scoreLabel: String = "",
    val summary: String = "",
    @SerializedName("harmful_ingredients") val harmfulIngredients: List<HarmfulIngredientDto> = emptyList(),
    @SerializedName("safe_ingredients") val safeIngredients: List<String> = emptyList(),
    @SerializedName("neutral_ingredients") val neutralIngredients: List<String> = emptyList(),
    @SerializedName("allergens_detected") val allergensDetected: List<String> = emptyList(),
    val additives: List<AdditiveDto> = emptyList(),
    val recommendation: String = ""
)

data class HarmfulIngredientDto(
    val name: String = "",
    val reason: String = "",
    @SerializedName("risk_level") val riskLevel: String = ""
)

data class AdditiveDto(
    val code: String = "",
    val name: String = "",
    val risk: String = "",
    val note: String = ""
)

// ─── History DTOs ─────────────────────────────────────────

data class ScanHistoryDto(
    @SerializedName("_id") val id: String = "",
    val product: ProductResponseDto = ProductResponseDto(),
    val scannedAt: Long = 0L
)

data class SaveHistoryRequestDto(
    val barcode: String
)

// ─── Favourite DTOs ───────────────────────────────────────

data class FavouriteDto(
    @SerializedName("_id") val id: String = "",
    val product: ProductResponseDto = ProductResponseDto(),
    val savedAt: Long = 0L
)

data class SaveFavouriteRequestDto(
    val barcode: String
)

// ─── Generic API Response ─────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null
)

data class UpdatePreferencesRequestDto(
    val preferences: UserPreferencesDto
)
