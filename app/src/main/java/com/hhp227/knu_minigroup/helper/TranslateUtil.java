package com.hhp227.knu_minigroup.helper;

import android.annotation.SuppressLint;

@SuppressLint("unused")
public class TranslateUtil {
    private static String translate(String dateString) {
        if (dateString.contains("오전")) {
            return dateString.replace("오전", "AM");
        } else if (dateString.contains("오후")) {
            return dateString.replace("오후", "PM");
        } else {
            return dateString;
        }
    }
}
