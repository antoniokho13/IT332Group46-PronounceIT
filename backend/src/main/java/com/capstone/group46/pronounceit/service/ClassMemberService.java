package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.ClassMemberEntity;
import com.capstone.group46.pronounceit.repository.ClassMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassMemberService {
    private final ClassMemberRepository classMemberRepository;

    public ClassMemberService(ClassMemberRepository classMemberRepository) {
        this.classMemberRepository = classMemberRepository;
    }

    public List<ClassMemberEntity> getAllClassMembers() {
        return classMemberRepository.findAll();
    }

    public Optional<ClassMemberEntity> getClassMemberById(Long memberId) {
        return classMemberRepository.findById(memberId);
    }

    public ClassMemberEntity createClassMember(ClassMemberEntity classMemberEntity) {
        return classMemberRepository.save(classMemberEntity);
    }

    public Optional<ClassMemberEntity> updateClassMember(Long memberId, ClassMemberEntity updatedClassMember) {
        return classMemberRepository.findById(memberId).map(classMemberEntity -> {
            classMemberEntity.setClassEntity(updatedClassMember.getClassEntity());
            classMemberEntity.setUser(updatedClassMember.getUser());
            return classMemberRepository.save(classMemberEntity);
        });
    }

    public void deleteClassMember(Long memberId) {
        classMemberRepository.deleteById(memberId);
    }
}