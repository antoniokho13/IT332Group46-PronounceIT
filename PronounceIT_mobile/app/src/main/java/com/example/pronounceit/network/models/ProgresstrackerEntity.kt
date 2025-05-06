package com.example.pronounceit.network.models

data class ProgressTrackerEntity(
    val progressId: Long,
    val user: UserEntity,
    val category: CategoryEntity,
    val lesson: LessonEntity,
    val status: String,
    val lastAttemptDate: LocalDateTime?,
    val completionDate: LocalDateTime?,
    val isUnlocked: Boolean
)