package com.android.util.date;

import android.annotation.SuppressLint;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期格式化工具类
 *
 * @author chen:
 */
public class DateFormatUtil {


    public static SimpleDateFormat yyyy__MM__dd() {
        return new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault());
    }

    public static SimpleDateFormat Z() {
        return new SimpleDateFormat("Z", Locale.getDefault());
    }

    public static SimpleDateFormat dd_HH_mm_ss_SSS() {
        return new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    }


    public static SimpleDateFormat yyyyMMdd() {
        return new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    }

    public static SimpleDateFormat yyyyMMddCh() {
        return new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss", Locale.getDefault());
    }

    public static SimpleDateFormat yyyyMMddHHmmss() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    }

    public static SimpleDateFormat yyyy_MM_dd_HH_mm_ss() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    public static SimpleDateFormat yyyy_MM_dd_HH_mm() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    public static SimpleDateFormat yyyy_MM_dd_HH_mm_Two(){
        return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
    }
    public static SimpleDateFormat dd_mm_hh_ss(){
        return new SimpleDateFormat("dd天HH:mm:ss");
    }
    public static SimpleDateFormat yyyy_MM_dd() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }
    public static SimpleDateFormat yyyy_MM_dd_two() {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
    }
    public static SimpleDateFormat yyyy_MM() {
        return new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
    }
    public static SimpleDateFormat yyyy_MM_Two() {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    }
    public static SimpleDateFormat MM_dd_HH_mm(){
        return new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
    }
    public static SimpleDateFormat HH_mm_ss() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    }

    public static SimpleDateFormat HHmmss() {
        return new SimpleDateFormat("HHmmss", Locale.getDefault());
    }

    public static SimpleDateFormat HH_mm() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public static SimpleDateFormat mm_ss() {
        return new SimpleDateFormat("mm:ss", Locale.getDefault());
    }

    public static SimpleDateFormat MMMM_dd_yyyy() {
        return new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
    }

    /**
     * 将时间格式字符串转换为时间
     *
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     * 将时间格式字符串转换为时间
     *
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate) {
        return strToDateLong(strDate, "yyyy-MM-dd");
    }

    /**
     * 将时间戳转换为字符串时间
     *
     * @param time
     * @param pattern
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String longToDateString(long time, String pattern) {
        return new SimpleDateFormat(pattern).format(new Date(time));
    }

    /**
     * 将时间格式字符串转换为星期几
     *
     * @param time
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String longToWeekString(long time) {
        String[] weeks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        try {
            Date date = new Date(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int week_index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (week_index < 0) {
                week_index = 0;
            }
            return weeks[week_index];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
