package com.capstone.group46.pronounceit.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "achievements")
public class AchievementEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(name = "badge_image_path")
    private String badgeImagePath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;
    
    @Column(name = "trigger_value")
    private Integer triggerValue;
    
    @Column(name = "points_reward")
    private Integer pointsReward;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TriggerType {
        FIRST_CORRECT_ANSWER,
        CONSECUTIVE_CORRECT_ANSWERS,
        TOTAL_CORRECT_ANSWERS,
        DAILY_STREAK,
        WEEKLY_STREAK,
        LESSONS_COMPLETED,
        PERFECT_PRONUNCIATION_SCORE,
        TIME_SPENT_LEARNING,
        FIRST_LOGIN,
        PROFILE_COMPLETION
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public AchievementEntity() {}
    
    public AchievementEntity(String title, String description, TriggerType triggerType) {
        this.title = title;
        this.description = description;
        this.triggerType = triggerType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBadgeImagePath() {
        return badgeImagePath;
    }
    
    public void setBadgeImagePath(String badgeImagePath) {
        this.badgeImagePath = badgeImagePath;
    }
    
    public TriggerType getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
    
    public Integer getTriggerValue() {
        return triggerValue;
    }
    
    public void setTriggerValue(Integer triggerValue) {
        this.triggerValue = triggerValue;
    }
    
    public Integer getPointsReward() {
        return pointsReward;
    }
    
    public void setPointsReward(Integer pointsReward) {
        this.pointsReward = pointsReward;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
