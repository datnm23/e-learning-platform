package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = ValidationPatterns.EMAIL_MESSAGE)
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải có từ 8 ký tự")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX, message = ValidationPatterns.PASSWORD_MESSAGE)
    private String password;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 50, message = "Tên phải có từ 2-50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(min = 2, max = 50, message = "Họ phải có từ 2-50 ký tự")
    private String lastName;
}