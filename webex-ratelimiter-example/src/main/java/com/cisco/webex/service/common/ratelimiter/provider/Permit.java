package com.cisco.webex.service.common.ratelimiter.provider;

import com.cisco.webex.service.common.ratelimiter.policy.action.Action;
import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
public final class Permit {
    private static final Logger LOG = LoggerFactory.getLogger(Permit.class);

    private Map<String, WeakReference<Counter>> counterRefs = new ConcurrentHashMap<>();
    private final Cache<String, Counter> counterCache;
    private final String id;
    private boolean dirty;

    public Permit(String id, Cache<String, Counter> counterCache) {
        this.id = id;
        this.counterCache = counterCache;
        this.dirty = true;
    }

    public boolean isDirty() { return dirty; }

    public void markDirty() { this.dirty = true; }

    private String getCompoundName(String key) { return id + "|" + key; }

    public Counter findOrCreateCounter(final String key, final long initValue, final long expireAfterMillis, final long time, final Action action) {
        String compoundKey = getCompoundName(key);
        LOG.info("Looking up {} {} {} {} ", compoundKey, initValue, expireAfterMillis, time);
        try {
            final BitSet cacheMiss = new BitSet(1);
            Counter value = counterCache.get(compoundKey, new Callable<Counter>() {
                @Override
                public Counter call() throws Exception {
                    cacheMiss.set(0);
                    Counter counter = new Counter(initValue, (expireAfterMillis > -1 ? (time + expireAfterMillis) : -1), action);
                    counterRefs.put(key, new WeakReference<Counter>(counter));
                    for(Map.Entry<String, WeakReference<Counter>> entry: counterRefs.entrySet()){
                        LOG.info("Permit counterRefs: key ->  {}, value -> {}", entry.getKey(), entry.getValue());
                    }
                    return counter;
                }
            });
            if (!cacheMiss.get(0) && value.isExpired(time)) {
                value.set(initValue);
                if (expireAfterMillis > -1) value.setExpiry(time + expireAfterMillis);
                counterCache.put(compoundKey, value); // put the key back to reset creation time

            }
            LOG.info("Counter : {}", value.toString());
            return value;
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error creating counter - " + compoundKey, e);
        }
    }

    public Counter getCounter(String key) {
        return counterCache.getIfPresent(getCompoundName(key));
    }

    public long getCounterCount() { return counterRefs.size(); }

    public long getCounterValue(String key) {
        AtomicLong val = counterCache.getIfPresent(getCompoundName(key));
        checkNotNull(val);
        return val.get();
    }

    // used for testing only
    public Map<String, WeakReference<Counter>> getRefs() {
        return counterRefs;
    }

    /*
    public void sync() {
        if (!isDirty()) return;
        counterRefs.stream().forEach(e -> {
            String compoundName = getCompoundName(e);
            Counter value = counterCache.getIfPresent(compoundName);
            if (value != null) counterCache.put(compoundName, value);
        });
    }
    */

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Permit{");
        sb.append("counters=").append(counterRefs);
        sb.append(", dirty=").append(dirty);
        sb.append('}');
        return sb.toString();
    }

    public void removeCounters() {
        for(Iterator<String> iter = counterRefs.keySet().iterator(); iter.hasNext(); ) {
            String compoundName = Permit.this.getCompoundName(iter.next());
            counterCache.invalidate(compoundName);
        }

        counterRefs.clear();
    }

    public int removeExpiredCounters(long time) {
        List<String> expiredCounters = new ArrayList<>();

        for(Iterator<Map.Entry<String, WeakReference<Counter>>> iter = counterRefs.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, WeakReference<Counter>> e = iter.next();
            String compoundName = Permit.this.getCompoundName(e.getKey());
            Counter counter = e.getValue().get();
            if (counter == null) {
                // weak reference has been cleared
                expiredCounters.add(e.getKey());
            } else if (counter.isExpired(time)) {
                counterCache.invalidate(compoundName);
                expiredCounters.add(e.getKey());
            }
        }

        for(Iterator<String> iter = expiredCounters.iterator(); iter.hasNext(); ) {
            counterRefs.remove(iter.next());
        }
        return expiredCounters.size();
    }

}
