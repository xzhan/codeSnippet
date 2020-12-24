package com.cisco.webex.service.common.ratelimiter;


import com.cisco.webex.service.common.ratelimiter.policy.Policy;
import com.cisco.webex.service.common.ratelimiter.policy.action.Action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The RateLimiter holds a set of ordered policies. Policies are ordered
 * due to the fact evaluation is done in order.
 *
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
public abstract class RateLimiter {
    // assertPolicies holds whitelist and blacklist in order
    private final List<Policy> assertPolicies = new ArrayList<>();
    private final List<Policy> policies = new ArrayList<>();
    private final List<Policy> datacentrePolicies = new ArrayList<>();

    public RateLimiter() { }

    public RateLimiter(List<Policy> policies) {
        addPolicies(policies);
    }

    public void addPolicies(List<Policy> policies) {
        if (policies == null) return;
        for (Policy p : policies) addPolicy(p);
    }

    public void addPolicy(Policy policy) {
        checkNotNull(policy);
        checkNotNull(policy.getName());
        if (getPolicy(policy.getName()) != null) {
            throw new IllegalArgumentException("Duplicate policy. A policy exists with name - "
                    + policy.getName() );
        }
        if (policy.getAssertAction() != null) {
            assertPolicies.add(policy);
        } else if (policy.hasDataCentreWideAction()) {
            datacentrePolicies.add(policy);
        } else {
            policies.add(policy);
        }
    }

    public Policy getPolicy(String policyName) {
        for (Policy p : getPolicies()) {
            if (p.getName().equals(policyName)) return p;
        }
        return null;
    }

    public boolean removePolicy(String policyName) {
        checkNotNull(policyName);
        Policy p = getPolicy(policyName);
        if (p != null) {
            if (p.getAssertAction() != null) return assertPolicies.remove(p);
            else if(p.hasDataCentreWideAction()) return datacentrePolicies.remove(p);
            else return policies.remove(p);
        }
        return false;
    }

    public void removePolicies() {
        datacentrePolicies.clear();
        policies.clear();
        assertPolicies.clear();
    }

    public List<Policy> getPolicies() {
        List<Policy> result = new ArrayList<>(assertPolicies);
        result.addAll(policies);
        result.addAll(datacentrePolicies);
        return result;
    }

    public final Action.Enforcement evaluate(RateLimitContext context) {
        for (Policy p : getPolicies()) {
            Action.Enforcement result = p.enforce(context);
            if (result.getStatus() == Action.Status.EVAL_SKIP) return result;
        }
        context.syncCounters();
        return Action.Enforcement.forceAllow(null); // allow requests if nothing has limited them
    }

    public RateLimitContext createContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }

}
