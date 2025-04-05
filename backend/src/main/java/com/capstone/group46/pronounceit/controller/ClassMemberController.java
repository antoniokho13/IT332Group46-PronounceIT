package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.ClassMemberEntity;
import com.capstone.group46.pronounceit.service.ClassMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-members")
public class ClassMemberController {
    private final ClassMemberService classMemberService;

    public ClassMemberController(ClassMemberService classMemberService) {
        this.classMemberService = classMemberService;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ClassMemberEntity> getClassMemberById(@PathVariable Long memberId) {
        return classMemberService.getClassMemberById(memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ClassMemberEntity>> getAllClassMembers() {
        List<ClassMemberEntity> members = classMemberService.getAllClassMembers();
        return ResponseEntity.ok(members);
    }

    @PostMapping
    public ClassMemberEntity createClassMember(@RequestBody ClassMemberEntity classMemberEntity) {
        return classMemberService.createClassMember(classMemberEntity);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<ClassMemberEntity> updateClassMember(@PathVariable Long memberId, @RequestBody ClassMemberEntity updatedClassMember) {
        return classMemberService.updateClassMember(memberId, updatedClassMember)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteClassMember(@PathVariable Long memberId) {
        classMemberService.deleteClassMember(memberId);
        return ResponseEntity.noContent().build();
    }
}