package com.github.datnm23.accountservice.entity;

import com.github.datnm23.accountservice.statics.Provider;
import lombok.Data;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth_providers",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_ext",
                columnNames = {"provider", "external_id"}),
        indexes = {
                @Index(name = "idx_oauth_user", columnList = "user_id"),
                @Index(name = "idx_oauth_provider_ext", columnList = "provider, external_id")
        })
@SQLRestriction("deleted_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthProvider {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, updatable = false, nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oauth_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20, nullable = false)
    private Provider provider;

    @Column(name = "external_id", length = 128, nullable = false)
    private String externalId;

    @Column(length = 255)
    private String email;

    @Column(length = 100)
    private String displayName;

    @Column(length = 1000)
    private String accessToken;

    @Column(length = 1000)
    private String refreshToken;

    private OffsetDateTime tokenExpiresAt;

    @CreatedDate
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
