package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.UserProfile;
import com.capstone.group46.pronounceit.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-profiles")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<UserProfile> getUserProfileById(@PathVariable Long profileId) {
        return userProfileService.getUserProfileById(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        List<UserProfile> userProfiles = userProfileService.getAllUserProfiles();
        return ResponseEntity.ok(userProfiles);
    }

    @PostMapping
    public UserProfile createUserProfile(@RequestBody UserProfile userProfile) {
        return userProfileService.createUserProfile(userProfile);
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<UserProfile> updateUserProfile(@PathVariable Long profileId, @RequestBody UserProfile updatedUserProfile) {
        return userProfileService.updateUserProfile(profileId, updatedUserProfile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable Long profileId) {
        userProfileService.deleteUserProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}