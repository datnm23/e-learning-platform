package com.github.datnm23.accountservice.event.listener;

import com.github.datnm23.accountservice.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationNotificationListener {
    // TODO: implement email logic
    @EventListener
    @Order(2)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
    log.info("User registered event received: {}", event);
    }
}
