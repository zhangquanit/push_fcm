
package com.android.util.os;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.android.util.ext.ToastUtil;

import java.lang.reflect.Method;

/**
 * 常用的跳转Intent
 *
 * @author 张全
 */
public class IntentUtil {

    /**
     * 跳转到应用市场进行软件评论和评分
     *
     * @param ctx
     */
    public static boolean startCommentApp(Context ctx) {
        try {
            String pkg = ctx.getPackageName();
            Uri uri = Uri.parse("market://details?id=" + pkg);
            if (uri == null) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ctx.getPackageManager().queryIntentActivities(intent, 65536).size() <= 0) {
                return false;
            }
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 系统锁屏是否设置了密码
     *
     * @param ctx
     * @return
     */

    public static boolean isSetSysPass(Context ctx) {
        try {
            @SuppressWarnings("rawtypes")
            Class cls = Class.forName("com.android.internal.widget.LockPatternUtils");
            Object obj = cls.getConstructors()[0].newInstance(new Object[]{
                    ctx
            });
            @SuppressWarnings("unchecked")
            Method method = cls.getMethod("isSecure", new Class[0]);
            boolean bool = ((Boolean) method.invoke(obj, new Object[0])).booleanValue();
            return bool;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息
     *
     * @param context
     * @param packageName 应用包名
     * @param defaultMsg  打开失败后的提示信息
     */
    public static void showInstalledAppDetails(Context context, String packageName, String toastMsg) {
        try {
            Intent intent = new Intent();
            final int apiLevel = Build.VERSION.SDK_INT;
            if (Build.VERSION.SDK_INT >= 9) { // 2.3及以上
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                Uri uri = Uri.fromParts("package", packageName, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                /*
                 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
                 */
                final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
                /*
                 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
                 */
                final String APP_PKG_NAME_22 = "pkg";
                final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
                final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

                final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
                intent.putExtra(appPkgName, packageName);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            if (!TextUtils.isEmpty(toastMsg)) {
                ToastUtil.show(toastMsg);
            }
        }
    }
}
