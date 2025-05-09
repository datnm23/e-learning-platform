package com.github.datnm23.accountservice.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserDomainEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID duy nhất của sự kiện")
    private UUID eventId = UUID.randomUUID();

    @Schema(description = "Thời điểm sự kiện xảy ra (UTC)")
    private Instant eventTimestamp = Instant.now();

    @Schema(description = "ID của User bị tác động bởi sự kiện")
    private UUID userId;

    @Schema(description = "Loại sự kiện (ví dụ: USER_CREATED, PROFILE_UPDATED)")
    private String eventType;
}
