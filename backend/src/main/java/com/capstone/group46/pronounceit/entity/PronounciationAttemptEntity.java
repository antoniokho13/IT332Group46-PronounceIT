package com.capstone.group46.pronounceit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ADD THESE IMPORTS
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "pronounciation_attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id", "lesson_id", "sessionId"})
)
public class PronounciationAttemptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // <-- ADD THIS LINE
    private WordEntity word;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // <-- ADD THIS LINE
    private LessonEntity lesson;

    // ... (rest of the file is unchanged) ...

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private double accuracy;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(nullable = false)
    private int attemptNumber;

    @Column(nullable = false)
    private String sessionId;

    // Getters and Setters
    public Long getAttemptId() {
        return attemptId;
    }
    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }
    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }
    public WordEntity getWord() {
        return word;
    }
    public void setWord(WordEntity word) {
        this.word = word;
    }
    public LessonEntity getLesson() {
        return lesson;
    }
    public void setLesson(LessonEntity lesson) {
        this.lesson = lesson;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public double getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    public boolean isCorrect() {
        return isCorrect;
    }
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
    public int getAttemptNumber() {
        return attemptNumber;
    }
    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}