package com.android.util.os;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 调用系统照相机工具类
 * <p>
 * <pre class="prettyprint">
 * 在Activity或Fragment调用列子
 * private final static int PERMISSION_CAMERA=1;
 * public void requestCamera(){
 * //在Manifest.xml中申请照相机权限   <uses-permission android:name="android.permission.CAMERA"/>
 * if (Build.VERSION.SDK_INT >= 23) {
 * //android6.0需要动态申请权限
 * int mCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
 * if (mCameraPermission != PackageManager.PERMISSION_GRANTED) {
 * ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
 * } else {
 * CameraUtil.openCamera(this);
 * }
 * } else {
 * CameraUtil.openCamera(this);
 * }
 * }
 *
 * @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
 * super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 * switch (requestCode) {
 * case PERMISSION_CAMERA:
 * if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
 * // Permission Granted
 * CameraUtil.openCamera(this);
 * } else {
 * // Permission Denied
 * Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
 * }
 * break;
 * default:
 * super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 * }
 * }
 * </pre>
 */
public class CameraUtil {
    private static List<LaucherAppInfo> cameraApps;

    /**
     * 打开相机
     *
     * @param ctx
     * @return
     */
    public static boolean openCamera(Context ctx) {
        if (null == cameraApps) {
            cameraApps = getCameraApp(ctx);
        }

        if (!cameraApps.isEmpty()) {
            for (LaucherAppInfo appInfo : cameraApps) {
                Intent intent = new Intent();
                try {
                    ComponentName comp = new ComponentName(appInfo.pkgName, appInfo.actName);
                    intent.setComponent(comp);
                    intent.setAction("android.intent.action.VIEW");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                    return true;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * 获取手机上的系统照相机
     *
     * @param ctx
     * @return
     */
    public static List<LaucherAppInfo> getCameraApp(Context ctx) {
        List<LaucherAppInfo> launcherApps = getLauncherApps(ctx);
        PackageManager pm = ctx.getPackageManager();

        //得到系统安装的所有程序包的PackageInfo对象
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        ArrayList<String> cameraPkgs = new ArrayList<>();
        for (PackageInfo pi : packages) {
            ApplicationInfo applicationInfo = pi.applicationInfo;
            if (!isUserApp(applicationInfo)) {
                //系统应用
                String packageName = applicationInfo.packageName;
                if (packageName.contains("camera")) {
                    cameraPkgs.add(packageName);
                }
            }
        }
        //过滤系统照相机
        List<LaucherAppInfo> cameraApps = new ArrayList<>();
        for (LaucherAppInfo appInfo : launcherApps) {
            for (String pkg : cameraPkgs) {
                if (appInfo.pkgName.equals(pkg)) {
                    cameraApps.add(appInfo);
                }
            }
        }
        //特殊处理
        if (!cameraPkgs.contains("com.android.camera")) {
            LaucherAppInfo appInfo = new LaucherAppInfo();
            appInfo.pkgName = "com.android.camera";
            appInfo.actName = "com.android.camera.Camera";
            cameraApps.add(appInfo);

            appInfo = new LaucherAppInfo();
            appInfo.pkgName = "com.android.camera";
            appInfo.actName = "com.android.camera.CameraLauncher";
            cameraApps.add(appInfo);
        }
        if (!cameraPkgs.contains("com.android.camera2")) {
            LaucherAppInfo appInfo = new LaucherAppInfo();
            appInfo.pkgName = "com.android.camera2";
            appInfo.actName = "com.android.camera.CameraLauncher";
            cameraApps.add(appInfo);
        }

        return cameraApps;
    }

    /**
     * 获取具有Main入口的应用
     *
     * @param ctx
     * @return
     */
    public static List<LaucherAppInfo> getLauncherApps(Context ctx) {
        PackageManager pm = ctx.getPackageManager();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolvInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        List<LaucherAppInfo> launcherApps = new ArrayList<LaucherAppInfo>();
        for (ResolveInfo info : resolvInfos) {
            ApplicationInfo appInfo = info.activityInfo.applicationInfo;
            String appName = appInfo.loadLabel(pm).toString();// 程序名称
            String packageName = appInfo.packageName;// 获取程序包名
            if (packageName.contains("camera") || appName.contains("相机")) {
                LaucherAppInfo laucherAppInfo = new LaucherAppInfo();
                laucherAppInfo.pkgName = packageName;
                laucherAppInfo.actName = info.activityInfo.name;
                launcherApps.add(laucherAppInfo);
            }
        }
        return launcherApps;
    }

    public static boolean isUserApp(ApplicationInfo applicationInfo) {
        return (!isSystemApp(applicationInfo) && !isSystemUpdateApp(applicationInfo));
    }

    public static boolean isSystemApp(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static boolean isSystemUpdateApp(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
    }

    /**
     * 具有入口的应用
     */
    public static class LaucherAppInfo {
        public String pkgName;
        public String actName;

        @Override
        public String toString() {
            return "LaucherAppInfo{" +
                    "pkgName='" + pkgName + '\'' +
                    ", actName='" + actName + '\'' +
                    '}';
        }
    }
}

