package com.android.util.encode;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * 3DES加密
 */
public class Des3Encoder {
    private static final String KEY = "sndo@agriculture@analytics"; //3DES的加密密钥长度要求是24个字节
    private final static String IV = "01234567";    // 向量
    private final static String ENCODING = "UTF-8"; // 加解密统一使用的编码方式

    public static void main(String[] args) throws Exception {
        test("12345678");
    }

    public static void test(String src) throws Exception {
        //UTF8编码->DES3加密->DES3解密->UTF8解码
        byte[] encryptBytes = Des3Encoder.encrypt(KEY, src.getBytes("UTF-8"));
        String data = Base64.encodeToString(encryptBytes, Base64.DEFAULT);
        byte[] decrypt = Des3Encoder.decrypt(KEY, Base64.decode(data, Base64.DEFAULT));
        data = new String(decrypt, "UTF-8");
    }

    /**
     * 加密
     *
     * @param key      密钥
     * @param srcBytes 原始数据
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(String key, byte[] srcBytes) throws Exception {
//		if (TextUtils.isEmpty(key) || null == srcBytes || srcBytes.length == 0) {
//			return srcBytes;
//		}
        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key.getBytes());
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        byte[] encryptData = cipher.doFinal(srcBytes);

        return encryptData;
    }

    /**
     * 解密
     *
     * @param key          解密密钥
     * @param encodedBytes 加密后的字节
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(String key, byte[] encodedBytes)
            throws Exception {
//		if (TextUtils.isEmpty(key) || null == encodedBytes
//				|| encodedBytes.length == 0) {
//			return encodedBytes;
//		}

        Key deskey = null;
        DESedeKeySpec spec = new DESedeKeySpec(key.getBytes());
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");
        IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips);

        byte[] decryptData = cipher.doFinal(encodedBytes);

        return decryptData;
    }
}
