package com.android.util.ext;

import com.android.util.encode.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 对象序列化
 *
 * @author 张全
 */
public class ObjectSerializable {

    public static <T> T deserializeObj(String data, Class<T> t) {
        byte[] value = Base64.decode(data, Base64.DEFAULT);
        ByteArrayInputStream input = new ByteArrayInputStream(value);
        ObjectInputStream inputStream = null;
        T obj = null;
        try {
            inputStream = new ObjectInputStream(input);
            obj = (T) inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return obj;
    }

    public static String serializeObj(Object obj) {
        ObjectOutputStream outputStream = null;
        String value = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            outputStream = new ObjectOutputStream(bos);
            outputStream.writeObject(obj);
            byte[] data = bos.toByteArray();
            value = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
