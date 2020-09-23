package com.android.util.ext;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.util.LContext;

/**
 * 项目公用的SharePrefrence，其他模块只需要定义自己的常量key即可
 *
 * @author 张全
 */
public class SPUtil {
    public static final String SP_NAME = "SPUtil";

    private static Context getContext() {
        return LContext.getContext();
    }

    public static SharedPreferences getSP() {
        return getContext().getSharedPreferences(SP_NAME, Context.MODE_APPEND);
    }

    public static void setString(String key, String val) {
        SharedPreferences pref = getSP();
        Editor edit = pref.edit();
        edit.putString(key, val);
        edit.commit();
    }

    public static void setBoolean(String key, boolean val) {
        SharedPreferences pref = getSP();
        Editor edit = pref.edit();
        edit.putBoolean(key, val);
        edit.commit();
    }

    public static void setFloat(String key, float val) {
        SharedPreferences pref = getSP();
        Editor edit = pref.edit();
        edit.putFloat(key, val);
        edit.commit();
    }

    public static void setInt(String key, int val) {
        SharedPreferences pref = getSP();
        Editor edit = pref.edit();
        edit.putInt(key, val);
        edit.commit();
    }

    public static void setLong(String key, long val) {
        SharedPreferences pref = getSP();
        Editor edit = pref.edit();
        edit.putLong(key, val);
        edit.commit();
    }

    public static String getString(String key) {
        SharedPreferences pref = getSP();
        return pref.getString(key, null);
    }

    public static String getString(String key, String def) {
        SharedPreferences pref = getSP();
        return pref.getString(key, def);
    }

    public static boolean getBoolean(String key) {
        SharedPreferences pref = getSP();
        return pref.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean def) {
        SharedPreferences pref = getSP();
        return pref.getBoolean(key, def);
    }

    public static int getInt(String key) {
        SharedPreferences pref = getSP();
        return pref.getInt(key, -1);
    }

    public static int getInt(String key, int def) {
        SharedPreferences pref = getSP();
        return pref.getInt(key, def);
    }

    public static long getLong(String key) {
        SharedPreferences pref = getSP();
        return pref.getLong(key, -1);
    }

    public static long getLong(String key, long def) {
        SharedPreferences pref = getSP();
        return pref.getLong(key, def);
    }

    public static float getFloat(String key) {
        SharedPreferences pref = getSP();
        return pref.getFloat(key, -1);
    }

    public static float getFloat(String key, float def) {
        SharedPreferences pref = getSP();
        return pref.getFloat(key, def);
    }

    public static void remove(String key) {
        SharedPreferences sp = getSP();
        sp.edit().remove(key).commit();
    }
}
