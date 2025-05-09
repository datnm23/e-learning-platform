package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthProviderDTO {

    private UUID userId;
    private Provider provider;
    private String externalId;
    private String email;
    private String displayName;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}
