package com.example.pronounceit.network.models

data class AchievementEntity(
    val id: Long,
    val title: String,
    val description: String,
    val triggerType: String,
    val triggerValue: Int?,
    val pointsReward: Int?,
    val isActive: Boolean,
    val badgeImagePath: String?
)