package com.github.datnm23.accountservice.service;

import com.github.datnm23.accountservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);

    UserDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO);

    UserDetailDTO getUserDetails(UUID userId);

    UserDTO getUserById(UUID userId);

    UserDTO getUserByEmail(String email);

    PageResponse<UserDTO> getUsers(int page, int size);

    PageResponse<UserDTO> searchUsers(String query, int page, int size);

    UserDTO activeUser(UUID userId);

    UserDTO deactivateUser(UUID userId);

    UserDTO verifyEmail(UUID userId);

    void deleteUser(UUID userId, UUID deletedBy);

    void restoreUser(UUID userId);

    boolean isEmailAvailable(String email);

    List<UserDTO> getAllUsers();

    Page<UserDTO> getAllUsers(Pageable pageable);

    boolean verifyEmail(String token);

    void resendVerificationEmail(String email);

    boolean isOwner(UUID targetUserId, Object principal);
}
