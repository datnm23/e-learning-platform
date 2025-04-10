package com.github.datnm23.accountservice.service;

import com.github.datnm23.accountservice.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDto createUser(UserRegistrationDto registrationDto);

    UserDto updateUser(UserUpdateDto userUpdateDto);

    UserDetailDto getUserDetails(UUID userId);

    Optional<UserDto> getUserById(UUID userId);

    Optional<UserDto> getUserByEmail(String email);

    PageResponse<UserDto> getUsers(int page, int size);

    PageResponse<UserDto> searchUsers(String query, int page, int size);

    UserDto activeUser(UUID userId);

    UserDto deactivateUser(UUID userId);

    UserDto verifyEmail(UUID userId);

    void deleteUser(UUID userId, UUID deletedByUserId);

    void restoreUser(UUID userId);

    boolean isEmailAvailable(String email);

    List<UserDto> getAllUsers();

}
