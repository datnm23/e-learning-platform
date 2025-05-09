package com.github.datnm23.accountservice.config;

public final class AppConstants {

    private AppConstants() {} // Private constructor to prevent instantiation

    // Kafka Topics
    public static final String KAFKA_TOPIC_USER_EVENTS = "user-events";

    // Cache Names
    public static final String CACHE_USER_BY_ID = "userCache";
    public static final String CACHE_USER_BY_EMAIL = "userEmailCache";
    public static final String CACHE_PROFILE_BY_USER_ID = "userProfileCache";

    // Email Verification
    public static final long DEFAULT_TOKEN_EXPIRATION_MINUTES = 24 * 60; // 24 hours
}
