package com.example.pronounceit.network.models

data class LoginResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val role: String
)