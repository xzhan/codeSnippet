package com.example.job;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ResendSchedule {


    private static final Logger log = LoggerFactory.getLogger(ResendSchedule.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    CacheManager cacheManager;
    @Scheduled(fixedRate = 10000)
    public void load4Resend() {
        log.info("The time is now {}", dateFormat.format(new Date()));
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("failMessageCache");
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        log.info("-- wns loading cache ....");
        nativeCache.asMap().forEach((k,v) -> log.info(k + " : " + v));

    }


}
