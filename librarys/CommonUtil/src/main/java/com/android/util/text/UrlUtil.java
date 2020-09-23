package com.android.util.text;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * URL 工具
 */
public class UrlUtil {
    private static final String CHARSET = "UTF-8";

    private UrlUtil() {

    }

    /**
     * 补全域名 首http:// 尾/
     *
     * @return String
     */
    public static String fullDomain(String s) {
        if (StringUtil.isEmpty(StringUtil.ignoreNull(s))) return null;
        if (s.indexOf("http://") != 0) s = "http://" + s;
        if (s.lastIndexOf("/") < (s.length() - 1)) s += "/";
        return s;
    }

    /**
     * 获得URL 参数
     *
     * @param url
     * @return Map<String                                                                                                                               ,                                                                                                                                                                                                                                                               String>
     */
    public static Map<String, String> getParm(String url) throws Exception {
        if (url == null || StringUtil.isEmpty(url)) return new HashMap<String, String>();
        url = URLDecoder.decode(url, CHARSET);
        int parmStart = url.indexOf('?');
        if (parmStart <= 0) return new HashMap<String, String>();

        Map<String, String> parm = new HashMap<String, String>();
        String parmStr = url.substring(parmStart + 1);
        String paramaters[] = parmStr.split("&");
        for (String param : paramaters) {
            String[] values = param.split("=");
            if (values.length == 2) {
                String key = values[0];
                String value = values[1];
                if (!StringUtil.isEmpty(key)) {
                    try {

                        key = URLDecoder.decode(key.trim(), CHARSET);
                        value = URLDecoder.decode(value.trim(), CHARSET);
                        if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(value)) {
                            parm.put(key, value);
                        }
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            }
        }
        return parm;
    }

    /**
     * 设置URL参数
     *
     * @param url
     * @param parm
     */
    public static String setParm(String url, Map<String, String> parm) throws Exception {
        if (url == null || StringUtil.isEmpty(url)) return null;
        if (parm == null || parm.isEmpty()) return url;
        Map<String, String> urlParm = getParm(url);
        urlParm.putAll(parm);

        StringBuffer newUrl = null;
        int parmStart = url.indexOf('?');

        if (parmStart >= 0) newUrl = new StringBuffer(url.substring(0, parmStart));
        else newUrl = new StringBuffer(url);

        newUrl.append("?");

        Set<String> keySet = urlParm.keySet();
        for (String key : keySet) {
            String value = urlParm.get(key);
            try {
                key = URLEncoder.encode(key.trim(), CHARSET);
                value = URLEncoder.encode(value.trim(), CHARSET);
                if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(value)) {
                    newUrl.append(key).append("=").append(value).append("&");
                }
            } catch (UnsupportedEncodingException e) {
            }
        }

        url = newUrl.toString().trim();

        if (url.substring(url.length() - 1).equals("&")) {
            return url.substring(0, url.length() - 1);
        } else return url;
    }

    /**
     * 截取端口
     *
     * @param str
     * @return int
     */
    public static int getPort(String str, int defult) {
        str = getDomain(str);
        if (str == null) return defult;

        int indexOf = str.indexOf(":");
        if (indexOf >= 0) str = str.substring(indexOf);
        if (str == null || str.trim().length() == 0) return defult;
        else {
            try {
                return Integer.parseInt(str.trim());
            } catch (Exception e) {
                return defult;
            }
        }
    }

    /**
     * 截取主机
     *
     * @param str
     * @return String
     */
    public static String getHost(String str) {
        str = getDomain(str);
        if (str == null) return null;

        int indexOf = str.indexOf(":");
        if (indexOf >= 0) str = str.substring(0, indexOf);

        return str;
    }

    /**
     * 截取域名
     *
     * @param str
     * @return domain
     */
    public static String getDomain(String str) {
        if (str == null || str.trim().length() == 0) return null;
        str = str.toLowerCase();
        str = str.replace("http://", "");

        int indexOf = str.indexOf("/");
        if (indexOf >= 0) str = str.substring(0, indexOf);
        return str;
    }
}
