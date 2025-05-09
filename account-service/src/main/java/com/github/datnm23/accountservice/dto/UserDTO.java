package com.github.datnm23.accountservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.github.datnm23.accountservice.statics.AccountStatus;

@Data
@Schema(description = "DTO chứa thông tin chi tiết của User để trả về cho client.")
public class UserDTO {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private AccountStatus status;
    private boolean active;
    private boolean emailVerified;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime lastLoginAt;

    private UserProfileDTO profile;
    private List<OAuthProviderDTO> oauthProviders;
}