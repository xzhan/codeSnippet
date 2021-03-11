package com.example;

import com.common.message.Message;
import com.example.client.WNSClient;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * build poc for base on the topic prefix to forward to correct WNS server vip
 * eg: sj1_meeting_meeting_webex_notification  config PROD GPS endpoint -> sj1 WNS
 *     ats1_meeting_meeting_webex_notification config ATS GPS endpoint  -> ats WNS
 *
 */
@RestController
public class ProxyController {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired ProxyConfig proxyConfig;
    @Autowired WNSClient wnsClient;
    @Autowired CacheManager cacheManager;


    @PostMapping(value = "/api/v1/recovery/", produces = MediaType.APPLICATION_JSON_VALUE )
    public void recovery(@RequestBody Message message) throws URISyntaxException {
        logger.info("-- gps topic: " + message.getTopic() + " key: " + message.getKey() + " value :" + message.getValue());
        String dcCode = message.getTopic().split("_")[0];
        String desUrl = proxyConfig.wnsUrlMap().get(dcCode);
        logger.info("dcCode: " + dcCode + "desURL" + desUrl);
        URI baseUrl = new URI(desUrl);

        wnsClient.insertCache(baseUrl, message);

    }


    // store on the cache
    @PostMapping(value = "/sj1/proxy/create")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void createOnSJ1(@RequestBody Message message) {

        logger.info( "-- wns received on SJ1:" +  "topic: " + message.getTopic() + " key: " + message.getKey() + " value :" + message.getValue());
        putIntoCache(message);

    }

    @PostMapping(value = "/ln1/proxy/create")
    public void createOnLN1() {

    }


    void putIntoCache(Message message){
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("failMessageCache");
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        String messageUUID = UUID.randomUUID().toString();
        String key = "["+ messageUUID +"]"+ "[" + message.getTopic() + "]" + "[" + message.getKey() +"]";
        nativeCache.put(key, message.getValue());
        logger.info("-- wns start to put into cache");
        nativeCache.asMap().forEach((k,v) -> logger.info(k + " : " + v));
    }


}
