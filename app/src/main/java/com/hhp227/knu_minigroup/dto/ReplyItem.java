package com.hhp227.knu_minigroup.dto;

public class ReplyItem {
    private int id, user_id;
    private boolean auth;
    private String name, profile_img, timestamp, reply;

    public ReplyItem() {
    }

    public ReplyItem(int id, int user_id, String name, String profile_img, String timestamp, String reply, boolean auth) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.profile_img = profile_img;
        this.timestamp = timestamp;
        this.reply = reply;
        this.auth = auth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }
}
