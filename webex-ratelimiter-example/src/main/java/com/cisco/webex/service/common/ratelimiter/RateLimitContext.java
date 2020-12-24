package com.cisco.webex.service.common.ratelimiter;

import com.cisco.webex.service.common.ratelimiter.policy.action.Action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * The context defines a set of methods that the rate limiter uses
 * to evaluate a policy
 *
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
public interface RateLimitContext {


    enum CounterType {
        USER, //Counter scope is user
        PROCESS, // VM, PROCESS scoped counter
        CLIENT,
        DC_USER, // Counter scope is per-user, but datacentre-wide
        DC,      // Counter scope is datacentre-wide
    };

    /**
     * @return - time in millis when the request was received
     */
    long getTime();

    /**
     * The user id associated with the request. Typically a UUID
     */
    String getId();

    /**
     * @return - true if debugging is enabled for the context
     */
    boolean isDebugEnabled();

    /**
     * @return - the request object
     */
    HttpServletRequest getRequest();

    /**
     * @return - the response object
     */
    default HttpServletResponse getResponse() { return null; }

    /**
     * @return - the target method for this request
     */
    Method getEndPointMethod();

//    /**
//     * Match a user property
//     * @param key - user property key
//     * @param value - value
//     * @return true if a match has been made
//     */
//    boolean isUserPropertyMatch(String key, String value);
//
//    /**
//     * Match a user property against a regex
//     * @param key
//     * @param regex
//     * @return true if a match is successful
//     */
//    boolean isUserPropertyMatch(String key, Pattern regex);
//
//    /**
//     * Match the client ID associated with this context
//     * @param expectedClientId
//     * @return true if a match is successful
//     */
//    boolean isClientIdMatch(String expectedClientId);


    /**
     * @return - the counter value and decrement it. Will create a counter with given value and
     * expiry if it doesn't exist.
     */
    long getAndDecrementCounter(CounterType type, String name, long initialValue, long expiry, Action action);

    /**
     * @return - the counter value and increment it. Will create a counter with given value and
     * expiry if it doesn't exist.
     */
    long getAndIncrementCounter(CounterType type, String name, long initialValue, long expiry, Action action);

    /**
     * @return - the current value of the counter
     */
    long getCounterValue(CounterType type, String name);

    /**
     * @return - the expiry value of the counter
     */
    long getCounterExpiry(CounterType type, String name);

    /**
     * @return - the total counters tracked by permit
     */
    long getCounterCount(CounterType type);

    /**
     * update the caches with the counters if the counters are dirty
     */
    void syncCounters();

    /**
     * @param s - string captured during a match
     */
    void pushMatchCapture(String s);

    /**
     * @return - all the captures as a formatted string delimited by pipe(|)
     * As a side effect, the capture stack is emptied.
     */
    String popMatchCaptures();

    /**
     * @param action - action to be reset (rewind counters)
     */
    void registerForCleanup(Action action, String counterName);

    /**
     * This callback is invoked just prior to the context being removed
     */
    void finish();
}
