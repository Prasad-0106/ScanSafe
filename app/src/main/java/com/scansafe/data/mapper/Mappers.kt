package com.scansafe.data.mapper

import com.google.gson.Gson
import com.scansafe.data.local.entity.FavouriteEntity
import com.scansafe.data.local.entity.ProductEntity
import com.scansafe.data.local.entity.ScanHistoryEntity
import com.scansafe.data.remote.dto.*
import com.scansafe.domain.model.*

private val gson = Gson()

// ─── Product Mappers ──────────────────────────────────────

fun ProductResponseDto.toDomain(): Product = Product(
    id = id,
    barcode = barcode,
    name = name,
    brand = brand,
    quantity = quantity,
    imageUrl = imageUrl,
    ingredients = ingredients,
    nutriScore = nutriScore,
    nutrition = nutrition.toDomain(),
    aiAnalysis = aiAnalysis?.toDomain(),
    cachedAt = cachedAt
)

fun NutritionDto.toDomain(): NutritionInfo = NutritionInfo(
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    sugar = sugar,
    fiber = fiber,
    sodium = sodium
)

fun AiAnalysisDto.toDomain(): AiAnalysis = AiAnalysis(
    healthScore = healthScore,
    scoreLabel = scoreLabel,
    summary = summary,
    harmfulIngredients = harmfulIngredients.map { it.toDomain() },
    safeIngredients = safeIngredients,
    neutralIngredients = neutralIngredients,
    allergensDetected = allergensDetected,
    additives = additives.map { it.toDomain() },
    recommendation = recommendation
)

fun HarmfulIngredientDto.toDomain(): HarmfulIngredient = HarmfulIngredient(
    name = name,
    reason = reason,
    riskLevel = riskLevel
)

fun AdditiveDto.toDomain(): Additive = Additive(
    code = code,
    name = name,
    risk = risk,
    note = note
)

// ─── Entity Mappers ───────────────────────────────────────

fun ProductResponseDto.toEntity(): ProductEntity = ProductEntity(
    barcode = barcode,
    id = id,
    name = name,
    brand = brand,
    quantity = quantity,
    imageUrl = imageUrl,
    ingredients = ingredients,
    nutriScore = nutriScore,
    calories = nutrition.calories,
    protein = nutrition.protein,
    carbs = nutrition.carbs,
    fat = nutrition.fat,
    sugar = nutrition.sugar,
    fiber = nutrition.fiber,
    sodium = nutrition.sodium,
    aiAnalysisJson = aiAnalysis?.let { gson.toJson(it) },
    cachedAt = System.currentTimeMillis()
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    barcode = barcode,
    name = name,
    brand = brand,
    quantity = quantity,
    imageUrl = imageUrl,
    ingredients = ingredients,
    nutriScore = nutriScore,
    nutrition = NutritionInfo(calories, protein, carbs, fat, sugar, fiber, sodium),
    aiAnalysis = aiAnalysisJson?.let {
        try { gson.fromJson(it, AiAnalysisDto::class.java).toDomain() } catch (e: Exception) { null }
    },
    cachedAt = cachedAt
)

// ─── History Mappers ─────────────────────────────────────

fun ScanHistoryDto.toDomain(): ScanHistory = ScanHistory(
    id = id,
    product = product.toDomain(),
    scannedAt = scannedAt
)

fun ScanHistoryEntity.toHistoryWithProduct(product: Product): ScanHistory = ScanHistory(
    id = id,
    product = product,
    scannedAt = scannedAt
)

// ─── Favourite Mappers ───────────────────────────────────

fun FavouriteDto.toDomain(): Favourite = Favourite(
    id = id,
    product = product.toDomain(),
    savedAt = savedAt
)

// ─── User Mappers ────────────────────────────────────────

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    totalScans = totalScans,
    averageScore = averageScore,
    preferences = preferences.toDomain()
)

fun UserPreferencesDto.toDomain(): UserPreferences = UserPreferences(
    isVegan = isVegan,
    isGlutenFree = isGlutenFree,
    isDiabeticFriendly = isDiabeticFriendly,
    isKeto = isKeto,
    isDarkMode = isDarkMode,
    notificationsEnabled = notificationsEnabled
)

fun UserPreferences.toDto(): UserPreferencesDto = UserPreferencesDto(
    isVegan = isVegan,
    isGlutenFree = isGlutenFree,
    isDiabeticFriendly = isDiabeticFriendly,
    isKeto = isKeto,
    isDarkMode = isDarkMode,
    notificationsEnabled = notificationsEnabled
)
