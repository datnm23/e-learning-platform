package com.github.datnm23.accountservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// "security-service" là tên đăng ký trên Eureka/Consul của Security Service
@FeignClient(name = "security-service", path = "/api/v1/internal/security") // Path có thể là internal
public interface SecurityServiceClient {
    @PostMapping("/hash")
    HashResponse hashPassword(@RequestBody HashRequest request);

    // DTOs cho request và response (đặt trong package client hoặc common DTO)
    record HashRequest(String plainPassword) {}
    record HashResponse(String hashedPassword) {}
}
