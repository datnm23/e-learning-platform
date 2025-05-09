package com.github.datnm23.accountservice.controller;

import com.github.datnm23.accountservice.dto.UserProfileDTO;
import com.github.datnm23.accountservice.dto.UserProfileUpdateDTO;
import com.github.datnm23.accountservice.security.YourCustomUserDetails;
import com.github.datnm23.accountservice.service.UserProfileService;
import com.github.datnm23.accountservice.service.UserService;
import com.github.datnm23.accountservice.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile Management", description = "APIs for managing user profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user profile by User ID")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, authentication)")
    public ResponseEntity<UserProfileDTO> getProfileByUserId(@PathVariable UUID userId) {
        log.debug("Request to get profile for user ID: {}", userId);
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile(authentication));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current authenticated user's profile")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateDTO dto) {
        UUID currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(userProfileService.updateProfile(currentUserId, dto, currentUserId));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user profile by User ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> updateUserProfileByUserId(
            @PathVariable UUID userId,
            @Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO,
            @Parameter(hidden = true) Authentication authentication) {

        UUID adminActorId = getUserIdFromAuthPrincipal(authentication);
        if (adminActorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Admin {} attempting to update profile for user ID: {}", adminActorId, userId);
        UserProfileDTO updatedProfile = userProfileService.updateProfile(userId, userProfileUpdateDTO, adminActorId);
        return ResponseEntity.ok(updatedProfile);
    }

    private UUID getUserIdFromAuthPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof YourCustomUserDetails) {
            return ((YourCustomUserDetails) principal).getUserId();
        } else if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                log.warn("Principal is a String but not a UUID: {}", principal);
                return null;
            }
        }
        return null;
    }
}
