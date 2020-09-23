package com.android.util.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.util.LContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 张全
 */
public class ModelDao {
    public static String TABLE = ModelDBHelper.TABLE;
    private static final String QUERY_ALL = "select * from " + TABLE;
    private static final String WHERE_CLAUSE = ModelColumn.uid + "=? and "
            + ModelColumn.key + "=?";

    private synchronized static SQLiteDatabase getDB() {
        return new ModelDBHelper(LContext.getContext()).getWritableDatabase();
    }

    public synchronized static List<String> getByColumn(
            String column, String value) {
        String sql = QUERY_ALL + " where " + column + " ='" + value + "' ";
        return get(sql);
    }

    public synchronized static List<String> getByColumns(
            String[] cloumns, String[] values) {
        final String and = " and ";
        StringBuffer sql = new StringBuffer(QUERY_ALL);
        sql.append(" where ");
        for (int i = 0; i < cloumns.length; i++) {
            sql.append(cloumns[i] + "='" + values[i] + "'" + and);
        }
        sql = sql.delete(sql.length() - and.length(), sql.length());
        return get(sql.toString());
    }

    public synchronized static List<String> getUserModels(String uid, String name) {
        String[] columns = {ModelColumn.uid, ModelColumn.model};
        String[] values = {uid, name};
        return getByColumns(columns, values);
    }

    public synchronized static List<String> get(String sql) {
        List<String> modelList = new ArrayList<>();

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = getDB();
            c = db.rawQuery(sql, null);
            while (c.moveToNext()) {
                String value = DBUtil.getString(c, ModelColumn.value);
                modelList.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeDB(db, c);
        }
        return modelList;
    }

    public synchronized static boolean save(DBModel model) {
        if (null == model) {
            return false;
        }

        List<DBModel> modelList = new ArrayList<DBModel>();
        modelList.add(model);
        return save(modelList);
    }

    public synchronized static boolean save(List<? extends DBModel> modelList) {
        if (null == modelList || modelList.isEmpty()) {
            return false;
        }
        boolean result = true;
        SQLiteDatabase db = null;
        try {
            db = getDB();
            // 使用事务机制 提高插入速度
            db.beginTransaction();
            int size = modelList.size();
            for (int i = 0; i < size; i++) {
                DBModel model = modelList.get(i);
                db.delete(
                        TABLE,
                        WHERE_CLAUSE,
                        new String[]{model.uid,
                                model.key});

                db.insert(TABLE, null, getValue(model));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (null != db)
                db.endTransaction();
            DBUtil.closeDB(db, null);
        }
        return result;
    }

    public synchronized static boolean update(DBModel model) {
        int result = 0;
        SQLiteDatabase db = null;
        try {
            db = getDB();
            result = db.update(
                    TABLE,
                    getValue(model),
                    WHERE_CLAUSE,
                    new String[]{model.uid,
                            model.key});
        } catch (Exception e) {
            e.printStackTrace();
            result = 0;
        } finally {
            DBUtil.closeDB(db, null);
        }
        return result == 1 ? true : false;
    }

    public synchronized static boolean deleteUserModels(String uid,
                                                        String modelName) {
        String[] columns = {ModelColumn.uid, ModelColumn.model};
        String[] values = {uid, modelName};
        return deleteByColumns(columns, values);
    }

    public synchronized static boolean delete(DBModel model) {
        return deleteByColumns(
                new String[]{ModelColumn.uid, ModelColumn.model, ModelColumn.key},
                new String[]{model.uid, model.name, model.key});
    }

    public synchronized static boolean delete(String uid, String modelName, List<String> keys) {
        boolean result = true;
        SQLiteDatabase db = null;
        try {
            db = getDB();
            StringBuffer sql = new StringBuffer();
            sql.append("delete from " + TABLE)
                    .append(" where ")
                    .append(ModelColumn.uid).append("='" + uid + "'")
                    .append(" and ")
                    .append(ModelColumn.model).append("='" + modelName + "'")
                    .append(" and ")
                    .append(ModelColumn.key).append(" in (");
            for (String key : keys) {
                sql.append("'" + key + "'").append(",");
            }
            sql = sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
            db.execSQL(sql.toString());
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            DBUtil.closeDB(db, null);
        }
        return result;

    }

    public synchronized static <T> boolean deleteByColumn(String column,
                                                          String value) {
        return deleteByColumns(new String[]{column}, new String[]{value});
    }

    public synchronized static boolean deleteByColumns(String[] columns,
                                                       String[] values) {
        boolean result = true;
        SQLiteDatabase db = null;
        try {
            db = getDB();
            StringBuffer whereClause = new StringBuffer();
            whereClause.append(columns[0]).append("=?");
            if (columns.length > 1) {
                for (int i = 1; i < columns.length; i++) {
                    whereClause.append(" and ").append(columns[i]).append("=?");
                }
            }
            db.delete(TABLE, whereClause.toString(), values);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            DBUtil.closeDB(db, null);
        }
        return result;
    }

    private static synchronized ContentValues getValue(DBModel model) {
        ContentValues values = new ContentValues();
        values.put(ModelColumn.uid, model.uid);
        values.put(ModelColumn.key, model.key);
        values.put(ModelColumn.value, model.value);
        values.put(ModelColumn.model, model.name);
        return values;
    }
}
