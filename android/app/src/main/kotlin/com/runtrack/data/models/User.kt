package com.runtrack.data.models

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val weightKg: Float? = null,
    val heightCm: Int? = null,
    val footSize: String? = null,
    val footWidth: String? = null,
    val archType: String? = null,
    val runningStyle: String? = null,
    val budgetWon: Int? = null,
    val preferredBrands: List<String>? = null,
    val createdAt: String,
    val updatedAt: String
)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val accessToken: String, val tokenType: String, val expiresIn: Int)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val weightKg: Float? = null,
    val heightCm: Int? = null,
    val footSize: String? = null,
    val footWidth: String? = null,
    val archType: String? = null,
    val runningStyle: String? = null,
    val budgetWon: Int? = null,
    val preferredBrands: List<String>? = null
)
