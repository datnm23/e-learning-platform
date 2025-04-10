package com.github.datnm23.accountservice.statics;

public class ValidationPatterns {
    // Regex patterns
    public static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    public static final String PHONE_REGEX = "^(84|0[3|5|7|8|9])+([0-9]{8})$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    // Error messages
    public static final String EMAIL_MESSAGE = "Email phải có định dạng hợp lệ";
    public static final String PHONE_MESSAGE = "Số điện thoại phải bắt đầu bằng 0 hoặc 84 và có 9-10 số";
    public static final String PASSWORD_MESSAGE = "Mật khẩu phải chứa ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt";
}
