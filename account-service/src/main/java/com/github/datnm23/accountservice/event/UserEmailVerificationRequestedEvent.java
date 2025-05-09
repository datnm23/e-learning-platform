package com.github.datnm23.accountservice.event;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Sự kiện được phát sinh khi có yêu cầu gửi email xác thực cho người dùng.")
public class UserEmailVerificationRequestedEvent extends UserDomainEvent {

    @Schema(description = "Email của người dùng cần xác thực")
    private String email;

    @Schema(description = "Tên người dùng để cá nhân hóa email")
    private String recipientName;

    @Schema(description = "Token xác thực duy nhất sẽ được gửi trong email")
    private String verificationToken;

    public UserEmailVerificationRequestedEvent(UUID userId, String email, String recipientName, String verificationToken) {
        super(UUID.randomUUID(), java.time.Instant.now(), userId, UserEmailVerificationRequestedEvent.class.getSimpleName());
        this.email = email;
        this.recipientName = recipientName;
        this.verificationToken = verificationToken;
    }
}