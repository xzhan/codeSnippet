package com.cisco.webex.service.common.ratelimiter.policy.action;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class TimeInterval {
    private static final Pattern TIME_UNITS = Pattern.compile("([0-9]+)(ms|s|m|h|d)");
    /**
     * ([0-9]+)(w|d|h|m|s|ms)
     */
    public static long parseToMillis(String intervalAndUnit) {
        Matcher matcher = TIME_UNITS.matcher(intervalAndUnit);
        if (matcher.find()) {
            TimeUnit unit = toTimeUnit(matcher.group(2));
            return unit.toMillis(Long.parseLong(matcher.group(1)));
        }
        throw new IllegalArgumentException("Time interval not recognized -'" + intervalAndUnit + "'");
    }

    private static TimeUnit toTimeUnit(String unit) {
        checkNotNull(unit);
        if (unit.equals("ms")) return TimeUnit.MILLISECONDS;
        else if (unit.equals("s") || unit.indexOf("sec") > -1) return TimeUnit.SECONDS;
        else if (unit.equals("m") || unit.indexOf("min") > -1) return TimeUnit.MINUTES;
        else if (unit.equals("h") || unit.indexOf("hour") > -1) return TimeUnit.HOURS;
        else if (unit.equals("d") || unit.indexOf("day") > -1) return TimeUnit.DAYS;
        throw new IllegalArgumentException("Time unit format not recognized -'" + unit +
                "'. Supported types are 'ms,s,sec,m,min,h,hour,d,day'");
    }

}