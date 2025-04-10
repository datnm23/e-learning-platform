package com.github.datnm23.accountservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
}