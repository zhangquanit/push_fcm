package com.android.util.encode;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * ECB加密
 *
 * @author 刘纯明
 */
public class DES {
    /**
     * 加密
     */
    public static String encodeECB(String key, String data) {
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            DESedeKeySpec desKeySpec = new DESedeKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return HEX.byteToString(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解密
     */
    public static String decodeECB(String key, String str) {
        try {
            byte[] data = HEX.stringToByte(str);
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            DESedeKeySpec desKeySpec = new DESedeKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
            SecretKey deskey = keyfactory.generateSecret(desKeySpec);
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            return new String(cipher.doFinal(data));
        } catch (Exception e) {
            return null;
        }
    }
}
