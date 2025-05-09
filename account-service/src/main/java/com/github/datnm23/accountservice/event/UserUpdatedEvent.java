package com.github.datnm23.accountservice.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Sự kiện được phát sinh khi thông tin cơ bản (firstName, lastName) của User được cập nhật.")
public class UserUpdatedEvent extends UserDomainEvent {

    @Schema(description = "Tên mới (nếu được cập nhật)", nullable = true)
    private String updatedFirstName;

    @Schema(description = "Họ mới (nếu được cập nhật)", nullable = true)
    private String updatedLastName;

    private static final String eventType = "USER_UPDATED";

    public UserUpdatedEvent(UUID userId, String updatedFirstName, String updatedLastName) {
        super(UUID.randomUUID(), Instant.now(), userId, UserUpdatedEvent.class.getSimpleName());
        this.updatedFirstName = updatedFirstName;
        this.updatedLastName = updatedLastName;
    }
}
