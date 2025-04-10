package com.github.datnm23.accountservice.service;

import com.github.datnm23.accountservice.dto.UserDetailDto;
import com.github.datnm23.accountservice.dto.UserProfileDto;
import com.github.datnm23.accountservice.dto.UserProfileUpdateDto;

import java.util.UUID;

public interface UserProfileService {
    UserProfileDto getProfileById(UUID userId);
    UserProfileDto updateProfile(UUID userId, UserProfileUpdateDto updateDto);
    UserDetailDto getUserDetails(UUID userId);
}
