package com.hhp227.knu_minigroup.helper;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd a h:mm:ss");

    public static long getTimeStamp(String dateString) throws ParseException {
        Date date = dateFormat.parse(dateString);
        return date != null ? date.getTime() : 0;
    }

    public static String getDateString(long timeStamp) {
        return dateFormat.format(timeStamp);
    }

    public static String getCalendarStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = "";

        try {
            Date date = format.parse(dateStr);
            format = new SimpleDateFormat("dd");
            String date1 = format.format(date);
            timestamp = date1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }
}