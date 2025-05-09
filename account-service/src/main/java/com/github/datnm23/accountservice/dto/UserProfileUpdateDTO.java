package com.github.datnm23.accountservice.dto;

import com.github.datnm23.accountservice.statics.Gender;
import com.github.datnm23.accountservice.statics.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Data
public class UserProfileUpdateDTO {

    @Size(max = 20, message = "Phone number không quá 20 ký tự ")
    @Pattern(regexp = ValidationPatterns.PHONE_REGEX, message = ValidationPatterns.PHONE_MESSAGE)
    private String phone;

    @Size(max = 255, message = "Avatar URL không vượt quá 255 ký tự")
    @URL(message = "Invalid URL format for avatar")
    @Schema(description = "URL của ảnh đại diện người dùng. Cung cấp giá trị để cập nhật.", example = "https://example.com/avatar.jpg", nullable = true)
    private String avatarUrl;

    @Size(max = 1000, message = "Bio không được vượt quá 1000 ký tự")
    private String bio;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Schema(description = "Giới tính (MALE, FEMALE, OTHER). Cung cấp giá trị để cập nhật.", example = "FEMALE", nullable = true)
    private Gender gender;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Schema(description = "Địa chỉ. Cung cấp giá trị để cập nhật.", example = "123 Example St, Apt 4B", nullable = true)
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Schema(description = "Thành phố. Cung cấp giá trị để cập nhật.", example = "Ho Chi Minh City", nullable = true)
    private String city;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Schema(description = "Quốc gia. Cung cấp giá trị để cập nhật.", example = "Vietnam", nullable = true)
    private String country;
    
    @Schema(description = "Bật/tắt nhận thông báo qua email. Cung cấp true/false để cập nhật.", example = "false", nullable = true)
    private Boolean emailNotifications;

    @Schema(description = "Bật/tắt nhận thông báo đẩy (push notifications). Cung cấp true/false để cập nhật.", example = "true", nullable = true)
    private Boolean pushNotifications;
}
