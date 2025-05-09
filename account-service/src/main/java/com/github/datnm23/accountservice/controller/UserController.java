package com.github.datnm23.accountservice.controller;

import com.github.datnm23.accountservice.dto.UserCreateDTO;
import com.github.datnm23.accountservice.dto.UserDTO;
import com.github.datnm23.accountservice.dto.UserUpdateDTO;
import com.github.datnm23.accountservice.exception.ActionNotAllowedException;
import com.github.datnm23.accountservice.exception.UserNotFoundException;
import com.github.datnm23.accountservice.service.UserService;
import com.github.datnm23.accountservice.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing user accounts")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register a new user account")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        log.info("Received request to register user with email: {}", userCreateDTO.getEmail());
        UserDTO createdUser = userService.createUser(userCreateDTO);
        URI location = URI.create(String.format("/api/v1/users/%s", createdUser.getUserId()));
        return ResponseEntity.created(location).body(createdUser);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify user email with a token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        log.info("Received request to verify email with token starting with: {}", token.substring(0, Math.min(token.length(), 10)));
        boolean success = userService.verifyEmail(token);
        if (success) {
            return ResponseEntity.ok("Email verified successfully.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token.");
        }
    }
    
    @PostMapping("/resend-verification-email")
    @Operation(summary = "Resend verification email to the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email resend request processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., email not provided)"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Action not allowed (e.g., email already verified)")
    })
    public ResponseEntity<String> resendVerificationEmail(@RequestParam("email") String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email parameter is required.");
        }
        log.info("Received request to resend verification email for: {}", email);
        try {
            userService.resendVerificationEmail(email);
            return ResponseEntity.ok("Verification email resend request processed. Please check your inbox.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ActionNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user details by ID")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name or @userService.isOwner(#userId, authentication.principal)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        log.debug("Received request to get user by ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's account details")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UUID currentUserId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(userService.getUserById(currentUserId));
    }

    @GetMapping
    @Operation(summary = "Get all users (Paginated)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "email") Pageable pageable) {
        log.debug("Received request to get all users (Admin) with pagination: {}", pageable);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update user details")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name or @userService.isOwner(#userId, authentication.principal)")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("Received request to update user ID: {}", userId);
        return ResponseEntity.ok(userService.updateUser(userId, userUpdateDTO));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Soft delete a user by ID")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, authentication)")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        UUID actorId = SecurityUtils.getCurrentUserId().orElse(userId);
        userService.deleteUser(userId, actorId);
        return ResponseEntity.noContent().build();
    }
}