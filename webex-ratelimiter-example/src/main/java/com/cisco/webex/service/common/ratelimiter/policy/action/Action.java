package com.cisco.webex.service.common.ratelimiter.policy.action;

import com.cisco.webex.service.common.ratelimiter.policy.Policy;
import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Action.Allow.class, name = "allow"),
        @JsonSubTypes.Type(value = Action.Deny.class, name = "deny"),
        @JsonSubTypes.Type(value = RateAction.class, name = "rate"),
})
public abstract class Action {

    protected static final Logger LOG = LoggerFactory.getLogger(Action.class);
    protected static final boolean DEBUG = true;
    protected String denyReason;
    protected Map<String, String> headers;

    @JsonIgnore
    private Policy parentPolicy;

    public enum Status {
        EVAL_NEXT,
        EVAL_SKIP
    }

    public Action() {
    }

    public abstract String getId();

    @JsonProperty("denyReason")
    public void setDenyReason(String denyReason) {
        this.denyReason = denyReason;
    }

    @JsonProperty("headers")
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setPolicy(Policy policy) {
        this.parentPolicy = policy;
    }

    public Policy getPolicy() {
        return parentPolicy;
    }

    protected String keyFor(RateLimitContext context, String prefix) {
        String captures = context.popMatchCaptures();
        LOG.info("captures :", captures);
        if (captures.length() > 0) {
            return String.format("%s:%s:%s", prefix, getId(), captures);
        } else {
            return String.format("%s:%s", prefix, getId());
        }
    }

    /**
     * Evaluates a policy
     * @return an Enforcement object identifying status and whether evaluation should proceed
     */
    public abstract Enforcement enforce(RateLimitContext context, String keyPrefix);

    /**
     * Perform cleanup (usually after a request is done execution)
     * Note that the method gets the entire key as an argument (not just the prefix)
     */
    public void release(RateLimitContext context, String key) {}

    public abstract static class ConfigurableAssertAction extends Action {
        protected boolean isAssertEnabled;
        public ConfigurableAssertAction() {
            super();
            this.isAssertEnabled = true;
        }

        public boolean isAssertEnabled() {
            return isAssertEnabled;
        }

        @JsonProperty("assert")
        public void setAssertEnabled(boolean enableAssert) {
            this.isAssertEnabled = enableAssert;
        }

    }

    public static class Allow extends ConfigurableAssertAction {

        @Override
        public String getId() {
            return "allow";
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
           if(context.isDebugEnabled()) LOG.info("ALLOW asserted " + toString());
           return Enforcement.forceAllow(getPolicy());
        }
    }



    public static class Deny extends ConfigurableAssertAction {

        private static final String DEFAULT_DENY_REASON = "Deny policy affect.";

        @JsonCreator
        public Deny(
                @JsonProperty("denyCode") int code) {
            this.denyCode = (code > 0 ? code : 429);
            this.denyReason = DEFAULT_DENY_REASON;
        }

        private final int denyCode;
        @Override
        public String getId() {
            return "deny";
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
            if(context.isDebugEnabled()) LOG.info("DENY asserted " + toString());
            return Enforcement.deny(getPolicy(), denyCode, headers, denyReason, -1 , 3600);
        }
    }



    public static class Enforcement {
        private final Status status;
        private final boolean isPass;
        private final int code;
        private final Map<String, String> headers;
        private final String message;
        private final Policy policy;
        private final long limit;
        private final int reset;
        private RateLimitContext context;

        private Enforcement(Status s, boolean isPass, Policy policy, int code, Map<String, String> headers, String msg, long limit, int reset) {
            this.status = s;
            this.isPass = isPass;
            this.policy = policy;
            this.code = code;
            this.headers = headers;
            this.message = msg;
            this.limit = limit;
            this.reset = reset;
        }

        public static Enforcement forceAllow(Policy policy) {
            return new Enforcement(Status.EVAL_SKIP, true, policy, -1, null,null, -1, -1);
        }

        public static Enforcement next(Policy policy) {
            checkNotNull(policy, "policy must not be null");
            return new Enforcement(Status.EVAL_NEXT, true, policy, -1, null,null, -1, -1);
        }

        public static Enforcement deny(String message, Policy policy, long limit, int reset) {
            return deny(message, null, policy, limit, reset);
        }

        public static Enforcement deny(String message, Map<String, String> headers, Policy policy, long limit, int reset) {
            return deny(policy, -1, headers, message, limit, reset);
        }

        public static Enforcement deny(Policy policy, int denyCode, Map<String, String> headers, String message, long limit, int reset) {
            checkNotNull(message, "message must not be null");
            checkNotNull(policy, "policy must not be null");
            return new Enforcement(Status.EVAL_SKIP, false, policy, denyCode, headers, message, limit, reset);
        }

        public Status getStatus() {
            return status;
        }

        public boolean isPass() {
            return isPass;
        }

        public Policy getPolicy() {
            return policy;
        }

        public long getLimit() {
            return limit;
        }

        public long getReset() {
            return reset;
        }

        public String getMessage() {
            return message;
        }

        public int getCode() {
            return code;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setContext(RateLimitContext context) {
            this.context = context;
        }

        public RateLimitContext getContext() {
            return context;
        }
    }

}
