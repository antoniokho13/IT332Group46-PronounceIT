package com.example.pronounceit.network.models

data class AchievementEntity(
    val id: Long,
    val title: String,
    val description: String,
    val pointsRequired: Int,
    val isActive: Boolean,
    val badgeImagePath: String?
)