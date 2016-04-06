package com.github.srgg.aws.fhsender;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * Generic Text event
 */
public class Event {
    @JsonProperty("ts")
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    @JsonProperty("s")
    private String sender;

    @JsonProperty("d")
    private String data;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
