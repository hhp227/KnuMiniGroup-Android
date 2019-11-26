package com.hhp227.knu_minigroup.dto;

public class GroupItem {
    private int id;
    private String image, name, info;

    public GroupItem() {
    }

    public GroupItem(int id, String image, String name, String info) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
