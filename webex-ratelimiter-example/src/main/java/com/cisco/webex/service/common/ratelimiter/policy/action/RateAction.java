package com.cisco.webex.service.common.ratelimiter.policy.action;

import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RateAction extends Action {
    private static final String DEFAULT_DENY_REASON = "Request rate exceeds threshold.";

    @JsonCreator
    public RateAction(@JsonProperty("permits") long permits, @JsonProperty("interval") String interval, @JsonProperty("scope") String scope) {
        this.permits = permits;
        this.interval = TimeInterval.parseToMillis(interval);
        this.scope = (scope == null) ? RateLimitContext.CounterType.USER : RateLimitContext.CounterType.valueOf(scope);
        this.denyReason = DEFAULT_DENY_REASON;
    }

    protected final long permits;
    protected final long interval;
    protected final RateLimitContext.CounterType scope;

    @Override
    public String getId() {
        return String.format("RateAction[%s:%d:%d]", scope, permits, interval);
    }

    /**
     * Evaluates a policy
     *
     * @param context
     * @param keyPrefix
     * @return an Enforcement object identifying status and whether evaluation should proceed
     */
    @Override
    public Enforcement enforce(RateLimitContext context, String keyPrefix) {
        String key = keyFor(context, keyPrefix);
        if (context.getAndDecrementCounter(scope, key, permits, interval, this) <= 0) {
            if (context.isDebugEnabled()) LOG.info("DENIED, permits exhausted, " + toString());
            long expiry = context.getCounterExpiry(scope, key);
            if (expiry != -1) expiry = (expiry - context.getTime()) / 1000;
            return Enforcement.deny(denyReason, headers, getPolicy(), permits, (int)expiry);
        }
        if (context.isDebugEnabled()) LOG.info("ALLOWED, permits available, " + toString());
        return Enforcement.next(getPolicy());
    }


    @Override
    public String toString() {
        return getId();
    }

    public long getLimit() {
        return permits;
    }

    @JsonProperty
    public String getInterval() {
        return interval + "ms";
    }

    public RateLimitContext.CounterType getScope() {
        return scope;
    }

    public boolean hasDatacentreScope() {
        return scope == RateLimitContext.CounterType.DC || scope == RateLimitContext.CounterType.DC_USER;
    }
}
