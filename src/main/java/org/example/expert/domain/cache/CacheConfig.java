package org.example.expert.domain.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final int MAX_CACHE_SIZE = 1000;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Collections.singletonList(new ConcurrentMapCache("userCache", createLimitedSizeMap(), false)));
        return cacheManager;
    }

    private ConcurrentMap<Object, Object> createLimitedSizeMap() {
        return new ConcurrentHashMap<>(new LinkedHashMap<Object, Object>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
    }
}