package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.dto.LoginRequest;
import com.capstone.group46.pronounceit.dto.LoginResponse;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.security.TokenBlacklist;
import com.capstone.group46.pronounceit.service.AuthService;
import com.capstone.group46.pronounceit.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(AuthService authService, UserService userService, TokenBlacklist tokenBlacklist) {
        this.authService = authService;
        this.userService = userService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) {
        UserEntity newUser = userService.createUser(user);
        return ResponseEntity.ok(newUser);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.addToken(token); // Blacklist the token
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
