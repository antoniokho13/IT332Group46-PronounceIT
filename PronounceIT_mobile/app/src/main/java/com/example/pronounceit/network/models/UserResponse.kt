package com.example.pronounceit.network.models

data class UserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

// NOTE: Backend returns accumulatedPoints as part of the user payload. Add it here so the app
// can read the current user's accumulated points without adding a separate endpoint.
data class UserResponseWithPoints(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val accumulatedPoints: Int? = 0
)