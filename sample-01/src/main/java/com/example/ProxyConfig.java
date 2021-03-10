package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProxyConfig {


    @Bean
    @ConfigurationProperties(prefix = "webex.wns.url")
    public Map<String, String> wnsUrlMap() {
        return new HashMap<>();
    }
}
