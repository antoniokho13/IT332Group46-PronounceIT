package com.example.pronounceit.network.models

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String = "STUDENT", // Default role for mobile app
    val accumulatedPoints: Int = 0 // Adding this to match backend expectation
)