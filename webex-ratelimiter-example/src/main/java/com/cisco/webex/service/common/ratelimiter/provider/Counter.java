package com.cisco.webex.service.common.ratelimiter.provider;

import com.cisco.webex.service.common.ratelimiter.policy.action.Action;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright 2016 Cisco Inc. All rights reserved.
 */
public class Counter extends AtomicLong {

    /**
     * Backwards ref to the Action this counter is associated with in order to support things like metrics reporting.
     */
    private Action action;

    private long expiry = -1;

    // if set to a positive value, the max bound is enforced on increment operation
    private final long maxValue;

    public Counter(long initValue, Action action) {
        this(initValue, -1, action);
    }

    public Counter(long initValue, long expiryInMillis, Action action) {
        super(initValue);
        this.maxValue = initValue;
        this.expiry = expiryInMillis;
        this.action = action;
    }

    public boolean isExpired(long time) {
        return (expiry > -1 && expiry < time);
    }

    public long getExpiry() { return expiry; }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public Action getAction() {
        return action;
    }

    public long getAndIncrementIfNotMax() {
        if (maxValue <= 0) return getAndIncrement();
        return getAndUpdate(curVal -> (curVal < maxValue) ? curVal + 1 : maxValue);
    }
}
