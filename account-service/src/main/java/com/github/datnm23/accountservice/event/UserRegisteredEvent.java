package com.github.datnm23.accountservice.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Schema(description = "Sự kiện được phát sinh khi một tài khoản User mới được tạo thành công.")
public class UserRegisteredEvent extends UserDomainEvent {

    @Schema(description = "Địa chỉ email của user mới")
    private String email;

    @Schema(description = "Tên của user mới")
    private String firstName;

    @Schema(description = "Họ của user mới")
    private String lastName;

    private static final String eventType = "USER_REGISTERED";

    public UserRegisteredEvent(UUID userId, String email, String firstName, String lastName) {
        super(UUID.randomUUID(), Instant.now(), userId, UserRegisteredEvent.class.getSimpleName());
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
