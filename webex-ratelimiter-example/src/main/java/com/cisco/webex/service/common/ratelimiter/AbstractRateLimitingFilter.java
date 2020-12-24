package com.cisco.webex.service.common.ratelimiter;

import com.cisco.webex.service.common.ratelimiter.policy.Policy;
import com.cisco.webex.service.common.ratelimiter.policy.action.Action;
import com.cisco.webex.service.common.ratelimiter.util.RateLimitingSettings;
import com.google.common.base.Stopwatch;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
public abstract class AbstractRateLimitingFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRateLimitingFilter.class);
    public static final String HEADER_RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_RATE_LIMIT_RESET = HttpHeaders.RETRY_AFTER;
    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
    protected final RateLimiter rateLimiter;


    // legacy settings
    protected RateLimitingSettings settings;

    public AbstractRateLimitingFilter(RateLimitingSettings settings, RateLimiter rateLimiter) {
        this.settings = settings;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    public abstract void onHandleViolation(RateLimitContext context, HttpServletRequest request, Action.Enforcement enforcement);

    public abstract boolean mustReject(HttpServletRequest request);

    public abstract void onFilterCompletion(HttpServletRequest request, String filterName, long elapsedTime);


    @Override
    public final void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        RateLimitContext context = null;
        try {
            try {
                context = rateLimiter.createContext(request, response);
                Action.Enforcement result = rateLimiter.evaluate(context);
                if (!result.isPass()) {
                    LOG.info("check result pass ? {}", result.isPass() );
                    if (handleViolation(context, request, response, result)) return;
                }
            } catch (Exception e) {
                LOG.warn("Unexpected error when rate limiting.", e);
            }
            chain.doFilter(req, res);
        } finally {
            onFilterCompletion(request, this.getClass().getSimpleName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            if (context != null) context.finish();
        }
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    private boolean handleViolation(RateLimitContext context,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Action.Enforcement enforcement) throws IOException {

        onHandleViolation(context, request, enforcement); // Do some specific violation handling - e.g., log the event
        Policy policy = enforcement.getPolicy();
        onReject(policy);

        if (!policy.enforce()) return false;

        if (enforcement.getReset() > -1) response.setHeader(HEADER_RATE_LIMIT_RESET, "" + enforcement.getReset());
        response.setHeader(HEADER_RATE_LIMIT_LIMIT, "" + enforcement.getLimit());

        if (enforcement.getHeaders() != null) {
            for (Map.Entry<String, String> entry : enforcement.getHeaders().entrySet()) {
                response.setHeader(entry.getKey(), entry.getValue());
            }
        }

        // log the metric
        int errorCode = HTTP_STATUS_TOO_MANY_REQUESTS;
        if (mustReject(request)) {
            errorCode = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
        if (enforcement.getCode() > 0) errorCode = enforcement.getCode();
        // sync
        response.sendError(errorCode, enforcement.getMessage());
        return true;
    }

    protected void onReject(Policy policy) {
        if (policy == null || policy.getName() == null) return;
        String name = policy.getName().replaceAll("\"[^\\dA-Za-z ]\"", "_");
        LOG.info("policy name {}", name );
    }


}
