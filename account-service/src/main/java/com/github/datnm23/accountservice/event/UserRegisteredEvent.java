package com.github.datnm23.accountservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;
    private final String firstName;
    private final String lastName;

    public UserRegisteredEvent(Object source, UUID userId, String email, String firstName, String lastName) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
