package com.github.datnm23.accountservice.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationAnalyticsListener {
    // TODO: implement analytics logic
    @EventListener
    @Order(3)
    public void handleUserRegistrationEvent() {
        log.info("User registration analytics event received");
    }
}
