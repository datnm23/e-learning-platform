package com.github.datnm23.accountservice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Size(min = 2, max = 50, message = "Tên phải có từ 2-50 ký tự")
    private String firstName;

    @Size(min = 2, max = 50, message = "Họ phải có từ 2-50 ký tự")
    private String lastName;

    private Boolean isActive;
}