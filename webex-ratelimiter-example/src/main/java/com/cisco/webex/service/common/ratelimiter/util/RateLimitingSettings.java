package com.cisco.webex.service.common.ratelimiter.util;


public class RateLimitingSettings {

    protected final String rateLimitPolicyJson;



    protected RateLimitingSettings(Builder builder)
    {

        rateLimitPolicyJson = builder.rateLimitPolicyJson;
    }


    public String getRateLimitPolicyJson() { return rateLimitPolicyJson; }



    public static class Builder {

        private String rateLimitPolicyJson;
        public Builder rateLimitPolicyJson(String rateLimitPolicyJson) { this.rateLimitPolicyJson = rateLimitPolicyJson; return this; }
        public RateLimitingSettings build() { return new RateLimitingSettings(this);}

    }
}

