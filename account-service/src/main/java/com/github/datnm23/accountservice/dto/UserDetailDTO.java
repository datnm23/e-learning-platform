package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserDetailDTO {
    // Thông tin User
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private boolean active;
    private boolean emailVerified;
    private OffsetDateTime createdAt;

    // Thông tin UserProfile
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String language;
    private boolean emailNotifications;
    private boolean pushNotifications;
}
