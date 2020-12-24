package com.cisco.webex.service.common.ratelimiter.policy.matcher;

import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright 2015 Cisco Inc. All rights reserved.
 */
public class EndPointMatcher extends Matcher {

    private final String method;
    private final Pattern url;
    private final String headerName;
    private final Pattern headerValue;
    private final String paramName;
    private final Pattern paramValue;

    @JsonCreator
    public EndPointMatcher(
            @JsonProperty("method") String method,
            @JsonProperty("url") String url,
            @JsonProperty("header") String headerName,
            @JsonProperty("headerValue") String headerValue,
            @JsonProperty("param") String paramName,
            @JsonProperty("paramValue") String paramValue
    ) {
        checkNotNull(url);
        this.method = method;
        this.url = (url == null ? null : Pattern.compile(url));
        this.headerName = headerName;
        this.headerValue = (headerValue == null ? null : Pattern.compile(headerValue));
        this.paramName = paramName;
        this.paramValue = (paramValue == null ? null : Pattern.compile(paramValue));

        if (paramName != null) {
            checkNotNull(paramValue);
        }

        if (headerName != null) {
            checkNotNull(headerValue);
        }
    }

    public EndPointMatcher(String method, String url) {
        this(method, url, null, null, null, null);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EndPointMatcher{");
        sb.append("method='").append(method).append('\'');
        sb.append(", url=").append(url);
        sb.append(", headerName='").append(headerName).append('\'');
        sb.append(", headerValue=").append(headerValue);
        sb.append(", paramName='").append(paramName).append('\'');
        sb.append(", paramValue=").append(paramValue);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean matches(RateLimitContext context) {
        HttpServletRequest request = context.getRequest();
        if (method != null && !request.getMethod().equalsIgnoreCase(method)) {
            return false;
        }
        if (url != null && !regexMatch(url.matcher(request.getRequestURI()), context)) {
            return false;
        }
        if (headerName != null && headerValue != null) {
            String value = request.getHeader(headerName);
            if (value == null) value = "__NULL__";
            if (!regexMatch(headerValue.matcher(value), context)) return false;
        }

        if (paramName != null && paramValue != null) {
            String value = request.getParameter(paramName);
            if (value == null) value = "__NULL__";
            if (!regexMatch(paramValue.matcher(value), context)) return false;
        }
        return true;
    }

}
