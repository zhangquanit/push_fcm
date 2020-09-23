package com.sndo.fcm.demo4;

import android.text.TextUtils;


import com.android.util.ext.SPUtil;
import com.android.util.log.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 张全
 */
public class FcmUtil {
    private static final String TAG = "Push";
    private static final String TOKEN = "FCM_TOKEN";
    private static final String MSG_IDS = "MSG_IDS";

    public static void log(String msg) {
        LogUtil.d(TAG, msg);
    }

    public static void saveToken(String token) {
        SPUtil.setString(TOKEN, token);
    }

    public static String getToken() {
        return SPUtil.getString(TOKEN);
    }

    public static void saveMsgId(String id) {
        String msgIds = SPUtil.getString(MSG_IDS);
        if (!TextUtils.isEmpty(msgIds)) {
            msgIds += "," + id;
        } else {
            msgIds = id;
        }
        SPUtil.setString(MSG_IDS, msgIds);
    }

    public static List<String> getMsgIds() {
        List<String> ids = new ArrayList();
        String value = SPUtil.getString(MSG_IDS);
        if (!TextUtils.isEmpty(value)) {
            String[] items = value.split(",");
            ids = Arrays.asList(items);
        }
        return ids;
    }



}
