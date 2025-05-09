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
@Schema(description = "Sự kiện được phát sinh khi tạo mới User")
public class UserCreatedEvent extends UserDomainEvent {

    @Schema(description = "Email của User")
    private String email;

    @Schema(description = "Tên của User")
    private String firstName;

    @Schema(description = "Họ của User")
    private String lastName;

    public UserCreatedEvent(UUID userId, String email, String firstName, String lastName) {
        super(UUID.randomUUID(), java.time.Instant.now(), userId, UserCreatedEvent.class.getSimpleName());
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}