package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = ValidationPatterns.EMAIL_MESSAGE)
    private String email;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 50, message = "Tên phải có từ 2-50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(min = 2, max = 50, message = "Họ phải có từ 2-50 ký tự")
    private String lastName;
}