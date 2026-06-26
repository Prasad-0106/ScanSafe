package com.scansafe.domain.model

data class ScanHistory(
    val id: String,
    val product: Product,
    val scannedAt: Long
)

data class Favourite(
    val id: String,
    val product: Product,
    val savedAt: Long
)
