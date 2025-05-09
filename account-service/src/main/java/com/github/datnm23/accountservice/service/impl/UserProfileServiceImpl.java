package com.github.datnm23.accountservice.service.impl;

import com.github.datnm23.accountservice.dto.UserDetailDTO;
import com.github.datnm23.accountservice.dto.UserProfileDTO;
import com.github.datnm23.accountservice.dto.UserProfileUpdateDTO;
import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;
import com.github.datnm23.accountservice.event.UserDomainEvent;
import com.github.datnm23.accountservice.event.UserProfileUpdatedEvent;
import com.github.datnm23.accountservice.exception.ProfileNotFoundException;
import com.github.datnm23.accountservice.exception.UserNotFoundException;
import com.github.datnm23.accountservice.mapper.UserProfileMapper;
import com.github.datnm23.accountservice.repository.UserProfileRepository;
import com.github.datnm23.accountservice.repository.UserRepository;
import com.github.datnm23.accountservice.security.SecurityUtils;
import com.github.datnm23.accountservice.security.YourCustomUserDetails;
import com.github.datnm23.accountservice.service.UserProfileService;
import com.github.datnm23.accountservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.github.datnm23.accountservice.config.AppConstants;

@Service("userProfileService")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final KafkaTemplate<String, UserDomainEvent> kafkaTemplate;
    private final UserService userService;

    @Value("${kafka.topic.user-events:" + AppConstants.KAFKA_TOPIC_USER_EVENTS + "}")
    private String userEventsTopic;

    @Override
    @Cacheable(value = AppConstants.CACHE_PROFILE_BY_USER_ID, key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public UserProfileDTO getProfileByUserId(UUID userId) {
        log.debug("Fetching profile for user ID: {}", userId);
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Profile not found for user ID: {}", userId);
                    return new ProfileNotFoundException(userId);
                });
        return userProfileMapper.toUserProfileDTO(userProfile);
    }

    @Override
    @CachePut(value = AppConstants.CACHE_PROFILE_BY_USER_ID, key = "#userIdToUpdate")
    public UserProfileDTO updateProfile(UUID userIdToUpdate, UserProfileUpdateDTO dto, UUID actorId) {
        log.info("Attempting to update profile for user ID: {} by actor ID: {}", userIdToUpdate, actorId);

        User userOfProfile = userRepository.findById(userIdToUpdate)
                .orElseThrow(() -> new UserNotFoundException(userIdToUpdate));

        // Permission checking is mainly handled by @PreAuthorize at controller level
        // actorId is used primarily for audit logging

        UserProfile userProfile = userProfileRepository.findByUserId(userIdToUpdate)
                .orElseGet(() -> {
                    log.info("Profile not found for user {}, creating a new one.", userIdToUpdate);
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(userOfProfile);
                    userOfProfile.setUserProfile(newProfile);
                    return newProfile;
                });

        Map<String, Object> changedFields = new HashMap<>();
        if (dto.getAvatarUrl() != null && !Objects.equals(dto.getAvatarUrl(), userProfile.getAvatarUrl())) {
            changedFields.put("avatarUrl", dto.getAvatarUrl());
        }
        if (dto.getBio() != null && !Objects.equals(dto.getBio(), userProfile.getBio())) {
            changedFields.put("bio", dto.getBio());
        }
        if (dto.getPhone() != null && !Objects.equals(dto.getPhone(), userProfile.getPhone())) {
            changedFields.put("phone", dto.getPhone());
        }
        if (dto.getEmailNotifications() != null && 
                !Objects.equals(dto.getEmailNotifications(), userProfile.isEmailNotifications())) {
            changedFields.put("emailNotifications", dto.getEmailNotifications());
        }

        userProfileMapper.updateUserProfileFromDto(dto, userProfile);
        UserProfile savedProfile = userProfileRepository.save(userProfile);

        log.info("Profile for user ID: {} updated by actor ID: {}", userIdToUpdate, actorId);

        if (!changedFields.isEmpty()) {
            publishUserProfileEvent(new UserProfileUpdatedEvent(userIdToUpdate, changedFields));
        }

        return userProfileMapper.toUserProfileDTO(savedProfile);
    }

    @Override
    public UserProfileDTO getCurrentUserProfile(Authentication authentication) {
        UUID currentUserId = getUserIdFromAuthPrincipal(authentication);
        if (currentUserId == null) {
            currentUserId = SecurityUtils.getRequiredCurrentUserId();
        }
        return getProfileByUserId(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailDTO getUserDetails(UUID userId) {
        log.debug("Fetching user details for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);
                
        return userProfileMapper.toUserDetailDTO(user, profile);
    }

    private void publishUserProfileEvent(UserDomainEvent event) {
        try {
            log.info("Publishing {} to Kafka topic {}: {}", event.getEventType(), userEventsTopic, event);
            kafkaTemplate.send(userEventsTopic, event.getUserId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish profile event {} to Kafka topic {}: {}", 
                event.getEventType(), userEventsTopic, e.getMessage(), e);
        }
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
                log.warn("Principal is a String but not a UUID in getUserIdFromAuthPrincipal: {}", principal);
                return null;
            }
        }
        log.warn("Could not extract UUID from principal of type: {}", 
            principal != null ? principal.getClass().getName() : "null");
        return null;
    }
}

