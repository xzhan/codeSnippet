package com.cisco.webex.service.common.ratelimiter.policy;

import com.cisco.webex.service.common.ratelimiter.RateLimitContext;
import com.cisco.webex.service.common.ratelimiter.policy.action.Action;
import com.cisco.webex.service.common.ratelimiter.policy.action.RateAction;
import com.cisco.webex.service.common.ratelimiter.policy.matcher.Matcher;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Policy {

    private static final Logger LOG = LoggerFactory.getLogger(Policy.class);



    private final String name;
    private final String description;
    private final boolean enforce;
    private final List<Matcher> matchers = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private Action assertAction;
    private boolean hasDatacentreAction = false;


    @JsonCreator
    public Policy(
            @JsonProperty("name") String name,
            @JsonProperty("description") String desc,
            @JsonProperty("enforce")  String enforce,
            @JsonProperty("match") List<Matcher> matchers,
            @JsonProperty("action") List<Action> actions)
    {
        checkNotNull(name);
        this.name = name;
        this.description = desc;
        this.enforce = (enforce != null ? Boolean.valueOf(enforce)  : true);
        addEnforcers(actions);
        addMatchers(matchers);
        validate();
    }

    private void validate() {
    }

    private void addEnforcers(Collection<Action> actions) {
        if (actions == null) return;
        for(Action e : actions) {
            if (isAssertEnabledAction(e)) {
                setAssertAction(e);
            } else {
                if(e instanceof RateAction && ((RateAction)e).hasDatacentreScope()) {
                    this.hasDatacentreAction = true;
                }
                this.actions.add(e);
            }
            e.setPolicy(this);
        }
    }

    private boolean isAssertEnabledAction(Action e) {
        return e instanceof Action.ConfigurableAssertAction && ((Action.ConfigurableAssertAction) e).isAssertEnabled();
    }

    private void addMatchers(Collection<Matcher> policySet) {
        if (policySet == null) return;
        matchers.addAll(policySet);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getEnforce() {
        return enforce;
    }

    public List<Matcher> getMatchers() {
        return matchers;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Action getAssertAction() {
        return assertAction;
    }

    public void setAssertAction(Action assertAction) {
        this.assertAction = assertAction;
    }

    public boolean hasDataCentreWideAction() {
        return hasDatacentreAction;
    }

    public boolean enforce() { return enforce; }

    public Action.Enforcement enforce(RateLimitContext context) {
        if (context.isDebugEnabled()) LOG.info("POLICY:ENFORCE: " + context + " " + toString());
        try {
            for (Matcher matcher : matchers) {
                if (!matcher.matches(context)) {
                    if (context.isDebugEnabled()) LOG.info("POLICY:MATCH:FAIL " + matcher.toString());
                    return Action.Enforcement.next(this);
                }
                if (context.isDebugEnabled()) LOG.info("POLICY:MATCH:OK " + matcher.toString());
            }
            if (assertAction != null) {
                Action.Enforcement result = assertAction.enforce(context, name);
                if (context.isDebugEnabled()) LOG.info("POLICY:ASSERT:" + (result.isPass() ? "PASS" : "FAIL")
                        + " " + assertAction.toString());
                if (result.getStatus() == Action.Status.EVAL_SKIP) {
                    return result;
                }
            }
            for (Action action : actions) {
                Action.Enforcement result = action.enforce(context, name);
                if (context.isDebugEnabled()) LOG.info("POLICY:ACTION:" + (result.isPass() ? "PASS" : "FAIL")
                        + " " + action.toString());
                if (result.getStatus() == Action.Status.EVAL_SKIP) {
                    return result;
                }
            }
            return Action.Enforcement.next(this);
        } catch(Exception e) {
            LOG.warn("Unexpected error evaluating policy:{} Policy will be skipped for current request.", name);
            return Action.Enforcement.next(this);
        } finally {
            context.popMatchCaptures();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Policy{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", enforce=").append(enforce);
        sb.append(", matchers=").append(matchers);
        sb.append(", actions=").append(actions);
        sb.append(", assertAction=").append(assertAction);
        sb.append('}');
        return sb.toString();
    }

    public static Builder builder(String name){
        return new Builder(name);
    }

    public static final class Builder {
        private String name;
        private String description;
        private boolean enforce = true;
        private boolean dist = false;
        private List<Action> actions = new ArrayList<>();
        private List<Matcher> matchers = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder enforce(boolean on) {
            this.enforce = on;
            return this;
        }
        public Builder matchers(List<Matcher> matchers) {
            this.matchers.addAll(matchers);
            return this;
        }
        public Builder matcher(Matcher m) {
            this.matchers.add(m);
            return this;
        }
        public Builder action(Action e) {
            this.actions.add(e);
            return this;
        }
        public Builder deny() {
            this.actions.add(new Action.Deny(429));
            return this;
        }
        public Builder deny(int code, String msg) {
            return deny(code, null, msg);
        }
        public Builder deny(int code, Map<String, String> headers, String msg) {
            Action action = new Action.Deny(code);
            action.setDenyReason(msg);
            action.setHeaders(headers);
            this.actions.add(action);
            return this;
        }
        public Builder allow() {
            this.actions.add(new Action.Allow());
            return this;
        }
        public Builder actions(List<Action> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public Policy build() {
            return new Policy(name, description, Boolean.toString(enforce), matchers, actions);
        }
    }

}

