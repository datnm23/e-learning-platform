package com.github.datnm23.accountservice.service;

import com.github.datnm23.accountservice.dto.UserDetailDTO;
import com.github.datnm23.accountservice.dto.UserProfileDTO;
import com.github.datnm23.accountservice.dto.UserProfileUpdateDTO;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface UserProfileService {
    /**
     * Get user profile by user ID
     */
    UserProfileDTO getProfileByUserId(UUID userId);

    /**
     * Update a user profile
     */
    UserProfileDTO updateProfile(UUID userId, UserProfileUpdateDTO updateDto, UUID actorId);

    /**
     * Get profile of currently authenticated user
     */
    UserProfileDTO getCurrentUserProfile(Authentication principal);
    
    /**
     * Get detailed user information
     */
    UserDetailDTO getUserDetails(UUID userId);
}
