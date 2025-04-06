package com.capstone.group46.pronounceit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "words")
public class WordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wordId;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @Column(nullable = false)
    private String word;

    @Column
    private String imageURL;

    @Column
    private String audioURL;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    // Getters and Setters
    public Long getWordId() {
        return wordId;
    }
    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }
    public LessonEntity getLesson() {
        return lesson;
    }
    public void setLesson(LessonEntity lesson) {
        this.lesson = lesson;
    }
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }
    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public String getAudioURL() {
        return audioURL;
    }
    public void setAudioURL(String audioURL) {
        this.audioURL = audioURL;
    }
    public UserEntity getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}