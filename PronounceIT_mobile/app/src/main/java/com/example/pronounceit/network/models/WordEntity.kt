package com.example.pronounceit.network.models

import java.time.LocalDateTime

data class WordEntity(
    val wordId: Long,
    val lesson: LessonEntity,
    val word: String,
    val imageURL: String?,
    val audioURL: String?,
    val createdBy: UserEntity,
    val createdDate: LocalDateTime
)