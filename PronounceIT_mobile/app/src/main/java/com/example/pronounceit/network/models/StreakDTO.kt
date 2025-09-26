package com.example.pronounceit.network.models

data class StreakDTO(
    val userId: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityDate: String,
    val streakStartDate: String,
    val totalActiveDays: Int
)