package com.android.util.db;

import com.android.util.log.LogUtil;

/**
 * @author 张全
 */
public class DBLog {
    private static final String TAG = "DBModel";

    public static void log(String msg) {
        LogUtil.d(TAG, msg);
    }
}
