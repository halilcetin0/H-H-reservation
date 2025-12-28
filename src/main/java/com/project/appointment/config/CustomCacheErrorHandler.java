package com.project.appointment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {
    
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache get error for key '{}' in cache '{}': {}. Continuing without cache.", 
                key, cache.getName(), exception.getMessage());
    }
    
    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Cache put error for key '{}' in cache '{}': {}. Continuing without cache.", 
                key, cache.getName(), exception.getMessage());
    }
    
    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache evict error for key '{}' in cache '{}': {}. Continuing without cache.", 
                key, cache.getName(), exception.getMessage());
    }
    
    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Cache clear error for cache '{}': {}. Continuing without cache.", 
                cache.getName(), exception.getMessage());
    }
}


