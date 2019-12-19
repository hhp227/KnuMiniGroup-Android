package com.hhp227.knu_minigroup.dto;

public class MessageItem {
    private String from;
    private String to;
    private String name;
    private String message;
    private long time;

    public MessageItem() {
    }

    public MessageItem(String from, String to, String name, String message, long time) {
        this.from = from;
        this.to = to;
        this.name = name;
        this.message = message;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
