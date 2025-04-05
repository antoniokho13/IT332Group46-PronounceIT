package com.capstone.group46.pronounceit.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "class_members")
public class ClassMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public ClassEntity getClassEntity() {
        return classEntity;
    }
    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }
    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }
}