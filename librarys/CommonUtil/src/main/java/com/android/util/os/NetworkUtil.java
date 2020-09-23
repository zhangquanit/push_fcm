package com.android.util.os;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 网络相关工具类.
 */
public final class NetworkUtil {
    public static final String SCHEMA_HTTP = "http://";
    private static final String NETWORK_TYPE_CMWAP = "cmwap";
    private static final String NETWORK_TYPE_UNIWAP = "uniwap";
    private static final String NETWORK_TYPE_3GWAP = "3gwap";
    private static final String NETWORK_TYPE_CTWAP = "ctwap";
    private static final String PROXY_CMWAP = "10.0.0.172";
    private static final String PROXY_CTWAP = "10.0.0.200";

    public enum NetState {
        LINECONNECTED, WIFICONNECTED, DISCONNECTED, UNKNOWN
    }

    private static final Pattern IP_PATTERN = Pattern
            .compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

    private NetworkUtil() {
    }

    /**
     * 获得IP
     *
     * @return String 网络IP地址
     */
    public static String getIp() {
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();
                    if (!ip.isLoopbackAddress()) {
                        String ipStr = ip.getHostAddress();
                        if (IP_PATTERN.matcher(ipStr).find()) return ipStr;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取wifi地址。
     *
     * @param context 上下文
     * @return mac地址
     */
    public static String getWifiMacAddress(Context context) {
        String wifiMac = "";
        // 加入try catch 解决线上崩溃
        try {
            if (null != context) {
                WifiManager wm = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                if (null != wm && null != wm.getConnectionInfo()) {
                    wifiMac = wm.getConnectionInfo().getMacAddress();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return wifiMac;
    }

    /**
     * 获取Wifi强度
     *
     * @param context
     * @return 得到的值是一个0到-100的区间值，是一个int型数据，其中0到-50表示信号最好，-50到-70表示信号偏差，小于-70表示最差，有可能连接不上或者掉线。
     */
    public static int getWifiStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        // 链接信号强度
        int strength = WifiManager.calculateSignalLevel(connectionInfo.getRssi(), 5);
        return strength;
    }

    /**
     * 获取网络连接类型：有线、无线
     *
     * @return
     */
    public static NetState getConnectionType(Context context) {
        boolean isConnect = isNetworkAvailable(context);
        if (isConnect) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    String typeName = info.getTypeName(); // WIFI/MOBILE
                    if ("ethernet".equals(typeName)) {//有线
                        return NetState.LINECONNECTED;
                    } else {
                        return NetState.WIFICONNECTED;
                    }
                }
            }
        } else {
            return NetState.DISCONNECTED;
        }
        return null;
    }

    /**
     * 获取代理host地址.目前仅支持{@code #NETWORK_TYPE_CMWAP},{@code #NETWORK_TYPE_UNIWAP},
     * {@code #NETWORK_TYPE_3GWAP},{@code #NETWORK_TYPE_CTWAP}.四种.其余不进行代理.
     *
     * @param context 上下文
     * @return 代理地址.
     */
    public static String getProxy(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getExtraInfo() == null) {
            return null;
        }

        String info = networkInfo.getExtraInfo().toLowerCase(
                Locale.getDefault());
        if (info != null) {
            if (info.startsWith(NETWORK_TYPE_CMWAP)
                    || info.startsWith(NETWORK_TYPE_UNIWAP)
                    || info.startsWith(NETWORK_TYPE_3GWAP)) {
                return PROXY_CMWAP;
            } else if (info.startsWith(NETWORK_TYPE_CTWAP)) {
                return PROXY_CTWAP;
            }
        }
        return null;
    }

    /**
     * 检测网络是否可用.wifi和手機有一種可用即爲可用.
     *
     * @param context 上下文对象.此处请使用application的context.
     * @return true可用.false不可用.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return isNetworkActive(info);
    }

    /**
     * 移动数据网络是否可用.
     *
     * @param context context.此处请使用application的context.
     * @return 是否可用.true可用.false不可用.
     */
    public static boolean isMobileNetAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return isNetworkActive(info);
    }

    /**
     * WIFI连接是否可用.
     */
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return isNetworkActive(info);
    }

    /**
     * 指定类型的连接是否可用.
     *
     * @param info networkinfo.
     * @return 是否可用.true可用, false不可用.
     */
    private static boolean isNetworkActive(NetworkInfo info) {
        if (info != null && info.isConnected() && info.isAvailable()) {
            return true;
        }
        return false;
    }
}
