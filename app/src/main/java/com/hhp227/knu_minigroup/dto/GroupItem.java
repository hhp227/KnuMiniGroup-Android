package com.hhp227.knu_minigroup.dto;

public class GroupItem {
    private boolean isAd, isAdmin, isJoined;
    private long timestamp;
    private String id, image, name, info, description, subscription, joinType;

    public GroupItem() {
    }

    public GroupItem(String id, boolean isAd, boolean isAdmin, boolean isJoined, long timestamp, String image, String name, String info, String description, String subscription, String joinType) {
        this.id = id;
        this.isAd = isAd;
        this.isAdmin = isAdmin;
        this.isJoined = isJoined;
        this.timestamp = timestamp;
        this.image = image;
        this.name = name;
        this.info = info;
        this.description = description;
        this.subscription = subscription;
        this.joinType = joinType;
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

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }
}
