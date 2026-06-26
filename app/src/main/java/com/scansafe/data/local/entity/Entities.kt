package com.scansafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val barcode: String,
    val id: String = "",
    val name: String,
    val brand: String = "",
    val quantity: String = "",
    val imageUrl: String = "",
    val ingredients: String = "",
    val nutriScore: String = "",
    // Nutrition (stored flat)
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val sugar: Double = 0.0,
    val fiber: Double = 0.0,
    val sodium: Double = 0.0,
    // AI Analysis stored as JSON string
    val aiAnalysisJson: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey
    val id: String,
    val barcode: String,
    val scannedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey
    val id: String,
    val barcode: String,
    val savedAt: Long = System.currentTimeMillis()
)
