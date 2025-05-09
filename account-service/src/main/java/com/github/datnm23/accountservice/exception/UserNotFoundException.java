package com.github.datnm23.accountservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends UserExeption {
    public UserNotFoundException(UUID id) {
        super("User not found with ID: " + id);
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
}
