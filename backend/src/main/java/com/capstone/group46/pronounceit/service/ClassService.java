package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.ClassEntity;
import com.capstone.group46.pronounceit.repository.ClassRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassService {
    private final ClassRepository classRepository;

    public ClassService(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<ClassEntity> getAllClasses() {
        return classRepository.findAll();
    }

    public Optional<ClassEntity> getClassById(Long classId) {
        return classRepository.findById(classId);
    }

    public ClassEntity createClass(ClassEntity classEntity) {
        return classRepository.save(classEntity);
    }

    public Optional<ClassEntity> updateClass(Long classId, ClassEntity updatedClass) {
        return classRepository.findById(classId).map(classEntity -> {
            classEntity.setName(updatedClass.getName());
            classEntity.setDescription(updatedClass.getDescription());
            return classRepository.save(classEntity);
        });
    }

    public void deleteClass(Long classId) {
        classRepository.deleteById(classId);
    }
}