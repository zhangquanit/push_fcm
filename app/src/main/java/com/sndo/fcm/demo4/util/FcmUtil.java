package com.sndo.fcm.demo4.util;

import android.util.Log;


/**
 * @author 张全
 */
public class FcmUtil {
    private static final String TAG = "Push";
//    private static final String TOKEN = "FCM_TOKEN";

    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    public static void saveToken(String token) {
//        SPUtil.setString(TOKEN, token);
    }

//    public static String getToken() {
//        return SPUtil.getString(TOKEN);
//    }

}
