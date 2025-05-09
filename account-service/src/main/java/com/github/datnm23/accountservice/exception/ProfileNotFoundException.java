package com.github.datnm23.accountservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileNotFoundException extends UserExeption {
    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(UUID userId) {
        super("Profile not found for user ID: " + userId);
    }
}
