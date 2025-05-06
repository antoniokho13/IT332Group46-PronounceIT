package com.example.pronounceit.network.models

data class ClassMemberEntity(
    val memberId: Long,
    val classEntity: ClassEntity,
    val user: UserEntity
)