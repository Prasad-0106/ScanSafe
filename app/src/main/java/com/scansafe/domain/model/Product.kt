package com.scansafe.domain.model

data class Product(
    val id: String = "",
    val barcode: String,
    val name: String,
    val brand: String = "",
    val quantity: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",
    val nutriScore: String = "", // A/B/C/D/E
    val nutrition: NutritionInfo = NutritionInfo(),
    val aiAnalysis: AiAnalysis? = null,
    val cachedAt: Long = 0L
)

data class NutritionInfo(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val sugar: Double = 0.0,
    val fiber: Double = 0.0,
    val sodium: Double = 0.0
)
