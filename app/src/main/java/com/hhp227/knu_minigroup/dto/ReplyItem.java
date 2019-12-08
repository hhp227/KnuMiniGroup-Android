package com.hhp227.knu_minigroup.dto;

public class ReplyItem {
    private int id, userId;
    private boolean auth;
    private String name, profileImg, timeStamp, reply;

    public ReplyItem() {
    }

    public ReplyItem(int id, int userId, boolean auth, String name, String profileImg, String timeStamp, String reply) {
        this.id = id;
        this.userId = userId;
        this.auth = auth;
        this.name = name;
        this.profileImg = profileImg;
        this.timeStamp = timeStamp;
        this.reply = reply;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
