package com.capstone.group46.pronounceit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "score_records")
public class ScoreRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private LocalDateTime completionDate;

    @Column(nullable = false)
    private long attemptsDuration;

    @Column(nullable = false)
    private int correctWords;

    @Column(nullable = false)
    private int incorrectWords;

    // Getters and Setters
    public Long getScoreId() {
        return scoreId;
    }
    public void setScoreId(Long scoreId) {
        this.scoreId = scoreId;
    }
    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }
    public LessonEntity getLesson() {
        return lesson;
    }
    public void setLesson(LessonEntity lesson) {
        this.lesson = lesson;
    }
    public CategoryEntity getCategory() {
        return category;
    }
    public void setCategory(CategoryEntity category) {
        this.category = category;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public LocalDateTime getCompletionDate() {
        return completionDate;
    }
    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }
    public long getAttemptsDuration() {
        return attemptsDuration;
    }
    public void setAttemptsDuration(long attemptsDuration) {
        this.attemptsDuration = attemptsDuration;
    }
    public int getCorrectWords() {
        return correctWords;
    }
    public void setCorrectWords(int correctWords) {
        this.correctWords = correctWords;
    }
    public int getIncorrectWords() {
        return incorrectWords;
    }
    public void setIncorrectWords(int incorrectWords) {
        this.incorrectWords = incorrectWords;
    }
}