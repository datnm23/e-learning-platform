package com.github.datnm23.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(name = "notification-service", path = "/api/v1/internal/notifications")
public interface NotificationServiceClient {
    @PostMapping("/send-verification-email")
    void sendVerificationEmail(@RequestBody VerificationEmailRequest request);

    record VerificationEmailRequest(String email, String recipientName, String verificationToken, UUID userId) {}
}
