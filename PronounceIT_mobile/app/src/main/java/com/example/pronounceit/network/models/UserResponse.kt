package com.example.pronounceit.network.models

data class UserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)