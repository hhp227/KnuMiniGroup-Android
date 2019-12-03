package com.hhp227.knu_minigroup.dto;

import android.graphics.Bitmap;

public class MemberItem {
    public String name, messageValue;
    public Bitmap profileImg;

    public MemberItem(String name, Bitmap profileImg, String messageValue) {
        this.name = name;
        this.profileImg = profileImg;
        this.messageValue = messageValue;
    }
}
