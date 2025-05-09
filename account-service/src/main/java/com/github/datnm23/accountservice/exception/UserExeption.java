package com.github.datnm23.accountservice.exception;

public class UserExeption extends RuntimeException {
    public UserExeption(String message) {
        super(message);
    }

    public UserExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
