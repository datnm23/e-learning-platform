package com.github.datnm23.accountservice.statics;

public enum AccountStatus {
    ACTIVE,  // Hoạt động
    INACTIVE, // Không hoạt động
    SUSPENDED, // Đã bị khóa    
    PENDING_VERIFICATION, // Chờ xác thực email
    CLOSED, // Đã bị đóng
    DEACTIVATED, // Đã bị deactivated   

}
