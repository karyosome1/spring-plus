package org.example.expert.domain.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void clearUserCache() {
        clearCache("userCache");
        System.out.println("캐시가 초기화되었습니다.");
    }
}