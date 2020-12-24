package com.cisco.webex.service.common.ratelimiter.policy.matcher;

import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A matcher is used for determining if a match can be made
 * in the current context based on the configured policy
 *
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EndPointMatcher.class, name = "end-point"),
})
public abstract class Matcher {

    protected boolean useCapture;

    public abstract boolean matches(RateLimitContext context);

    public void setUseCapture(boolean flag) {
        useCapture = flag;
    }

    public boolean isUseCapture() {
        return useCapture;
    }

    protected boolean regexMatch(java.util.regex.Matcher m, RateLimitContext context) {
        if (m.find()) {
            if (useCapture) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= m.groupCount(); i++) {
                    sb.append(m.group(i)).append("|");
                }
                if (sb.length() > 0) context.pushMatchCapture(sb.toString());
            }
            return true;
        }
        return false;
    }
}
