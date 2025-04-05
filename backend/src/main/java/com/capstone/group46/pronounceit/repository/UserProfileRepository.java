package com.capstone.group46.pronounceit.repository;

import com.capstone.group46.pronounceit.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

}
