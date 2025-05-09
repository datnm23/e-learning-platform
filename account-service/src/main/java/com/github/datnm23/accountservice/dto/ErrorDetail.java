package com.github.datnm23.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
