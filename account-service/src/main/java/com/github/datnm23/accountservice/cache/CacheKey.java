package com.github.datnm23.accountservice.cache;

public enum CacheKey {
    USER("user:%s"),                     // id
    OAUTH("oauth:providers:%s:%s"),      // provider, externalId
    RATE_LIMIT("ratelimit:account:%s");  // ip

    private final String pattern;

    CacheKey(String pattern) {
        this.pattern = pattern;
    }

    public String format(Object... args) {
        return String.format(pattern, args);
    }
}
