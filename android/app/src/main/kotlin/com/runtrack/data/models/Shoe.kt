package com.runtrack.data.models

data class Shoe(
    val id: Int,
    val userId: Int,
    val brand: String,
    val model: String,
    val size: String? = null,
    val color: String? = null,
    val purchasePriceWon: Int? = null,
    val purchaseDate: String? = null,
    val notes: String? = null,
    val cumulativeKm: Float,
    val runCount: Int,
    val condition: String,
    val createdAt: String,
    val updatedAt: String
)

data class ShoeCreateRequest(
    val brand: String,
    val model: String,
    val size: String? = null,
    val color: String? = null,
    val purchasePriceWon: Int? = null,
    val purchaseDate: String? = null,
    val notes: String? = null
)

data class ShoeUpdateRequest(
    val brand: String? = null,
    val model: String? = null,
    val size: String? = null,
    val color: String? = null,
    val purchasePriceWon: Int? = null,
    val condition: String? = null,
    val notes: String? = null
)

data class ShoeStats(val cumulativeKm: Float, val runCount: Int, val condition: String)
