package com.github.datnm23.accountservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.datnm23.accountservice.entity.EmailVerificationToken;
import com.github.datnm23.accountservice.entity.User;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(User user); // Có thể dùng để kiểm tra user đã có token chưa

    // Xóa tất cả token của một user (dùng khi resend hoặc user bị xóa)
    @Modifying 
    void deleteAllByUser(User user);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
