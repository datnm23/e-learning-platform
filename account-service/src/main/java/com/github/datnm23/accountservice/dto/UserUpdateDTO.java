package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UserUpdateDTO {
    @Size(min = 2, max = 50, message = "Tên phải có từ 2-50 ký tự")
    private String firstName;

    @Size(min = 2, max = 50, message = "Họ phải có từ 2-50 ký tự")
    private String lastName;
}