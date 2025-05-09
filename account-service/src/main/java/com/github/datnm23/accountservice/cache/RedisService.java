package com.github.datnm23.accountservice.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements CacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> ops() {
        return redisTemplate.opsForValue();
    }

    private String key(CacheKey k, Object... args) {
        return k.format(args);
    }

    @Override
    public <T> void set(CacheKey k, Duration ttl, T val) {
        if (val == null) return;
        try {
            ops().set(key(k), val, ttl);
        } catch (Exception e) {
            log.error("Redis SET failed for key {}", k, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> get(CacheKey k, Class<T> type, Object... args) {
        try {
            Object v = ops().get(key(k, args));
            return (v != null && type.isInstance(v)) ? Optional.of((T) v) : Optional.empty();
        } catch (Exception e) {
            log.error("Redis GET failed for key {}", k, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(CacheKey k, Object... args) {
        try {
            return redisTemplate.delete(key(k, args));
        } catch (Exception e) {
            log.error("Redis DEL failed for key {}", k, e);
            return false;
        }
    }

    @Override
    public long incr(CacheKey k, Duration ttl, Object... args) {
        String realKey = key(k, args);
        try {
            Long c = ops().increment(realKey);
            if (c != null && c == 1L) { // lần đầu
                redisTemplate.expire(realKey, ttl.toMillis(), TimeUnit.MILLISECONDS);
            }
            return c == null ? -1L : c;
        } catch (Exception e) {
            log.error("Redis INCR failed for key {}", realKey, e);
            return -1L;
        }
    }

    private String key(CacheKey k) {
        return k.format();
    }
}
