package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDetailDto {
    // Thông tin User
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;

    // Thông tin UserProfile
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String address;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
}
