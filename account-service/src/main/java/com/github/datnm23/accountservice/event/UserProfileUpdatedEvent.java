package com.github.datnm23.accountservice.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Sự kiện được phát sinh khi cập nhật thông tin User")
public class UserProfileUpdatedEvent extends UserDomainEvent {

    @Schema(description = "Các trường đã thay đổi và giá trị mới")
    private Map<String, Object> changedFields;

    public UserProfileUpdatedEvent(UUID userId, Map<String, Object> changedFields) {
        super(UUID.randomUUID(), java.time.Instant.now(), userId, UserProfileUpdatedEvent.class.getSimpleName());
        this.changedFields = changedFields;
    }
}