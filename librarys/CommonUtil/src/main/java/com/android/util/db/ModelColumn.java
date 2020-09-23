package com.android.util.db;

/**
 * @author 张全
 */
public interface ModelColumn {
    /**
     * 用户id
     */
    String uid = "uid";
    /**
     * 该条数据的唯一key
     */
    String key = "key";
    /**
     * 字段的value
     */
    String value = "value";
    /**
     * 功能模块
     */
    String model = "model";
}
