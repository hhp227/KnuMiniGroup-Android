package com.hhp227.knu_minigroup.dto;

import java.io.Serializable;

public class User implements Serializable {
    String userId, password, imageId;

    public User() {
    }

    public User(String userId, String password, String imageId) {
        this.userId = userId;
        this.password = password;
        this.imageId = imageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
