package com.hhp227.knu_minigroup.dto;

public class GroupItem {
    private int id;
    private boolean isAd, isAdmin;
    private String image, name, info, description, subscription;

    public GroupItem() {
    }

    public GroupItem(int id, boolean isAd, boolean isAdmin, String image, String name, String info, String description, String subscription) {
        this.id = id;
        this.isAd = isAd;
        this.isAdmin = isAdmin;
        this.image = image;
        this.name = name;
        this.info = info;
        this.description = description;
        this.subscription = subscription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}
