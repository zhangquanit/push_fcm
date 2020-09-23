package com.android.util.os;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.android.util.LContext;
import com.android.util.encode.MD5;

import java.util.List;

/**
 * 手机工具类
 *
 * @author 张全
 */
public class DeviceUtil {
    public static int screenHeight = -1;
    public static int screenWidth = -10;
    public static float screenDensity = -1;
    private static int statusBarHeight = -1;
    private static int screenHeightWithoutStatusBar = -1;

    private DeviceUtil() {

    }

    /**
     * 获取设备机型.
     *
     * @return 设备机型字符串
     */
    public static String getDeviceType() {
        return Build.MODEL;
    }

    /**
     * 获取系统版本.
     *
     * @return 当前版本
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取应用版本名称.
     *
     * @param context 上下文
     * @return 应用版本名
     */
    public static String getAppVersionName(Context context) {
        PackageInfo pi = getPackageInfo(context);
        if (null != pi) {
            return pi.versionName;
        }
        return null;
    }

    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String metaValue = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                metaValue = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return metaValue;
    }

    /**
     * 获取版本号。
     *
     * @param context 上下文
     * @return 应用版本号
     */
    public static int getAppVersionCode(Context context) {
        PackageInfo pi = getPackageInfo(context);
        if (null != pi) {
            return pi.versionCode;
        }
        return -1;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    /**
     * 获取设备号，imei号。
     *
     * @param context 上下文
     * @return 设备ID
     */
    public static String getDeviceId(Context context) {
        try {
            TelephonyManager ty =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = ty.getDeviceId();
            if (!TextUtils.isEmpty(deviceId))
                return deviceId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String deviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(deviceId))
                return deviceId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取显示矩阵。
     *
     * @param context 上下文
     * @return 返回矩阵
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * 获取屏幕宽度。
     *
     * @param context 上下文
     * @return 宽度
     */
    public static int getScreenWidthPx(Context context) {
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        return screenWidth;
    }

    /**
     * 获取屏幕高度。
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getScreenHeightPx(Context context) {
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        return screenHeight;
    }

    /**
     * 获取屏幕状态栏高度
     *
     * @param act
     * @return
     */
    public static int getStatusBarHeight(Activity act) {
        if (statusBarHeight > 0) {
            return statusBarHeight;
        }
        try {
            Context appContext = act.getApplicationContext();
            int resourceId =
                    appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = appContext.getResources().getDimensionPixelSize(resourceId);
            }
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取屏幕高度(去掉状态栏高度)
     *
     * @param act
     * @return
     */
    public static int getScreenHeightWithoutStausBar(Activity act) {
        if (screenHeightWithoutStatusBar > 0) {
            return screenHeightWithoutStatusBar;
        }
        screenHeightWithoutStatusBar = screenHeight - getStatusBarHeight(act);
        return screenHeightWithoutStatusBar;
    }

    /**
     * dip转px。
     *
     * @param context 上下文
     * @param dip     dip
     * @return px
     */
    public static int dip2px(Context context, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics());
    }

    /**
     * px转dip
     *
     * @param context
     * @param px
     * @return
     */
    public static int px2dip(Context context, int px) {
        return Math.round(px
                / context.getResources().getDisplayMetrics().density);
    }

    /**
     * 是否安装了某个app
     *
     * @param context
     * @param pkg     target app的包名
     * @return
     */
    public static boolean isAppInstalled(Context context, String pkg) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
            String packageName = applicationInfo.packageName;
            if (packageName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得应用程序的数字签名
     *
     * @return
     */
    public static String getSignature() {
        try {
            PackageManager pm = LContext.getContext().getPackageManager();
            // 得到当前应用程序的签名
            PackageInfo info = pm.getPackageInfo(LContext.getContext().getPackageName(),
                    PackageManager.GET_SIGNATURES);
            String signature = info.signatures[0].toCharsString();//原始md5值，比较长
            signature = MD5.MD5Encode(signature);
            return signature;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPkgName() {
        return LContext.getContext().getPackageName();
    }
}
