package com.github.datnm23.accountservice.event.listener;

import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;
import com.github.datnm23.accountservice.event.UserRegisteredEvent;
import com.github.datnm23.accountservice.mapper.UserProfileMapper;
import com.github.datnm23.accountservice.repository.UserProfileRepository;
import com.github.datnm23.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserProfileCreationListener {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserProfileMapper profileMapper;

    @EventListener
    @Transactional
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.debug("Creating profile for newly registered user: {}", event.getUserId());

        // Get user reference
        User user = userRepository.getReferenceById(event.getUserId());

        // Create profile
        UserProfile profile = profileMapper.createProfileFromUser(user);

        // Save profile
        UserProfile savedProfile = profileRepository.save(profile);

        log.debug("User profile created for user ID: {}", event.getUserId());
    }
}
