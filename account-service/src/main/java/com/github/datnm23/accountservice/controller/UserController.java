package com.github.datnm23.accountservice.controller;
import com.github.datnm23.accountservice.dto.UserDetailDto;
import com.github.datnm23.accountservice.dto.UserDto;
import com.github.datnm23.accountservice.dto.UserRegistrationDto;
import com.github.datnm23.accountservice.dto.UserUpdateDto;
import com.github.datnm23.accountservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Operations for managing user accounts")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with basic information")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserDto createdUser = userService.createUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves basic user information by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/details")
    @Operation(summary = "Get user details", description = "Retrieves complete user information including profile")
    @ApiResponse(responseCode = "200", description = "User details found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDetailDto> getUserDetails(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        UserDetailDto userDetail = userService.getUserDetails(userId);
        return ResponseEntity.ok(userDetail);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves user by email address")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "Email address", required = true)
            @PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List users", description = "Retrieves paginated list of users")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<PageResponse<UserDto>> getUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserDto> users = userService.getUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Searches users by name or email")
    @ApiResponse(responseCode = "200", description = "Search results retrieved")
    public ResponseEntity<PageResponse<UserDto>> searchUsers(
            @Parameter(description = "Search query")
            @RequestParam String query,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserDto> results = userService.searchUsers(query, page, size);
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Updates basic user information")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateDto updateDto) {
        UserDto updatedUser = userService.updateUser(userId, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "Update user profile", description = "Updates user profile information")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody UserProfileUpdateDto profileUpdateDto) {
        UserProfileDto updatedProfile = userService.updateUserProfile(userId, profileUpdateDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/{userId}/activate")
    @Operation(summary = "Activate user", description = "Activates a deactivated user account")
    @ApiResponse(responseCode = "200", description = "User activated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> activateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        UserDto activatedUser = userService.activateUser(userId);
        return ResponseEntity.ok(activatedUser);
    }

    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates an active user account")
    @ApiResponse(responseCode = "200", description = "User deactivated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> deactivateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        UserDto deactivatedUser = userService.deactivateUser(userId);
        return ResponseEntity.ok(deactivatedUser);
    }

    @PutMapping("/{userId}/verify-email")
    @Operation(summary = "Verify email", description = "Marks user email as verified")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> verifyEmail(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        UserDto verifiedUser = userService.verifyEmail(userId);
        return ResponseEntity.ok(verifiedUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Soft deletes a user account")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Admin UUID performing the deletion", required = true)
            @RequestParam UUID adminId) {
        userService.deleteUser(userId, adminId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/restore")
    @Operation(summary = "Restore user", description = "Restores a previously deleted user account")
    @ApiResponse(responseCode = "204", description = "User restored successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PreAuthorize("hasRole('ADMIN'