package com.example.pronounceit.network.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class CategoryEntity(
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("createdBy") val createdBy: UserEntity,
    @SerializedName("createdDate") val createdDate: LocalDateTime,
    @SerializedName("active") val active: Boolean,
    @SerializedName("lessons") val lessons: List<LessonEntity>? = null
)