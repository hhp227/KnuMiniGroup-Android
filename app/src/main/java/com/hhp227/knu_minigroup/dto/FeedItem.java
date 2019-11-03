package com.hhp227.knu_minigroup.dto;

public class FeedItem {
    int id;
    private String name, content, image, timeStamp, replyCount;

    public FeedItem() {
    }

    public FeedItem(int id, String name, String content, String image, String timeStamp, String replyCount) {
        super();
        this.id = id;
        this.name = name;
        this.content = content;
        this.image = image;
        this.timeStamp = timeStamp;
        this.replyCount = replyCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }
}
