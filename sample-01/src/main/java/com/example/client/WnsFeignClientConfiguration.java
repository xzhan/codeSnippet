package com.example.client;


import feign.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class WnsFeignClientConfiguration {

    @Autowired
    private RemoteClientConfiguration remoteClientConfiguration;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

}
