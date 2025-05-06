package com.example.pronounceit.network.models

import java.time.LocalDateTime

data class ClassEntity(
    val classId: Long,
    val name: String,
    val description: String?,
    val createdBy: UserEntity,
    val createdDate: LocalDateTime,
    val members: List<ClassMemberEntity>?
)