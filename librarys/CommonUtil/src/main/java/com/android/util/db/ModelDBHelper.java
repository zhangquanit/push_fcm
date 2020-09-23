package com.android.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author 张全
 */
public class ModelDBHelper extends SQLiteOpenHelper {
    public static final String DB = "model.db";//数据库名
    public static final String TABLE = "model";//表名

    /**
     * 数据库版本号
     */
    private static final int VERSION = 1;

    public ModelDBHelper(Context ctx) {
        super(ctx, DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sql;
        sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS [" + TABLE + "] ( \n");
        sql.append(ModelColumn.uid + " INTEGER, \n");// 用户id
        sql.append(ModelColumn.key + " VARCHAR,\n");//
        sql.append(ModelColumn.value + " VARCHAR,\n");//
        sql.append(ModelColumn.model + " VARCHAR");
        sql.append(") \n");
        db.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
