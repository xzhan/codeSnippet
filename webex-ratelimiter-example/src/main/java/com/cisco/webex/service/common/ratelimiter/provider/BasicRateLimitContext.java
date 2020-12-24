package com.cisco.webex.service.common.ratelimiter.provider;

import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.cisco.webex.service.common.ratelimiter.policy.action.Action;
import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
public abstract class BasicRateLimitContext implements RateLimitContext {

    public static final String PROCESS_PERMIT_KEY = "<<PROCESS_PERMIT>>";
    private static final Logger LOG = LoggerFactory.getLogger(BasicRateLimitContext.class);
    public static final String HEADER_RATE_LIMIT_DEBUG = "X-RateLimit-Debug";
    public static final String EXPIRY_KEY_SUFFIX = "-expiry";

    protected final String userId;
    protected final long time;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private final Cache<String, Permit> permitCache;
    private final Cache<String, Counter> counterCache;
    private final Permit userPermit;
    private final Permit processPermit;
    private final Map<Action, String> resetActions = new HashMap<>();
    private final StringBuilder captures = new StringBuilder();
    protected final boolean debug;

    public BasicRateLimitContext(final String userId, final HttpServletRequest request,
                                 final Cache<String, Permit> permitCache,
                                 final Cache<String, Counter> counterCache)
            throws ExecutionException {
        this(userId, permitCache, counterCache, request.getHeader(HEADER_RATE_LIMIT_DEBUG) != null);
        this.request = request;
    }

    public BasicRateLimitContext(final String userId, final HttpServletRequest request,
                                 final Cache<String, Permit> permitCache,
                                 final Cache<String, Counter> counterCache,
                                 final boolean enableDebug)
            throws ExecutionException {
        this(userId, permitCache, counterCache, enableDebug ? true : (request.getHeader(HEADER_RATE_LIMIT_DEBUG) != null));
        this.request = request;
    }

    public BasicRateLimitContext(final String userId, final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final Cache<String, Permit> permitCache,
                                 final Cache<String, Counter> counterCache,
                                 final boolean enableDebug)
            throws ExecutionException {
        this(userId, permitCache, counterCache, enableDebug ? true : (request.getHeader(HEADER_RATE_LIMIT_DEBUG) != null));
        this.request = request;
        this.response = response;
    }

    /**
     * Constructor used for non-HTTP rate limit contexts.
     */
    public BasicRateLimitContext(final String userId,
                                 final Cache<String, Permit> permitCache,
                                 final Cache<String, Counter> counterCache,
                                 final boolean debug)
            throws ExecutionException {
        this.userId = userId;
        this.time = System.currentTimeMillis();
        this.permitCache = permitCache;
        this.counterCache = counterCache;
        this.debug = debug;

	// Do not use Java 8 - this must compile with Java 7 for use with CI
        this.userPermit = permitCache.get(userId, new Callable<Permit>() {
            @Override
            public Permit call() throws Exception {
                return new Permit(userId, counterCache);
            }
        });
        this.processPermit = permitCache.get(PROCESS_PERMIT_KEY, new Callable<Permit>() {
            @Override
            public Permit call() throws Exception {
                return new Permit(PROCESS_PERMIT_KEY, counterCache);
            }
        });
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BasicRateLimitContext{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", uri=").append(request.getRequestURI());
        sb.append(", debug=").append(debug);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    @Override
    public Method getEndPointMethod() {
        return null;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public boolean isDebugEnabled() {
        return debug;
    }

    @Override
    public long getCounterValue(CounterType type, String name) {
        return getPermit(type).getCounterValue(name);
    }

    @Override
    public long getCounterExpiry(CounterType type, String name) {
        return getPermit(type).getCounter(name).getExpiry();
    }

    @Override
    public long getAndDecrementCounter(CounterType type, String name, long initialValue, long expiry, Action action) {
        AtomicLong acl = getPermit(type).findOrCreateCounter(name, initialValue, expiry, time, action);
        LOG.info("COUNTER--: " + name + " " + acl.get());
        getPermit(type).markDirty();
        return acl.getAndDecrement();
    }

    @Override
    public long getAndIncrementCounter(CounterType type, String name, long initialValue, long expiry, Action action) {
        Counter acl = getPermit(type).findOrCreateCounter(name, initialValue, expiry, time, action);
        LOG.info("COUNTER++: " + name + " " + acl.get());
        getPermit(type).markDirty();
        return acl.getAndIncrementIfNotMax();
    }

    @Override
    public long getCounterCount(CounterType type) {
        return getPermit(type).getCounterCount();
    }

    @Override
    public void syncCounters() {
        if (userPermit.isDirty()) {
            permitCache.put(userId, userPermit);
        }
        if (processPermit.isDirty()) {
            permitCache.put(PROCESS_PERMIT_KEY, processPermit);
        }
    }

    @Override
    public void registerForCleanup(Action action, String counterName) {
        resetActions.put(action, counterName);
    }

    @Override
    public void pushMatchCapture(String capture) {
        if (captures.length() > 0) captures.append("|");
        captures.append(capture);
    }

    @Override
    public String popMatchCaptures() {
        String c = captures.toString();
        captures.setLength(0); // clear the buffer
        return c;
    }

    @Override
    public void finish() {
        for (Map.Entry<Action,String> entry : resetActions.entrySet()) {
            entry.getKey().release(this, entry.getValue());
        }
    }

    private Permit getPermit(CounterType type) {
        switch(type) {
            case USER: return userPermit;
            case PROCESS: return processPermit;
        }
        throw new IllegalArgumentException("Type:" + type + " does not have a corresponding permit!");
    }

}
