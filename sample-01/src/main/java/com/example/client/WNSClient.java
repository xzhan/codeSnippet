package com.example.client;

import com.common.message.Message;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name="WNSClient", configuration = {WnsFeignClientConfiguration.class})
public interface WNSClient {

    @PostMapping(
            path = "/proxy/create",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void insertCache(URI baseUri, @RequestBody Message message);
}


