package com.scansafe.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary Palette: Fresh Produce ─────────────────────────
val PrimaryGreenLight = Color(0xFF1FA365)
val PrimaryGreenDark = Color(0xFF3FD18B)

// ─── Neutral Backgrounds ────────────────────────────────────
val BackgroundLight = Color(0xFFFAFAF7)
val BackgroundDark = Color(0xFF0F1311) // Solid dark bg (no gradients)

// ─── Layered Surfaces (Elevation Tints) ──────────────────────
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF171B19) // Layered dark surface

val SurfaceVariantLight = Color(0xFFF3F3EF)
val SurfaceVariantDark = Color(0xFF212623)

// ─── Semantic Health-Score Scale ────────────────────────────
object HealthScoreScale {
    val A = Color(0xFF16A34A)
    val B = Color(0xFF65A30D)
    val C = Color(0xFFF59E0B)
    val D = Color(0xFFEA580C)
    val E = Color(0xFFDC2626)

    fun getColorForGrade(grade: String): Color {
        return when (grade.uppercase()) {
            "A" -> A
            "B" -> B
            "C" -> C
            "D" -> D
            "E" -> E
            else -> C // Default / Neutral
        }
    }

    fun getColorForScore(score: Int): Color {
        return when {
            score >= 80 -> A
            score >= 60 -> B
            score >= 40 -> C
            score >= 20 -> D
            else -> E
        }
    }
}

// ─── AI Feature Accent (Soft Violet-Teal Accent) ─────────────
val AiViolet = Color(0xFF8B5CF6)
val AiTeal = Color(0xFF14B8A6)

// ─── Outlines & Borders ─────────────────────────────────────
val OutlineLight = Color(0xFFE2E4E0)
val OutlineDark = Color(0xFF2B312E)

// ─── Text Colors ────────────────────────────────────────────
val TextPrimaryLight = Color(0xFF161917)
val TextPrimaryDark = Color(0xFFE3E5E3)
val TextSecondaryLight = Color(0xFF5C625E)
val TextSecondaryDark = Color(0xFF9CA29E)

// ─── Compatibility / Legacy Support ────────────────────────
// Keeping original names mapped to new theme to ensure no compilation errors on screens
val HealthGreen = PrimaryGreenLight
val HealthGreenDark = PrimaryGreenLight
val HealthGreenContainer = SurfaceVariantLight

val WarnOrange = HealthScoreScale.C
val WarnOrangeDark = HealthScoreScale.D
val WarnOrangeContainer = SurfaceVariantLight

val DangerRed = HealthScoreScale.E
val DangerRedDark = HealthScoreScale.E
val DangerRedContainer = SurfaceVariantLight

val OnHealthGreen = Color(0xFFFFFFFF)
val OnDangerRed = Color(0xFFFFFFFF)
val OnWarnOrange = Color(0xFFFFFFFF)

val NutriA = Color(0xFF038141)
val NutriB = Color(0xFF85BB2F)
val NutriC = Color(0xFFFECC00)
val NutriD = Color(0xFFEE8100)
val NutriE = Color(0xFFE63312)
