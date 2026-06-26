package com.scansafe.domain.model

data class AiAnalysis(
    val healthScore: Int,             // 1-10
    val scoreLabel: String,           // Dangerous / Moderate / Healthy
    val summary: String,
    val harmfulIngredients: List<HarmfulIngredient>,
    val safeIngredients: List<String>,
    val neutralIngredients: List<String>,
    val allergensDetected: List<String>,
    val additives: List<Additive>,
    val recommendation: String
)

data class HarmfulIngredient(
    val name: String,
    val reason: String,
    val riskLevel: String  // High / Medium / Low
)

data class Additive(
    val code: String,
    val name: String,
    val risk: String,  // Safe / Moderate / Avoid
    val note: String
)
