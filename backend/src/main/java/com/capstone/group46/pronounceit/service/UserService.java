package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.repository.UserRepository;
import com.capstone.group46.pronounceit.entity.AchievementEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserEntity> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id){
        return userRepository.findById(id);
    }

    public UserEntity createUser(UserEntity user) {
        // ✅ Updated to allow TEACHER and STUDENT roles for new users
        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            user.setRole("STUDENT"); // Set default role if none provided
        } else if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("TEACHER") && !role.equalsIgnoreCase("STUDENT")) {
            throw new IllegalArgumentException("Invalid role. Allowed roles: ADMIN, TEACHER, STUDENT.");
        }

        // ✅ Initialize accumulated points to 0 for new users
        if (user.getAccumulatedPoints() == null) {
            user.setAccumulatedPoints(0);
        }

        // ✅ Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<UserEntity> updateUser(Long id, UserEntity updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setEmail(updatedUser.getEmail());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }

            if (updatedUser.getAccumulatedPoints() != null) {
                user.setAccumulatedPoints(updatedUser.getAccumulatedPoints());
            }

            return userRepository.save(user);
        });
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean validatePassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // ========= Accumulated Points Management ========= //
    
    public UserEntity addPoints(Long userId, Integer points) {
        return userRepository.findById(userId).map(user -> {
            Integer currentPoints = user.getAccumulatedPoints() != null ? user.getAccumulatedPoints() : 0;
            user.setAccumulatedPoints(currentPoints + points);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public UserEntity subtractPoints(Long userId, Integer points) {
        return userRepository.findById(userId).map(user -> {
            Integer currentPoints = user.getAccumulatedPoints() != null ? user.getAccumulatedPoints() : 0;
            Integer newPoints = Math.max(0, currentPoints - points); // Ensure points don't go below 0
            user.setAccumulatedPoints(newPoints);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public UserEntity setPoints(Long userId, Integer points) {
        return userRepository.findById(userId).map(user -> {
            user.setAccumulatedPoints(Math.max(0, points)); // Ensure points are not negative
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    // ========= Achievement Management ========= //

    public UserEntity unlockAchievement(Long userId, AchievementEntity achievement) {
        return userRepository.findById(userId).map(user -> {
            if (!user.getAchievements().contains(achievement)) {
                user.getAchievements().add(achievement);
                return userRepository.save(user);
            }
            return user; // Achievement already unlocked
        }).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public boolean hasAchievement(Long userId, Long achievementId) {
        return userRepository.findById(userId).map(user -> 
            user.getAchievements().stream()
                .anyMatch(achievement -> achievement.getId().equals(achievementId))
        ).orElse(false);
    }

    public List<AchievementEntity> getUserAchievements(Long userId) {
        return userRepository.findById(userId)
            .map(UserEntity::getAchievements)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
}