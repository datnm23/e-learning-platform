package com.github.datnm23.accountservice.entity;

import com.github.datnm23.accountservice.statics.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, active = false, deleted_by = ? WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User {


    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "user_id", length = 36, updatable = false, nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.PENDING_VERIFICATION;

    @Column(nullable = false)
    private boolean active = false;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime lastLoginAt;

    private OffsetDateTime deletedAt;

    @Column(length = 36)
    private UUID deletedBy;
    
    @Version
    private Long version; 

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuthProvider> oauthProviders = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile profile;

    @Transient
    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    public void setUserProfile(UserProfile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setUser(null);
            }
        } else {
            profile.setUser(this);
        }
        this.profile = profile;
    }

    public void updateActiveStatus() {
        this.active = (this.status == AccountStatus.ACTIVE && this.emailVerified && this.deletedAt == null);
    }

}