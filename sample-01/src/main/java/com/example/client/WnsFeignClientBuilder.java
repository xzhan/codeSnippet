package com.example.client;

import feign.Contract;
import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Import(FeignClientsConfiguration.class)
@Component
public class WnsFeignClientBuilder {


    @Autowired
    private final WNSClient wnsClient;

    @Autowired
    public WnsFeignClientBuilder(Decoder decoder, Encoder encoder, Contract contract) {
        this.wnsClient = Feign.builder()
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .target(Target.EmptyTarget.create(WNSClient.class));
    }

    public WNSClient getWnsClient() {
        return wnsClient;
    }


}
