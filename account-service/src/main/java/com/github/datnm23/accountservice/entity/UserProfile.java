package com.github.datnm23.accountservice.entity;

import com.github.datnm23.accountservice.statics.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles",
        indexes = @Index(name = "idx_profile_lang", columnList = "language"))
@SQLRestriction("deleted_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 512)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 20)
    private String phone;

    @Column
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(length = 10)
    private String language;

    @Column(nullable = false)
    private boolean emailNotifications = true;

    @Column(nullable = false)
    private boolean pushNotifications = true;

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