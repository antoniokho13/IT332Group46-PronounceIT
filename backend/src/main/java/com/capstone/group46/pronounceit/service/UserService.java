package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.repository.UserRepository;
import com.capstone.group46.pronounceit.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
        // ✅ Set default role if none provided
        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            user.setRole("USER");
        } else if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("USER")) {
            throw new IllegalArgumentException("Invalid role. Allowed roles: ADMIN, USER.");
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

            return userRepository.save(user);
        });
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean validatePassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}