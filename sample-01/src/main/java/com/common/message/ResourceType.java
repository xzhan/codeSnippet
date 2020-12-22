package com.common.message;

/**
 * @see <a href="https://wiki.cisco.com/x/iGCSCw">WNS - Kafka Message Channel Telemetry Definition</a>
 */
public enum ResourceType {
    MEETING("meeting"),
    USER("user"),
    CONTACT("contact"),
    RECORDING("recording"),
    CONFERENCE("conference"),
    TRANSCRIPT("transcript"),
    MEETING_INSTANCE("meetinginstance"),
    CRASH_LOG("crashlog");

    private String value;

    ResourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
