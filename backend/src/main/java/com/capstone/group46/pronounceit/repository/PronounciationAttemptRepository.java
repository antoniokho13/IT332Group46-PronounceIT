package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.PronounciationAttemptEntity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PronounciationAttemptRepository extends JpaRepository<PronounciationAttemptEntity, Long> {
    int countByUser_IdAndWord_WordIdAndLesson_LessonId(Long userId, Long wordId, Long lessonId);
    Optional<PronounciationAttemptEntity> findByUser_IdAndWord_WordIdAndLesson_LessonId(Long userId, Long wordId, Long lessonId);
    Optional<PronounciationAttemptEntity> findByUser_IdAndWord_WordIdAndLesson_LessonIdAndSessionId(
        Long userId, Long wordId, Long lessonId, String sessionId
    );
}