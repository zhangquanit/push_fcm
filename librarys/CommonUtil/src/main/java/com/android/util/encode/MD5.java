package com.android.util.encode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5
 *
 * @author 刘纯明
 */
public class MD5 {
    public static String MD5Encode(String str) {
        return MD5Encode(str.getBytes());
    }

    public static String MD5Encode(byte[] toencode) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(toencode);
            return HEX.byteToString(md5.digest());
        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }
}
