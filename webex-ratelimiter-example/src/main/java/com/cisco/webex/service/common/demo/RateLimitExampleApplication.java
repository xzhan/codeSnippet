package com.cisco.webex.service.common.demo;


import com.cisco.webex.service.common.ratelimiter.AbstractRateLimitingFilter;
import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.cisco.webex.service.common.ratelimiter.RateLimiter;
import com.cisco.webex.service.common.ratelimiter.policy.Policy;
import com.cisco.webex.service.common.ratelimiter.policy.action.Action;
import com.cisco.webex.service.common.ratelimiter.provider.BasicRateLimitContext;
import com.cisco.webex.service.common.ratelimiter.provider.Counter;
import com.cisco.webex.service.common.ratelimiter.provider.Permit;
import com.cisco.webex.service.common.ratelimiter.util.RateLimitingSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class RateLimitExampleApplication {
    private static final Logger log = LoggerFactory.getLogger(RateLimitExampleApplication.class);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    /* This is a local cache. A real implementation could have this as a shared cache across instances using
     * something like redis
     */
    private Cache<String, Permit> permitsCache = CacheBuilder.newBuilder().build();
    private Cache<String, Counter> counterCache = CacheBuilder.newBuilder().build();

    public static void main(String[] args) {
        SpringApplication.run(RateLimitExampleApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean rateLimitFilterBean() throws IOException {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(rateLimitingFilter());
        return bean;
    }


    private Filter rateLimitingFilter() throws IOException
    {
        String rateLimitPolicy = getRateLimitPolicy("rateLimitingPolicy");
        RateLimitingSettings settings = new RateLimitingSettings.Builder()
                .rateLimitPolicyJson(rateLimitPolicy)
                .build();


        // The core of the example - create a new rate limiter derived from the abstract RateLimiter class. There is
        // one method to override - createContext - that creates a rate limiting context for the current request that
        // in turn will require an extension of BasicRateLimitContext.
        // The rate-limit.json file in resources/static is the declarative method for creating rate limit policies
        RateLimiter rateLimiter = new RateLimiter() {

            @Override
            public RateLimitContext createContext(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

                // This userid is really just an example. A real implementation would have it stashed somewhere
                // in the request attributes like we do in spark with AuthInfo, or something equivalent
                final String userid = request.getHeader("Userid") == null ? UUID.randomUUID().toString() : request.getHeader("Userid");
                return new BasicRateLimitContext(userid, request, response, permitsCache, counterCache, true) {

                };
            }

        };

        rateLimiter.addPolicies(parseJsonPolicies(rateLimitPolicy));

        return new ExampleRateLimitingFilter(settings, rateLimiter);
    }


    private String getRateLimitPolicy(String name) throws IOException
    {
        String uri = String.format("/static/%s.json", name);
        return CharStreams.toString(new InputStreamReader(
                getClass().getResourceAsStream(uri)));
    }


    private static List<Policy> parseJsonPolicies(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<Policy>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    class ExampleRateLimitingFilter extends AbstractRateLimitingFilter {

        ExampleRateLimitingFilter(RateLimitingSettings settings, RateLimiter rateLimiter) {
            super(settings, rateLimiter);
        }

        @Override
        public void onHandleViolation(RateLimitContext rateLimitContext, HttpServletRequest request, Action.Enforcement enforcement) {
            log.info("Rate-limiting {} based on policy {}", request.getContextPath(), enforcement.getPolicy().getName());
        }

        @Override
        public boolean mustReject(HttpServletRequest request) {
            // Can use the settings here to determine if there is something special to be done to rate limit the request
            // that doesn't fall neatly into the policies
            return false;
        }

        @Override
        public void onFilterCompletion(HttpServletRequest httpServletRequest, String s, long l) {

        }
    }
}
