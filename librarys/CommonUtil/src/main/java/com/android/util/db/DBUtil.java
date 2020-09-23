package com.android.util.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * DB工具类
 *
 * @author 张全
 */
public final class DBUtil {

    public synchronized static int getInt(Cursor c, String column) {
        return c.getInt(c.getColumnIndex(column));
    }

    public synchronized static float getFloat(Cursor c, String column) {
        return c.getFloat(c.getColumnIndex(column));
    }

    public synchronized static long getLong(Cursor c, String column) {
        return c.getLong(c.getColumnIndex(column));
    }

    public synchronized static double getDouble(Cursor c, String column) {
        return c.getDouble(c.getColumnIndex(column));
    }

    public synchronized static String getString(Cursor c, String column) {
        return c.getString(c.getColumnIndex(column));
    }

    /**
     * 执行sql操作
     *
     * @param sql
     * @return
     */
    public synchronized static boolean executeSql(SQLiteDatabase db, String sql) {
        if (TextUtils.isEmpty(sql)) {
            return false;
        }
        boolean result = true;
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            closeDB(db, null);
        }
        return result;
    }

    /**
     * 关闭数据库
     *
     * @param db
     * @param c
     */
    public synchronized static void closeDB(SQLiteDatabase db, Cursor c) {
        try {
            if (null != db)
                db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (null != c)
                c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void closeDB(SQLiteDatabase db) {
        try {
            if (null != db)
                db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void closeCursor(Cursor c) {
        try {
            if (null != c)
                c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
