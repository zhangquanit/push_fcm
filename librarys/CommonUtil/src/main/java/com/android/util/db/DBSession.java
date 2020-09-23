package com.android.util.db;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author 张全
 */
public class DBSession<T> {
    private Class<T> cls;
    private Field uidField;
    private Field keyField;
    private String modleName;
    private String uid;
    private static Map<String, List<ObservableEmitter>> observerableMap = new HashMap<>();
    private static final String DEF_UID = "-1";


    public DBSession(Class<T> cls) {
        this(cls, null);
    }

    public DBSession(Class<T> cls, String uid) {
        this.cls = cls;
        this.uid = uid;
        modleName = cls.getSimpleName();

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Uid.class)) {
                uidField = field;
                uidField.setAccessible(true);
            } else if (field.isAnnotationPresent(Key.class)) {
                keyField = field;
                keyField.setAccessible(true);
            }
        }
    }

    public List<T> query() {
        List<String> values = ModelDao.getUserModels(getUid(), modleName);
        List<T> list = new ArrayList<>();
        for (String value : values) {
            list.add(new Gson().fromJson(value, cls));
        }
        return list;
    }

    public Observable<List<T>> queryWithObserverable() {
        return Observable.create(new ObservableOnSubscribe<List<T>>() {
            @Override
            public void subscribe(ObservableEmitter<List<T>> emitter) throws Exception {
                List<ObservableEmitter> weakReferences = observerableMap.get(modleName);
                if (null == weakReferences) {
                    weakReferences = new ArrayList<>();
                    observerableMap.put(modleName, weakReferences);
                }
                weakReferences.add(emitter);

                //先发射一次
                push();
            }
        });
    }

    public void delete() {
        ModelDao.deleteUserModels(getUid(), modleName);
        push();
    }

    public void delete(T value) {
        if (null == value) return;
        List<T> values = new ArrayList<>();
        values.add(value);
        delete(values);
    }

    public void delete(List<T> values) {
        if (null == values | values.isEmpty()) return;
        String uid = getUid(values.get(0));
        List<String> keys = new ArrayList<>();
        for (T value : values) {
            String key = getKey(value);
            keys.add(key);
        }
        ModelDao.delete(uid, modleName, keys);
        push();
    }

    public void insert(T value) {
        if (null == value) return;
        List<T> values = new ArrayList<>();
        values.add(value);
        insert(values);
    }

    public void insert(List<T> values) {
        if (null == values | values.isEmpty()) return;
        List<DBModel> models = new ArrayList<>();
        for (T value : values) {
            DBModel dbModel = new DBModel();
            dbModel.uid = getUid(value);
            dbModel.name = modleName;
            dbModel.key = getKey(value);
            dbModel.value = new Gson().toJson(value);
            models.add(dbModel);
        }
        ModelDao.save(models);
        push();
    }

    private String getUid() {
        String userId = uid;
        if (TextUtils.isEmpty(userId)) {
            userId = DEF_UID;
        }
        return userId;
    }

    private String getUid(T value) {
        String userId = uid;
        if (TextUtils.isEmpty(userId) && null != uidField && null != value) {
            try {
                Object id = uidField.get(value);
                if (null != id) userId = id.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(userId)) {
            userId = DEF_UID;
        }
        return userId;
    }

    private void push() {
        if (observerableMap.containsKey(modleName)) {
            List<ObservableEmitter> emitterWeakReference = observerableMap.get(modleName);
            if (null == emitterWeakReference) return;
            Iterator<ObservableEmitter> iterator = emitterWeakReference.iterator();
            while (iterator.hasNext()) {
                ObservableEmitter emitter = iterator.next();
                if (null != emitter && !emitter.isDisposed()) {
                    emitter.onNext(query());
                } else {
                    iterator.remove();
                }
            }
        }
    }

    private String getKey(T value) {
        String key = null;
        if (null == value || null == keyField) return null;
        try {
            Object keyValue = keyField.get(value);
            if (null != keyValue) key = keyValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }
}
