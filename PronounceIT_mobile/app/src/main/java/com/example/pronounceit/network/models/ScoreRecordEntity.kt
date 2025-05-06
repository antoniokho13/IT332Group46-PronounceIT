package com.example.pronounceit.network.models

import java.time.LocalDateTime

data class ScoreRecordEntity(
    val scoreId: Long,
    val user: UserEntity,
    val lesson: LessonEntity,
    val category: CategoryEntity,
    val score: Double,
    val completionDate: LocalDateTime,
    val attemptsDuration: Long,
    val correctWords: Int,
    val incorrectWords: Int
)