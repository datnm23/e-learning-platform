package com.github.datnm23.accountservice.exception.handler;

import com.github.datnm23.accountservice.dto.ErrorDetail;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.datnm23.accountservice.exception.*;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserExeption.class)
    public ResponseEntity<ErrorDetail> handleAccountException(UserExeption ex, WebRequest request) {
        log.error("Account service error: {}", ex.getMessage(), ex);
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Account Service Error",
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleProfileNotFoundException(ProfileNotFoundException ex, WebRequest request) {
        log.warn("Profile not found: {}", ex.getMessage());
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorDetail> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        log.warn("Duplicate resource violation: {}", ex.getMessage());
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    // Handle validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors.toString(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<ErrorDetail> handleActionNotAllowedException(ActionNotAllowedException ex, WebRequest request) {
        log.warn("Action not allowed: {}", ex.getMessage());
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorDetail> handleOptimisticLockingFailureException(Exception ex, WebRequest request) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());
        ErrorDetail errorDetails = new ErrorDetail(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "The resource was updated by another transaction. Please try again with the latest data.",
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }
}
