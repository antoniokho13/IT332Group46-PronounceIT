package com.example.pronounceit.network.models

data class ScoreRecordDTO(
    val lessonId: Long,
    val score: Double,
    val attemptsDuration: Long,
    val correctWords: Int,
    val incorrectWords: Int,
    val sessionId: String
)
