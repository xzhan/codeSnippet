package com.common.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String topic;
    private String key;
    private String value;

    @JsonCreator
    public Message(@JsonProperty("topic") String topic,@JsonProperty("key") String key,@JsonProperty("value") String value )
    {
        this.topic = topic;
        this.key = key;
        this.value = value;
    }

    public Message() {

    }
}
