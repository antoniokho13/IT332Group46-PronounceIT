package com.example.pronounceit.network.models

import java.time.LocalDateTime

data class PronounciationAttemptEntity(
    val attemptId: Long,
    val user: UserEntity?, // nullable
    val word: WordEntity,
    val lesson: LessonEntity,
    val timestamp: LocalDateTime?, // nullable
    val accuracy: Double,
    val isCorrect: Boolean,
    val attemptNumber: Int
)