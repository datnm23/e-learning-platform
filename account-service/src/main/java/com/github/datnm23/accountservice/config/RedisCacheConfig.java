package com.github.datnm23.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    @Value("${app.cache.default-ttl:15m}")
    private Duration defaultTtl;

    @Value("${app.cache.user-cache.ttl:30m}") // TTL riêng cho cache user profile
    private Duration userCacheTtl;

    @Value("${app.cache.oauth-provider-cache.ttl:60m}")
    private Duration oauthProviderCacheTtl;

    @Value("${app.cache.prefix:account-service}")
    private String cachePrefix;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        // --- Định nghĩa  Serializers
        RedisSerializationContext.SerializationPair<String> keySerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());
        RedisSerializationContext.SerializationPair<Object> valueSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());

        // --- Cấu hình MẶC ĐỊNH cho tất cả các cache ---
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(defaultTtl)
                .serializeKeysWith(keySerializer)
                .serializeValuesWith(valueSerializer)
                // Tiền tố cho Key
                .prefixCacheNameWith(cachePrefix + "::")
                .disableCachingNullValues();


        return RedisCacheManager.builder(redisConnectionFactory)
                // Áp dụng mặc định cho tất cả cache
                .cacheDefaults(defaultConfig)
                // Cấu hình cache cụ thể
                .withCacheConfiguration("userCache", // Tương ứng  @Cacheable("userCache")
                        defaultConfig.entryTtl(userCacheTtl) // Ghi đè TTL
                )
                .withCacheConfiguration("oauthProviderCache", // Tương ứng  @Cacheable("oauthProviderCache")
                        defaultConfig.entryTtl(oauthProviderCacheTtl) // Ghi đè TTL
                )
                // Add other specific cache configs here...


                // --- Optional Features ---
                .enableStatistics()       // Bật số liệu thống kê để theo dõi (Actuator /metrics)
                .transactionAware()     // Liên kết các hoạt động bộ nhớ đệm với Spring transactions
                .build();
    }
}
