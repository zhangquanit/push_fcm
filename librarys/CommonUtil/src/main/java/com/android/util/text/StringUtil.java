package com.android.util.text;

import android.text.TextUtils;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * 字符串工具
 */
public class StringUtil {

    private StringUtil() {

    }

    public static boolean isEmpty(CharSequence var) {
        return var == null || var.length() == 0;
    }

    /**
     * 校验空字符串
     *
     * @param var
     * @return 是否为空
     */
    public static boolean isEmpty(String var) {
        return var == null || var.trim().length() == 0
                || "null".equalsIgnoreCase(var);
    }

    /**
     * 校验List是否为空
     *
     * @param list
     * @return
     */
    public static <T> boolean isEmpty(List<T> list) {
        return null == list || list.size() == 0;
    }

    /**
     * 校验数组是否为空
     *
     * @param array 数组
     * @return
     */
    public static <T> boolean isEmpty(T[] array) {
        return null == array || array.length == 0;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * 忽略空白不分写大小比较字符串
     *
     * @param var1
     * @param var2
     * @return boolean
     */
    public static boolean equals(String var1, String var2) {
        if (isEmpty(var1) && isEmpty(var2))
            return true;
        else if (isEmpty(var1) || isEmpty(var2))
            return false;
        else if (var1.trim().length() != var2.trim().length())
            return false;
        else
            return var1.trim().toLowerCase().equals(var2.trim().toLowerCase());
    }

    /**
     * 转化成字符串
     *
     * @param o
     * @return
     */
    public static String toString(Object o) {
        if (o == null)
            return null;
        else
            return o.toString();
    }

    /**
     * 转换 Float
     *
     * @param txt
     * @return
     */
    public static float toFloat(String txt) {
        if (!isEmpty(txt))
            return Float.parseFloat(txt);
        return 0;
    }

    /**
     * 转换 Int
     *
     * @param txt
     * @return
     */
    public static int toInt(String txt, int defult) {
        int data = defult;
        try {
            if (!isEmpty(txt)) {
                data = Integer.parseInt(txt);
            }
        } catch (Exception e) {
            data = defult;
        }
        return data;
    }

    /**
     * 转换 Int
     *
     * @param txt
     * @return
     */
    public static int toInt(String txt) {
        if (!isEmpty(txt))
            return Integer.parseInt(txt);
        return 0;
    }

    /**
     * 忽略null
     *
     * @param text
     * @return
     */
    public static String ignoreNull(String text) {
        if ("null".equalsIgnoreCase(text)) {
            return null;
        } else {
            return text;
        }
    }

    /**
     * 判断字符串是否以中文开头
     *
     * @param str
     * @return
     */
    public static boolean isChinese(String str) {
        if (TextUtils.isEmpty(str)) return false;
        char[] mCharArray = str.toCharArray();
        String numstr = Integer.toBinaryString(mCharArray[0]);
        if (numstr.length() > 8) {
            return true;
        }
        return false;
    }

    /**
     * 去掉空格
     *
     * @param text
     * @return
     */
    public static String trim(String text) {
        if (null == text) {
            return null;
        }
        return text.trim().replaceAll(" ", "");
    }

    /**
     * 去掉空格
     *
     * @param textView
     * @return
     */
    public static String trim(TextView textView) {
        if (null == textView) {
            return null;
        }
        CharSequence text = textView.getText();
        if (null == text) {
            return null;
        }
        return text.toString().trim().replaceAll(" ", "");
    }

    /**
     * 用法： TextView 避免由于占位导致的排版混乱 </br>
     * 将textview中的字符全角化。即将所有的数字、字母及标点全部转为全角字符，使它们与汉字同占两个字节，这样就可以避免由于占位导致的排版混乱问题了
     * </BR> 摘自如下文章：</br> <a
     * href="http://blog.csdn.net/venus565825/article/details/8172320"
     * >http://blog.csdn.net/venus565825/article/details/8172320</a>
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        if (StringUtil.isEmpty(input))
            return null;
        input = input.replaceAll("\\〜", "-");

        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

}
