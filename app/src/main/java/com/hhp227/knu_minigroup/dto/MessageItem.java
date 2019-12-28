package com.hhp227.knu_minigroup.dto;

public class MessageItem {
    private String from;
    private String name;
    private String message;
    private boolean seen;
    private long timestamp;

    public MessageItem() {
    }

    public MessageItem(String from, String name, String message, boolean seen, long timestamp) {
        this.from = from;
        this.name = name;
        this.message = message;
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
