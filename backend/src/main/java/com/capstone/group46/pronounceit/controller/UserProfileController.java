package com.capstone.group46.pronounceit.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.group46.pronounceit.entity.UserProfile;
import com.capstone.group46.pronounceit.service.UserProfileService;

@RestController
@RequestMapping("/api/user-profiles")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN') or #profileId == principal.id")
    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable Long profileId) {
        return userProfileService.getUserProfileById(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        List<UserProfile> userProfiles = userProfileService.getAllUserProfiles();
        return ResponseEntity.ok(userProfiles);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #userProfile.user.id == principal.id")
    public UserProfile createUserProfile(@RequestBody UserProfile userProfile) {
        return userProfileService.createUserProfile(userProfile);
    }

    @PutMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN') or #profileId == principal.id")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable Long profileId, @RequestBody UserProfile updatedUserProfile) {
        return userProfileService.updateUserProfile(profileId, updatedUserProfile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN') or #profileId == principal.id")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable Long profileId) {
        userProfileService.deleteUserProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}