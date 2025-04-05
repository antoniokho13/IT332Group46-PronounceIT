package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public List<LessonEntity> getAllLessons() {
        return lessonRepository.findAll();
    }

    public Optional<LessonEntity> getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }

    public LessonEntity createLesson(LessonEntity lesson) {
        return lessonRepository.save(lesson);
    }

    public Optional<LessonEntity> updateLesson(Long lessonId, LessonEntity updatedLesson) {
        return lessonRepository.findById(lessonId).map(lesson -> {
            lesson.setName(updatedLesson.getName());
            lesson.setFocus(updatedLesson.getFocus());
            lesson.setSequence(updatedLesson.getSequence());
            lesson.setActive(updatedLesson.isActive());
            return lessonRepository.save(lesson);
        });
    }

    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }
}