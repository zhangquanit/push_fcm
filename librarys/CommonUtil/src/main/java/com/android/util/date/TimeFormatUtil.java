package com.android.util.date;

import android.text.TextUtils;

import java.util.Locale;

/**
 * 时间格式化工具类
 */
public final class TimeFormatUtil {
    private TimeFormatUtil() {
    }

    ;

    /**
     * 格式化毫秒數
     *
     * @param duration 毫秒数
     * @return 格式化后的数据
     */
    public static String formatMillSecondsToStr(int duration) {
        duration /= 1000;
        int minute = duration / 60;
        int second = duration % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
    }

    public static String formatMillSecondsToSecond(int duration) {
        duration /= 1000;
        int second = duration % 60;
        return String.format(Locale.getDefault(), "%02d", second);
    }

    /**
     * 格式化秒数
     *
     * @param duration
     * @return
     */
    public static String formatSecondsToStr(int duration) {
        int minute = duration / 60;
        int hour = minute / 60;
        if (hour > 0) {// 大于0证明有小时 此时min求余
            minute = minute % 60;
        }
        int second = duration % 60;

        String result = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        return result;
    }

    /**
     * 返回2012-08-07 18:20格式
     *
     * @param time 格式：20120807
     * @return
     */
    public static String formatServerTime(String time) {
        if (time != null && time.length() > 12) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(time.substring(0, 4));// year
            buffer.append("-");
            buffer.append(time.substring(4, 6));// month
            buffer.append("-");
            buffer.append(time.substring(6, 8));// day
            buffer.append(" ");
            buffer.append(time.substring(8, 10));// hour
            buffer.append(":");
            buffer.append(time.substring(10, 12));// min
            return buffer.toString();
        }
        return null;
    }

    /**
     * 格式化时间为 00:00:00(小时:分钟:秒)或者00:00(分钟:秒)
     *
     * @param duration 秒为单位
     * @return
     */
    public static String formatSeekbarTime(int duration) {
        int minute = 0;
        int hour = 0;
        int second = 0;
        String result = null;
        if (duration >= 60 * 60) {// 小时
            minute = duration / 60;
            hour = minute / 60;
            minute = minute % 60;
            second = duration % 60;
            result = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
        } else {// 分钟
            minute = duration / 60;
            second = duration % 60;
            result = String.format(Locale.getDefault(), "%02d:%02d", minute, second);
        }
        return result;
    }

    /**
     * 秒转化为分
     *
     * @param seconds
     * @return
     */
    public static int secondsToMin(String seconds) {
        if ("null".equals(seconds))
            return 0;
        if (!TextUtils.isEmpty(seconds)) {
            int ss = Integer.valueOf(seconds);
            if (ss >= 60)
                return (ss + 59) / 60;
            else
                return 1;
        }
        return 0;
    }

}
