package com.android.util.text;

import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author 张全
 */
public class RegUtil {

    public static final String REG_EMAIL = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
    public static final String REG_PWD = "^[a-zA-Z0-9_!@#$%^&*~]{6,16}$";//由6-16位数字、字母、特殊字符组成
    public static final String USER_PWD = "^[a-zA-Z0-9]{6,16}$";//由6-16位数字、大小写字母
    public static final String REG_LETTER_NUM = "^[A-Za-z0-9]+$";//字母或数字  ^[0-9a-zA-Z]{6,16}$ 表示字母或数字 6-16位
    public static final String REG_PHONE = "^[1][3,4,5,7,8][0-9]{9}$"; //电话号码
    private static int[] cardNo_wi = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
            8, 4, 2};
    private static String[] cardNo_model = new String[]{"1", "0", "X", "9", "8", "7", "6",
            "5", "4", "3", "2"};

    /**
     * 纯数字验证
     *
     * @param number
     * @return
     */
    public static boolean isNumber(String number) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(number).matches();
    }

    /**
     * 确认字符串是否为email格式
     *
     * @param strEmail
     * @return
     */
    public static boolean isEmail(String strEmail) {
        return match(REG_EMAIL, strEmail);
    }

    /**
     * 正则校验
     *
     * @param reg
     * @param content
     * @return
     */
    public static boolean match(String reg, String content) {
        if (TextUtils.isEmpty(reg) || TextUtils.isEmpty(content)) {
            return false;
        }
        Pattern pattern = Pattern.compile(reg);
        boolean matches = pattern.matcher(content).matches();
        return matches;
    }

    /**
     * 密码格式校验：6-16位，包含数字、字母
     *
     * @param content
     * @return
     */
    public static boolean isPassword(String content) {
        return match(REG_PWD, content);
    }

    public static boolean password(String content) {
        return match(USER_PWD, content);
    }

    public static boolean isPassword(String content, int min, int max) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        String reg = "^[0-9a-zA-Z]{" + min + "," + max + "}$";
        Pattern pattern = Pattern.compile(reg);
        boolean matches = pattern.matcher(content).matches();
        return matches;
    }

    /**
     * 电话号码格式检验
     *
     * @param phoneNo
     * @return
     */
    public static boolean isPhoneNum(String phoneNo) {
        return match(REG_PHONE, phoneNo);
    }

    /**
     * 身份证号检验
     *
     * @param cardNo
     * @return
     */
    public static boolean checkCardNo(String cardNo) {
        if (TextUtils.isEmpty(cardNo)) return false;
        String preNo = cardNo.substring(0, 17);
        int sum = 0;
        for (int i = 0; i < cardNo_wi.length; i++) {
            char c = preNo.charAt(i);
            int j = c - '0';
            sum += cardNo_wi[i] * j;
        }

        int m = sum % 11;

        String last = cardNo.substring(cardNo.length() - 1, cardNo.length());
        if (cardNo_model[m].equals(last)) {
            return true;
        }
        return false;
    }

}
