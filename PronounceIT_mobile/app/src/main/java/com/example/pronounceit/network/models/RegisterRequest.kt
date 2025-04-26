package com.example.pronounceit.network.models

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String = "USER" // Default role
)