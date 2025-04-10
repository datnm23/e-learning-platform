package com.github.datnm23.accountservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserUpdatedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean activeStatusChanged;

    public UserUpdatedEvent(Object source, UUID userId, String email, String firstName, String lastName, boolean activeStatusChanged) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.activeStatusChanged = activeStatusChanged;
    }
}
