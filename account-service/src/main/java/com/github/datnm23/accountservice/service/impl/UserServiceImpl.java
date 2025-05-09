package com.github.datnm23.accountservice.service.impl;

import com.github.datnm23.accountservice.client.NotificationServiceClient;
import com.github.datnm23.accountservice.client.SecurityServiceClient;
import com.github.datnm23.accountservice.dto.UserCreateDTO; 
import com.github.datnm23.accountservice.dto.UserDTO;
import com.github.datnm23.accountservice.dto.UserUpdateDTO; 
import com.github.datnm23.accountservice.entity.User; 
import com.github.datnm23.accountservice.entity.UserProfile;
import com.github.datnm23.accountservice.entity.EmailVerificationToken;
import com.github.datnm23.accountservice.statics.AccountStatus; 
import com.github.datnm23.accountservice.event.UserProfileUpdatedEvent;
import com.github.datnm23.accountservice.event.UserCreatedEvent;
import com.github.datnm23.accountservice.event.UserDomainEvent;
import com.github.datnm23.accountservice.event.UserEmailVerificationRequestedEvent; 
import com.github.datnm23.accountservice.exception.DuplicateResourceException;
import com.github.datnm23.accountservice.exception.UserNotFoundException; 
import com.github.datnm23.accountservice.exception.ActionNotAllowedException;
import com.github.datnm23.accountservice.mapper.UserMapper; 
import com.github.datnm23.accountservice.repository.UserRepository;
import com.github.datnm23.accountservice.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.Map;  
import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import com.github.datnm23.accountservice.service.UserService;
import com.github.datnm23.accountservice.security.SecurityUtils;
import com.github.datnm23.accountservice.dto.PageResponse;
import com.github.datnm23.accountservice.dto.UserDetailDTO;
import com.github.datnm23.accountservice.config.AppConstants;

@Service("userService") // Đặt tên bean là "userService"
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService { // Đổi tên interface

    private final UserRepository userRepository; // Đổi tên repository
    private final UserMapper userMapper;         // Đổi tên mapper
    private final KafkaTemplate<String, UserDomainEvent> kafkaTemplate;
    private final SecurityServiceClient securityServiceClient; // Inject client
    private final NotificationServiceClient notificationServiceClient; // Inject client
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${kafka.topic.user-events:" + AppConstants.KAFKA_TOPIC_USER_EVENTS + "}")
    private String userEventsTopic;

    @Value("${app.email.verification.token.expiration-minutes:1440}")
    private long tokenExpirationMinutes;

    @Cacheable(value = AppConstants.CACHE_USER_BY_ID, key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    @Override
    public UserDTO getUserById(UUID userId) {
        log.debug("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException(userId);
                });
        return userMapper.toUserDTO(user);
    }

    @Override
    @Cacheable(value = AppConstants.CACHE_USER_BY_EMAIL, key = "#email.toLowerCase()", unless = "#result == null")
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException(email);
                });
        return userMapper.toUserDTO(user);
    }

    @Override
    @CachePut(value = AppConstants.CACHE_USER_BY_ID, key = "#userId") // Cập nhật cache userCache
    @CacheEvict(value = AppConstants.CACHE_USER_BY_EMAIL, key = "#result.email.toLowerCase()", condition = "#result != null && #result.email != null") // Vô hiệu hóa cache userEmailCache nếu email thay đổi (hiếm khi)
    public UserDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO) {
        log.info("Attempting to update user with ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Update failed: User not found with ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        Map<String, Object> changedFields = new HashMap<>();
        if (userUpdateDTO.getFirstName() != null && !userUpdateDTO.getFirstName().equals(existingUser.getFirstName())) {
            changedFields.put("firstName", userUpdateDTO.getFirstName());
        }
        if (userUpdateDTO.getLastName() != null && !userUpdateDTO.getLastName().equals(existingUser.getLastName())) {
            changedFields.put("lastName", userUpdateDTO.getLastName());
        }

        userMapper.updateUserFromDto(userUpdateDTO, existingUser);
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully for ID: {}", userId);

        if (!changedFields.isEmpty()) {
            publishUserEvent(new UserProfileUpdatedEvent(updatedUser.getUserId(), changedFields));
        }
        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    @CacheEvict(value = AppConstants.CACHE_USER_BY_ID, key = "#userId")
    // Cần vô hiệu hóa cả userEmailCache nếu email có thể dùng để tìm user đó
    // @CacheEvict(value = AppConstants.CACHE_USER_BY_EMAIL, key = "TBD: How to get email before delete?", allEntries = false)
    // Tốt nhất là để service consumer của event UserDeletedEvent tự xử lý cache của nó
    public void deleteUser(UUID userId, UUID actorId) {
        // If actorId not provided, use current user
        UUID effectiveActorId = actorId != null ? actorId : SecurityUtils.getRequiredCurrentUserId();
        
        // Check permissions
        if (!userId.equals(effectiveActorId) && !SecurityUtils.hasAdminRole()) {
            throw new ActionNotAllowedException("Not authorized to delete this user");
        }
        
        log.info("Attempting to soft delete user with ID: {} by user: {}", userId, effectiveActorId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Deletion failed: User not found with ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        // Thực hiện soft delete
        user.setDeletedAt(java.time.OffsetDateTime.now());
        user.setDeletedBy(effectiveActorId); // Gán người xóa
        user.setStatus(AccountStatus.CLOSED); // Hoặc một trạng thái "DELETED" nếu có
        user.updateActiveStatus(); // active sẽ là false
        userRepository.save(user); // Lưu thay đổi

        // Ghi log đã soft delete (thay vì dùng @SQLDelete)
        log.info("User {} soft deleted successfully by user {}", userId, effectiveActorId);

        // Publish UserDeletedEvent (hoặc UserSoftDeletedEvent)
        // publishUserEvent(new UserDeletedEvent(userId, deletedBy));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        Page<User> userPage = userRepository.findAll(pageable); // Query đã tự lọc deleted_at IS NULL nhờ @Where
        return userPage.map(userMapper::toUserDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return getAllUsers(Pageable.unpaged()).getContent();
    }

    @Override
    @Transactional
    public boolean verifyEmail(String tokenValue) {
        log.debug("Attempting to verify email with token: {}", tokenValue);
        Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenRepository.findByToken(tokenValue);

        if (tokenOpt.isEmpty()) {
            log.warn("Email verification failed: Token not found.");
            return false;
        }

        EmailVerificationToken token = tokenOpt.get();

        if (token.getExpiryDate().isBefore(OffsetDateTime.now())) {
            log.warn("Email verification failed: Token for user {} has expired.", token.getUser().getUserId());
            emailVerificationTokenRepository.delete(token);
            return false;
        }

        User user = token.getUser();
        if (user == null) {
            log.error("Email verification failed: Token {} is not associated with any user.", tokenValue);
            emailVerificationTokenRepository.delete(token);
            return false;
        }

        if (user.isEmailVerified()) {
            log.info("Email for user {} is already verified.", user.getUserId());
            emailVerificationTokenRepository.delete(token);
            return true;
        }

        user.setEmailVerified(true);
        if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
            user.setStatus(AccountStatus.ACTIVE);
        }
        user.updateActiveStatus();
        userRepository.save(user);
        emailVerificationTokenRepository.delete(token);

        log.info("Email for user {} verified successfully. Status set to {}.", user.getUserId(), user.getStatus());
        return true;
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resend verification email requested for: {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.isEmailVerified()) {
            log.warn("Cannot resend verification email: Email for user {} is already verified.", user.getUserId());
            throw new ActionNotAllowedException("Email is already verified.");
        }

        emailVerificationTokenRepository.deleteAllByUser(user);

        String verificationTokenValue = generateAndSaveVerificationToken(user);
        publishUserEvent(new UserEmailVerificationRequestedEvent(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                verificationTokenValue
        ));
        
        log.info("New verification email request processed for user {}.", user.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(UUID targetUserId, Object principalObject) {
        return targetUserId != null && 
               SecurityUtils.getCurrentUserId()
                   .map(id -> id.equals(targetUserId))
                   .orElse(false);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public UserDTO verifyEmail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setEmailVerified(true);
        if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
            user.setStatus(AccountStatus.ACTIVE);
        }
        user.updateActiveStatus();
        userRepository.save(user);
        
        return userMapper.toUserDTO(user);
    }

    @Override
    public PageResponse<UserDTO> searchUsers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchUsers(query, pageable);
        
        List<UserDTO> users = userPage.getContent().stream()
            .map(userMapper::toUserDTO)
            .collect(Collectors.toList());
            
        return new PageResponse<UserDTO>(users, 
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.isFirst(),
            userPage.isLast());
    }

    @Override
    public UserDTO activeUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setStatus(AccountStatus.ACTIVE);
        user.updateActiveStatus();
        userRepository.save(user);
        
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.setStatus(AccountStatus.INACTIVE);
        user.updateActiveStatus();
        userRepository.save(user);
        
        return userMapper.toUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailDTO getUserDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        UserProfile profile = user.getProfile();
        
        return userMapper.toUserDetailDTO(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserDTO> users = userPage.getContent().stream()
            .map(userMapper::toUserDTO)
            .collect(Collectors.toList());
        
        return new PageResponse<UserDTO>(users, 
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.isFirst(),
            userPage.isLast());
    }

    @Override
    @Transactional
    public void restoreUser(UUID userId) {
        // Implement to restore a soft-deleted user
        User user = userRepository.findIncludingDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (user.getDeletedAt() == null) {
            throw new ActionNotAllowedException("User is not deleted");
        }
        
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        user.setStatus(AccountStatus.ACTIVE);
        user.updateActiveStatus();
        userRepository.save(user);
    }

    @Override
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Attempting to create user for email: {}", userCreateDTO.getEmail());

        if (userRepository.existsByEmailIgnoreCase(userCreateDTO.getEmail())) {
            log.warn("User creation failed: Email {} already exists", userCreateDTO.getEmail());
            throw new DuplicateResourceException("Email address already in use: " + userCreateDTO.getEmail());
        }

        User user = userMapper.toUser(userCreateDTO);
        
        // Hash password
        SecurityServiceClient.HashResponse hashResponse = securityServiceClient.hashPassword(
                new SecurityServiceClient.HashRequest(userCreateDTO.getPassword())
        );
        user.setPasswordHash(hashResponse.hashedPassword());

        user.setStatus(AccountStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.updateActiveStatus();

        // Create UserProfile
        UserProfile profile = new UserProfile();
        user.setUserProfile(profile);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getUserId());

        // Publish event
        publishUserEvent(new UserCreatedEvent(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        ));

        // Send verification email
        String verificationTokenValue = generateAndSaveVerificationToken(savedUser);
        publishUserEvent(new UserEmailVerificationRequestedEvent(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                verificationTokenValue
        ));

        return userMapper.toUserDTO(savedUser);
    }

    // --- Private Helper Methods ---
    private void publishUserEvent(UserDomainEvent event) {
        try {
            log.info("Publishing {} to Kafka topic {}: {}", event.getEventType(), userEventsTopic, event);
            kafkaTemplate.send(userEventsTopic, event.getUserId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish event {} to Kafka topic {}: {}", event.getEventType(), userEventsTopic, e.getMessage(), e);
        }
    }

    private String generateAndSaveVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                tokenValue,
                OffsetDateTime.now().plusMinutes(tokenExpirationMinutes),
                user
        );
        emailVerificationTokenRepository.save(verificationToken);
        log.debug("Generated and saved new verification token for user {}", user.getUserId());
        return tokenValue;
    }

    private UUID getUserIdFromPrincipalObject(Object principal) {
        return SecurityUtils.getUserIdFromPrincipalObject(principal).orElse(null);
    }
}