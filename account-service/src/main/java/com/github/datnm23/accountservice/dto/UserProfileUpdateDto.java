package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.Gender;
import com.github.datnm23.accountservice.statics.ValidationPatterns;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
public class UserProfileUpdateDto {
    @URL(message = "Avatar URL phải là một URL hợp lệ")
    private String avatarUrl;

    @Size(max = 1000, message = "Bio không được vượt quá 1000 ký tự")
    private String bio;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Pattern(regexp = ValidationPatterns.PHONE_REGEX, message = ValidationPatterns.PHONE_MESSAGE)
    private String phone;

    private String address;

    private Boolean emailNotifications;
    private Boolean pushNotifications;
}
