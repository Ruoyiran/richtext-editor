package com.royran.rteditor.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    @SuppressLint("SimpleDateFormat")
    public static String formatDateMinute(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        return formatter.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateCleanMillis(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmSSS");
        return formatter.format(date);
    }
}
