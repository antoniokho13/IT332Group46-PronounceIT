package com.example.pronounceit.network.models

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String? = null,
    val oldPassword: String? = null
)