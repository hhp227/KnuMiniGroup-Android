package com.hhp227.knu_minigroup.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.hhp227.knu_minigroup.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    // 타임 제네레이터
    public static String getPeriodTime(Context context, String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.format_date2));
        Date date;

        df.setTimeZone(TimeZone.getDefault());
        if (TextUtils.isEmpty(strDate))
            return "";
        try {
            date = df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        assert date != null;
        return sdf.format(date);
    }
}