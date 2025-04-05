package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.ClassEntity;
import com.capstone.group46.pronounceit.service.ClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ClassEntity> getClassById(@PathVariable Long classId) {
        return classService.getClassById(classId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ClassEntity>> getAllClasses() {
        List<ClassEntity> classes = classService.getAllClasses();
        return ResponseEntity.ok(classes);
    }

    @PostMapping
    public ClassEntity createClass(@RequestBody ClassEntity classEntity) {
        return classService.createClass(classEntity);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<ClassEntity> updateClass(@PathVariable Long classId, @RequestBody ClassEntity updatedClass) {
        return classService.updateClass(classId, updatedClass)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long classId) {
        classService.deleteClass(classId);
        return ResponseEntity.noContent().build();
    }
}