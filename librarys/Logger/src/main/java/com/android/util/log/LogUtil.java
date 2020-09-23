package com.android.util.log;

import android.util.Log;

/**
 * 日志
 */
public final class LogUtil {
    public static boolean isOpen = true; // 是否开启日志
    private static final String TAG = "LogUtil";

    private LogUtil() {

    }

    public static void openLog(boolean open) {
        LogUtil.isOpen = open;
        Logger.logEnabled(open);
    }


    public static void d(String msg) {
        if (isOpen)
            Log.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (isOpen)
            Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable e) {
        if (isOpen)
            Log.d(tag, msg, e);
    }

    public static void i(String msg) {
        if (isOpen)
            Log.i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (isOpen)
            Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable e) {
        if (isOpen)
            Log.i(tag, msg, e);
    }

    public static void w(String msg) {
        if (isOpen)
            Log.w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (isOpen)
            Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable e) {
        if (isOpen)
            Log.w(tag, msg, e);
    }

    public static void e(String msg) {
        if (isOpen)
            Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (isOpen)
            Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (isOpen)
            Log.e(tag, msg, e);
    }

    public static void e(Throwable e) {
        if (isOpen) Log.e(TAG, "message", e);
    }
}
