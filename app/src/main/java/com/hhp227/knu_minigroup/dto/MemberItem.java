package com.hhp227.knu_minigroup.dto;

public class MemberItem {
    private String name, profileImg;

    public MemberItem(String name, String profileImg) {
        this.name = name;
        this.profileImg = profileImg;
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
}
