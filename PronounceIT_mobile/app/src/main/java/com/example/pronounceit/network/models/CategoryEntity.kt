package com.example.pronounceit.network.models

import java.time.LocalDateTime

data class CategoryEntity(
    val categoryId: Long,
    val name: String,
    val description: String?,
    val createdBy: UserEntity,
    val createdDate: LocalDateTime,
    val active: Boolean,
    val lessons: List<LessonEntity>?
)