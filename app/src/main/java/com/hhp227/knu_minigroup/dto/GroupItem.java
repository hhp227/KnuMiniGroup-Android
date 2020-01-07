package com.hhp227.knu_minigroup.dto;

public class GroupItem {
    private boolean isAd, isAdmin, isJoined;
    private long timestamp;
    private String id, author, image, name, info, description, jointype;

    public GroupItem() {
    }

    public GroupItem(String id, boolean isAd, boolean isAdmin, boolean isJoined, long timestamp, String author, String image, String name, String info, String description, String jointype) {
        this.id = id;
        this.isAd = isAd;
        this.isAdmin = isAdmin;
        this.isJoined = isJoined;
        this.timestamp = timestamp;
        this.author = author;
        this.image = image;
        this.name = name;
        this.info = info;
        this.description = description;
        this.jointype = jointype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJoinType() {
        return jointype;
    }

    public void setJoinType(String joinType) {
        this.jointype = joinType;
    }
}
