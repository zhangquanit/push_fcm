
package com.android.util.os;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.util.Collections;
import java.util.List;

/**
 * @author 张全
 */
public class OsUtil {

    public static boolean isDeviceAccelerometerRotation(Context ctx) {
        int rotaion = Settings.System.getInt(ctx.getContentResolver(), "accelerometer_rotation", 0);
        return rotaion == 1;
    }

    public static boolean setDeviceAccelerometerRotation(Context ctx, boolean rotate) {
        return Settings.System.putInt(ctx.getContentResolver(), "accelerometer_rotation", rotate ? 1 : 0);
    }


    public static boolean isGpsEnabled(Context ctx) {
        return ((LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled("gps");
    }

    public static boolean getScreenBrightnessMode(Context ctx) {
        return 1 == Settings.System.getInt(ctx.getContentResolver(), "screen_brightness_mode", 1);
    }

    public static int getScreenBrightness(Context ctx) {
        return Settings.System.getInt(ctx.getContentResolver(), "screen_brightness", 255);
    }

    public static boolean setScreenBrightness(Context ctx, int brightnes) {
        return Settings.System.putInt(ctx.getContentResolver(), "screen_brightness", brightnes);
    }

    public static List<ApplicationInfo> getApplicationInfos(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            List<ApplicationInfo> localList = pm.getInstalledApplications(0);
            Collections.sort(localList, new ApplicationInfo.DisplayNameComparator(pm));
            return localList;
        } catch (Throwable localThrowable) {
            localThrowable.printStackTrace();
        }
        return null;
    }

    public static class SystemBarsHelper {
        private WindowManager windowManager;
        private View view;
        private Context ctx;
        private boolean hasAddedStatusBar;

        public SystemBarsHelper(Context ctx) {
            this.ctx = ctx;
            this.windowManager = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE));
        }

        @TargetApi(14)
        public boolean hasPermanentMenuKey(Context ctx) {
            try {
                if ((Build.VERSION.SDK_INT >= 14) && (ViewConfiguration.get(ctx).hasPermanentMenuKey())) {
                    return true;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return false;
        }

        public static int getStatusBarHeight(Context ctx) {
            try {
                int i = Integer.parseInt(Class.forName("com.android.internal.R$dimen").getField("status_bar_height").get(null).toString());
                int j = ctx.getResources().getDimensionPixelSize(i);
                return j;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (int) (0.5F + 25.0F * ctx.getResources().getDisplayMetrics().density);
        }
    }
}
