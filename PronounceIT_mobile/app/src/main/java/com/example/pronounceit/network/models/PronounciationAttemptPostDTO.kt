package com.example.pronounceit.network.models

data class PronounciationAttemptPostDTO(
    val wordId: Long,
    val lessonId: Long,
    val accuracy: Double,
    val isCorrect: Boolean,
    val attemptNumber: Int,
    val sessionId: String
)