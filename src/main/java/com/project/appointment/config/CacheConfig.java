package com.project.appointment.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();
        
        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Business cache - 1 hour TTL
        cacheConfigurations.put("business", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("businesses", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Services cache - 30 minutes TTL
        cacheConfigurations.put("services", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Employees cache - 30 minutes TTL
        cacheConfigurations.put("employees", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Work schedules cache - 1 hour TTL
        cacheConfigurations.put("workSchedules", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Appointments cache - 15 minutes TTL
        cacheConfigurations.put("appointments", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Dashboard cache - 5 minutes TTL
        cacheConfigurations.put("dashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Analytics cache - 10 minutes TTL
        cacheConfigurations.put("analytics", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

