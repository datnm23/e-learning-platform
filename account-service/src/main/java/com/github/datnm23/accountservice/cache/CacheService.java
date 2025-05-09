package com.github.datnm23.accountservice.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {
    //Tăng giá trị của một counter trong Redis thêm 1.
    long incr(CacheKey key, Duration ttl, Object... keyArgs);

    //Lấy giá trị từ Redis bằng key chỉ định.
    <T> Optional<T> get(CacheKey key, Class<T> type, Object... keyArgs);

    //Đặt một giá trị vào Redis với key và TTL chỉ định.
    <T> void set(CacheKey key, Duration ttl, T value);

    //Xóa một key (và giá trị tương ứng) khỏi Redis.
    boolean delete(CacheKey key, Object... keyArgs);
}
