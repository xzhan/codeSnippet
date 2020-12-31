package com.example;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class CachingConfiguration extends CachingConfigurerSupport {

    @Bean
    @ConfigurationProperties(prefix = "webex.kafkaclient.cache.caffeine.spec")
    public Map<String, String> caffeineSpec() {
        return new HashMap<>();
    }

    @Bean
    public CacheManager cacheManager(Ticker ticker) {
        Map<String, String> cacheSpecs = caffeineSpec();
        List<CaffeineCache> caches = cacheSpecs.entrySet().stream()
                .map(entry -> buildCache(entry.getKey(), entry.getValue(), ticker))
                .collect(Collectors.toList());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }

    private CaffeineCache buildCache(String name, String spec, Ticker ticker) {
        return new CaffeineCache(name, Caffeine.from(spec)
                .ticker(ticker).recordStats()
                .build());
    }

    @Bean
    public Ticker ticker() {
        return Ticker.systemTicker();
    }
}
