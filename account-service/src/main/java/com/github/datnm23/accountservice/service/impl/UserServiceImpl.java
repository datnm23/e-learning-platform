package com.github.datnm23.accountservice.service.impl;

import com.github.datnm23.accountservice.dto.*;
import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;
import com.github.datnm23.accountservice.event.UserRegisteredEvent;
import com.github.datnm23.accountservice.exception.EmailAlreadyExistsException;
import com.github.datnm23.accountservice.exception.ResourceNotFoundException;
import com.github.datnm23.accountservice.mapper.UserDetailMapper;
import com.github.datnm23.accountservice.mapper.UserMapper;
import com.github.datnm23.accountservice.mapper.UserProfileMapper;
import com.github.datnm23.accountservice.repository.UserProfileRepository;
import com.github.datnm23.accountservice.repository.UserRepository;
import com.github.datnm23.accountservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public UserDto createUser(UserRegistrationDto registrationDto) {
        log.debug("Creating new user with email: {}", registrationDto.getEmail());

        // Check if email is already taken
        if (!isEmailAvailable(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already in use: " + registrationDto.getEmail());
        }

        // Map registration DTO to User entity
        User user = userMapper.toEntity(registrationDto);

        // Save user
        User savedUser = userRepository.save(user);
        log.debug("User created with ID: {}", savedUser.getUserId());

        // Publish event for user registration
        eventPublisher.publishEvent(new UserRegisteredEvent(
                this,
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        ));

        // Return user DTO
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto updateUser(UUID userId, UserUpdateDto userUpdateDto) {
        log.debug("Updating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Track changes for the event
        boolean nameChanged = !user.getFirstName().equals(userUpdateDto.getFirstName()) ||
                !user.getLastName().equals(userUpdateDto.getLastName());
        boolean activeStatusChanged = userUpdateDto.getIsActive() != null &&
                !user.getIsActive().equals(userUpdateDto.getIsActive());

        // Update user entity from DTO
        userMapper.updateUserFromDto(userUpdateDto, user);
        user.setUpdatedAt(LocalDateTime.now());

        // Save updated user
        User updatedUser = userRepository.save(user);
        log.debug("User updated: {}", updatedUser.getUserId());

        // Publish event for user update
        eventPublisher.publishEvent(new UserUpdatedEvent(
                this,
                updatedUser.getUserId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                activeStatusChanged,
                nameChanged
        ));

        // Return updated user DTO
        return userMapper.toDto(updatedUser);
    }



    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userDetails", key = "#userId")
    public UserDetailDto getUserDetails(UUID userId) {
        log.debug("Retrieving user details for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        UserProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> {
                    // Create default profile if not exists
                    log.debug("Profile not found, creating default for user ID: {}", userId);
                    UserProfile newProfile = profileMapper.createProfileFromUser(user);
                    return profileRepository.save(newProfile);
                });

        // Map both user and profile to detail DTO
        return detailMapper.toDetailDto(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public Optional<UserDto> getUserById(UUID userId) {
        log.debug("Retrieving user by ID: {}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByEmail", key = "#email")
    public Optional<UserDto> getUserByEmail(String email) {
        log.debug("Retrieving user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsers(int page, int size) {
        log.debug("Retrieving users page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> usersPage = userRepository.findAll(pageable);

        List<UserDto> users = userMapper.toDtoList(usersPage.getContent());

        return new PageResponse<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> searchUsers(String query, int page, int size) {
        log.debug("Searching users with query: {}, page: {}, size: {}", query, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        Page<User> usersPage = userRepository.searchByNameOrEmail(query, pageable);

        List<UserDto> users = userMapper.toDtoList(usersPage.getContent());

        return new PageResponse<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast()
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails"}, key = "#userId")
    public UserDto activateUser(UUID userId) {
        log.debug("Activating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.debug("User activated: {}", userId);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails"}, key = "#userId")
    public UserDto deactivateUser(UUID userId) {
        log.debug("Deactivating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setIsActive(false);

        User savedUser = userRepository.save(user);
        log.debug("User deactivated: {}", userId);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userDetails"}, key = "#userId")
    public UserDto verifyEmail(UUID userId) {
        log.debug("Verifying email for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setIsEmailVerified(true);

        User savedUser = userRepository.save(user);
        log.debug("Email verified for user: {}", userId);

        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userProfiles", "userDetails", "usersByEmail"}, allEntries = true)
    public void deleteUser(UUID userId, UUID deletedByUserId) {
        log.debug("Soft deleting user with ID: {} by admin ID: {}", userId, deletedByUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Soft delete user
        user.softDelete(deletedByUserId);
        userRepository.save(user);

        // Soft delete profile
        UserProfile profile = profileRepository.findById(userId).orElse(null);
        if (profile != null) {
            profile.softDelete();
            profileRepository.save(profile);
        }

        log.debug("User deleted: {}", userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userProfiles", "userDetails", "usersByEmail"}, allEntries = true)
    public void restoreUser(UUID userId) {
        log.debug("Restoring deleted user with ID: {}", userId);

        // Find user including deleted ones
        User user = userRepository.findByUserIdIncludingDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Restore user
        user.restore();
        userRepository.save(user);

        // Restore profile if exists
        profileRepository.findByUserIdIncludingDeleted(userId).ifPresent(profile -> {
            profile.restore();
            profileRepository.save(profile);
        });

        log.debug("User restored: {}", userId);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Retrieving all users");
        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }
}