package com.hhp227.knu_minigroup.dto;

import java.util.List;

public class ArticleItem {
    private boolean auth;
    private long timestamp;
    private String id, uid, name, title, content, image, date, replyCount;
    private List<String> images;

    public ArticleItem() {
    }

    public ArticleItem(String id, String uid, String name, String title, String content, String image, List<String> images, String date, String replyCount, boolean auth, long timestamp) {
        super();
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.title = title;
        this.content = content;
        this.image = image;
        this.images = images;
        this.date = date;
        this.replyCount = replyCount;
        this.auth = auth;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
