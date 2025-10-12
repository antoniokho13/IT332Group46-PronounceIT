package com.example.pronounceit.network.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class LessonEntity(
    @SerializedName("lessonId") val lessonId: Long,
    @SerializedName("category") val category: CategoryReference,  // Changed to CategoryReference to avoid circular reference
    @SerializedName("name") val name: String,
    @SerializedName("focus") val focus: String?,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("createdBy") val createdBy: UserEntity,
    @SerializedName("createdDate") val createdDate: LocalDateTime,
    @SerializedName("active") val active: Boolean,
    @SerializedName("words") val words: List<WordEntity>? = null,
    var locked: Boolean = false
)

// Simple class to hold just the category ID and name to avoid circular references
data class CategoryReference(
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("name") val name: String
)