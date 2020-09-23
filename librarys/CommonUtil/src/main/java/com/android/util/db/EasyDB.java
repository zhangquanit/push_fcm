package com.android.util.db;

/**
 * @author 张全
 */
public class EasyDB {

    public static <T> DBSession<T> with(Class<T> cls) {
        return new DBSession<T>(cls);
    }

    public static <T> DBSession<T> with(Class<T> cls, String uid) {
        return new DBSession<T>(cls, uid);
    }
}
