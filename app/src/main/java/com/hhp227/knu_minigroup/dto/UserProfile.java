package com.hhp227.knu_minigroup.dto;

public class UserProfile {
    String imageId, messageId;

    public UserProfile() {
    }

    public UserProfile(String imageId, String messageId) {
        this.imageId = imageId;
        this.messageId = messageId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
