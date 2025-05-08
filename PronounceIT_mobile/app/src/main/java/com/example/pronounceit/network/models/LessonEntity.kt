package com.example.pronounceit.network.models

import java.time.LocalDateTime

data class LessonEntity(
    val lessonId: Long,
    val category: CategoryEntity,
    val name: String,
    val focus: String?,
    val sequence: Int,
    val createdBy: UserEntity,
    val createdDate: LocalDateTime,
    val active: Boolean,
    val words: List<WordEntity>?
)