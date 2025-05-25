package com.capstone.group46.pronounceit.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.capstone.group46.pronounceit.entity.PronounciationAttemptEntity;

@Repository
public interface PronounciationAttemptRepository extends JpaRepository<PronounciationAttemptEntity, Long> {
    int countByUser_IdAndWord_WordIdAndLesson_LessonId(Long userId, Long wordId, Long lessonId);
    Optional<PronounciationAttemptEntity> findByUser_IdAndWord_WordIdAndLesson_LessonId(Long userId, Long wordId, Long lessonId);
    Optional<PronounciationAttemptEntity> findByUser_IdAndWord_WordIdAndLesson_LessonIdAndSessionId(
        Long userId, Long wordId, Long lessonId, String sessionId
    );
    List<PronounciationAttemptEntity> findAllByUser_IdAndLesson_LessonIdAndSessionId(Long userId, Long lessonId, String sessionId);

    @Query("SELECT pa.word.wordId AS wordId, pa.word.word AS word, " +
           "AVG(pa.accuracy) AS avgAccuracy, " +
           "AVG(pa.attemptNumber) AS avgAttempts, " +
           "SUM(CASE WHEN pa.isCorrect = true THEN 1 ELSE 0 END) * 1.0 / COUNT(pa) * 100 AS avgCorrectlyPronounced " +
           "FROM PronounciationAttemptEntity pa " +
           "WHERE pa.lesson.lessonId = :lessonId " +
           "GROUP BY pa.word.wordId, pa.word.word")
    List<Map<String, Object>> getWordStatisticsByLessonId(@Param("lessonId") Long lessonId);
}