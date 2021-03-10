package com.example.client;


import com.example.Application;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = {Application.class})
public class RemoteClientConfiguration {

    private String currentAppName;

    public String getCurrentAppName() {
        return currentAppName;
    }
}
