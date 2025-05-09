package com.github.datnm23.accountservice.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {

    @Value("${feign.client.config.default.connectTimeout:5000}") // 5 giây
    private int connectTimeout;

    @Value("${feign.client.config.default.readTimeout:10000}") // 10 giây
    private int readTimeout;

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Retryer feignRetryer() {
        // period: Thời gian chờ giữa các lần retry (ms)
        // maxPeriod: Thời gian chờ tối đa giữa các lần retry (ms)
        // maxAttempts: Số lần retry tối đa
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // Hoặc FULL để debug chi tiết
    }
}

// Trong @FeignClient:
// @FeignClient(name = "security-service", path = "/api/v1/internal/security", configuration = CustomFeignClientConfiguration.class)