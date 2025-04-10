package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserProfileDto {
    private UUID userId;
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String address;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
}
