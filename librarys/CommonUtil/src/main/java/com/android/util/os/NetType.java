package com.android.util.os;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 网络类型
 */
public class NetType {
    /**
     * 无网络
     */
    public static final int INVALID = -1;
    /**
     * 有线LAN
     */
    public static final int ETHERNET = 0;
    /**
     * 无线WIFI
     */
    public static final int WIFI = 1;
    /**
     * 移动网络2G
     */
    public static final int MOBILE_2G = 2;
    /**
     * 移动网络3G
     */
    public static final int MOBILE_3G = 3;
    /**
     * 移动网络4G
     */
    public static final int MOBILE_4G = 4;

    /**
     * 获得网络类型
     *
     * @param ctx
     * @return {@link #INVALID}</br> {@link #WIFI}</br> {@link #ETHERNET}</br> {@link #MOBILE_2G}</br> {@link #MOBILE_3G}</br> {@link #MOBILE_4G}
     */
    public static int getNetType(Context ctx) {
        if (ctx == null) return INVALID;
        final ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo mobNetInfoActivity = connectivityManager.getActiveNetworkInfo();
        if (mobNetInfoActivity != null && mobNetInfoActivity.isAvailable()) {
            int netType = mobNetInfoActivity.getType();
            switch (netType) {
                case ConnectivityManager.TYPE_WIFI:
                    return WIFI;
                case ConnectivityManager.TYPE_ETHERNET:
                    return ETHERNET;
                case ConnectivityManager.TYPE_MOBILE:
                    if (isFastMobileNetwork(ctx)) {
                        return MOBILE_3G;
                    } else {
                        return MOBILE_2G;
                    }
                default:
                    return INVALID;
            }
        } else {
            return INVALID;
        }
    }

    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }
}
