package com.example.job;

import com.alibaba.fastjson.JSONObject;
import com.common.message.meeting.PostMeetingNotificationMessage;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ResendSchedule {


    private static final Logger log = LoggerFactory.getLogger(ResendSchedule.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired(required = false)
    @Qualifier(value = "KAFKA_TEMPLATE_NOTIFICATION")
    private KafkaTemplate<Object, Object> template;

    @Autowired
    CacheManager cacheManager;
    @Scheduled(fixedRate = 10000)
    public void load4Resend() {
        log.info("The time is now {}", dateFormat.format(new Date()));
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("failMessageCache");
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        log.info("-- wns loading cache ....");
        nativeCache.asMap().forEach((k,v) -> log.info(k + " : " + v));
        nativeCache.asMap().forEach((k,v) -> sendMessage(k,v));

    }

    private void sendMessage(Object k, Object v) {

        String[] keys = k.toString().split("]");
        String topic = keys[1];
        String key = keys[2];
        PostMeetingNotificationMessage postMeetingNotificationMessage = JSONObject.parseObject(v.toString(), PostMeetingNotificationMessage.class);
        log.info("-- wns sending topic :" + topic +" " + " key :" + key + " value " + v.toString());
        template.send(new ProducerRecord<>(topic, key, postMeetingNotificationMessage));
    }

}
