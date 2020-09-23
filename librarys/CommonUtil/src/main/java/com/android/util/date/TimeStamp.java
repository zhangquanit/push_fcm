package com.android.util.date;

import java.util.Date;

/**
 * 时间戳(秒)
 */
public class TimeStamp {

    /* 时间差值(秒) */
    private static long TimeStampDiff = 0;

    private TimeStamp() {
    }

    public static long getTimeStampDiff() {
        return TimeStampDiff;
    }

    /**
     * 获得本地时间(秒)
     */
    private static long getLocalTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获得秒数
     *
     * @return long
     */
    public synchronized static long toSeconds() {
        return getLocalTime() - TimeStampDiff;
    }

    public synchronized static long toLong() {
        return toSeconds() * 1000;
    }

    /**
     * 获得秒数
     *
     * @return int
     */
    public synchronized static int toInt() {
        return (int) toSeconds();
    }

    /**
     * 获得日期 Date
     *
     * @return Date
     */
    public synchronized static Date toDate() {
        return new Date(toSeconds() * 1000L);
    }

    /**
     * 矫正时间戳(秒)
     *
     * @param timeStamp
     */
    public synchronized static void correction(long timeStamp) {
        TimeStampDiff = getLocalTime() - timeStamp;
    }

    public static Date getCurrentDate() {
        return toDate();
    }
}
