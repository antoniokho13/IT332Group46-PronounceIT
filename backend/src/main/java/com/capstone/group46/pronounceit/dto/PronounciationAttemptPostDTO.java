package com.capstone.group46.pronounceit.dto;

public class PronounciationAttemptPostDTO {
    public Long wordId;
    public Long lessonId;
    public double accuracy;
    public boolean isCorrect;
    public int attemptNumber;
    public String sessionId; // <-- Add this
}
