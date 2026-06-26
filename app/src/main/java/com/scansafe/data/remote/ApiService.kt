package com.scansafe.data.remote

import com.scansafe.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<ApiResponse<AuthResponseDto>>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<ApiResponse<AuthResponseDto>>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("api/auth/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>

    @POST("api/auth/google")
    suspend fun googleSignIn(@Body body: Map<String, String>): Response<ApiResponse<AuthResponseDto>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto): Response<ApiResponse<Unit>>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestDto): Response<ApiResponse<Unit>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto): Response<ApiResponse<Unit>>

    // ─── Product ──────────────────────────────────────
    @GET("api/product/{barcode}")
    suspend fun getProduct(@Path("barcode") barcode: String): Response<ApiResponse<ProductResponseDto>>

    // ─── History ──────────────────────────────────────
    @GET("api/history")
    suspend fun getHistory(): Response<ApiResponse<List<ScanHistoryDto>>>

    @POST("api/history")
    suspend fun saveHistory(@Body body: SaveHistoryRequestDto): Response<ApiResponse<ScanHistoryDto>>

    @DELETE("api/history/{id}")
    suspend fun deleteHistory(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ─── Favourites ───────────────────────────────────
    @GET("api/favourites")
    suspend fun getFavourites(): Response<ApiResponse<List<FavouriteDto>>>

    @POST("api/favourites")
    suspend fun addFavourite(@Body body: SaveFavouriteRequestDto): Response<ApiResponse<FavouriteDto>>

    @DELETE("api/favourites/{id}")
    suspend fun removeFavourite(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ─── User ─────────────────────────────────────────
    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserDto>>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<ApiResponse<UserDto>>

    @PUT("api/user/preferences")
    suspend fun updatePreferences(@Body body: UpdatePreferencesRequestDto): Response<ApiResponse<UserDto>>
}
