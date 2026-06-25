package com.example.myapplication.data.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nome: String,
    val email: String,
    val password: String,
    val role: String
)

data class AuthResponse(
    val token: String
)