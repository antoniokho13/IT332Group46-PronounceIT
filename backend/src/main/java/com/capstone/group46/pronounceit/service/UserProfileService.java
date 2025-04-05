package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.UserProfile;
import com.capstone.group46.pronounceit.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }

    public Optional<UserProfile> getUserProfileById(Long profileId) {
        return userProfileRepository.findById(profileId);
    }

    public UserProfile createUserProfile(UserProfile userProfile) {
        return userProfileRepository.save(userProfile);
    }

    public Optional<UserProfile> updateUserProfile(Long profileId, UserProfile updatedUserProfile) {
        return userProfileRepository.findById(profileId).map(userProfile -> {
            userProfile.setFirstName(updatedUserProfile.getFirstName());
            userProfile.setLastName(updatedUserProfile.getLastName());
            userProfile.setAvatarImage(updatedUserProfile.getAvatarImage());
            return userProfileRepository.save(userProfile);
        });
    }

    public void deleteUserProfile(Long profileId) {
        userProfileRepository.deleteById(profileId);
    }
}