package com.github.datnm23.accountservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens", indexes = {
    @Index(name = "idx_evt_token", columnList = "token", unique = true)
})
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100) // Tăng độ dài nếu token phức tạp hơn
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private OffsetDateTime expiryDate;

    @CreatedDate // Thời điểm token được tạo
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER) // EAGER để dễ lấy user khi có token
    @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "user_id")
    private User user;

    public EmailVerificationToken(String token, OffsetDateTime expiryDate, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }
}
