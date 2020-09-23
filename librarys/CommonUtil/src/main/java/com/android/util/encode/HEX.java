package com.android.util.encode;

/**
 * Hex 工具
 */
public class HEX {
    private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * byte[] 转换成 String
     *
     * @param b
     * @return String
     */
    public static String byteToString(byte[] b) {
        char[] newChar = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
            newChar[2 * i + 1] = hex[b[i] & 0xf];
        }
        return new String(newChar);
    }

    /**
     * String 转换成 byte[]
     *
     * @param hexString
     * @return byte[]
     */
    public static byte[] stringToByte(String hexString) {
        if (hexString.length() % 2 != 0) throw new IllegalArgumentException("error");
        char[] chars = hexString.toCharArray();
        byte[] b = new byte[chars.length / 2];
        for (int i = 0; i < b.length; i++) {
            int high = Character.digit(chars[2 * i], 16) << 4;
            int low = Character.digit(chars[2 * i + 1], 16);
            b[i] = (byte) (high | low);
        }
        return b;
    }
}
