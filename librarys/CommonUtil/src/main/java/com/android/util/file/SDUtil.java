package com.android.util.file;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.android.util.log.LogUtil;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * SD卡工具类
 *
 * @author 张全
 */
public final class SDUtil {
    private SDUtil() {
    }

    /**
     * SD卡是否已挂载.
     *
     * @return true SD挂载成功
     */
    public static boolean isSDValiable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡可用的根目录(包括内置SD卡和外置SD卡)
     *
     * @return SD卡根目录
     */
    public static String getSDRootPath() {
        String rootPath = getSDCardPath();
        if (isSecondSDCardAvailable()) {
            rootPath = Environment.getExternalStorageDirectory().getParent();
        }
        return rootPath;
    }

    /**
     * 获取第一块SD卡路径。
     *
     * @return string 第一块SD卡的绝对路径
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取第二块SD卡路径,目前只考虑最多有两个SD卡的情况。
     *
     * @return string 第二块SD卡的绝对路径
     */
    public static String getSecondSDCardPath() {
        if (isSDValiable()) {
            String parentPath = Environment.getExternalStorageDirectory()
                    .getParent();

            if (!TextUtils.isEmpty(parentPath)) {
                File externalRoot = new File(parentPath);
                // 列出外部SD卡的根目录文件，一般为/storage
                File[] files = externalRoot.listFiles();

                for (int i = 0, length = files.length; i < length; i++) {
                    // 过滤不可读取的USB设备，过滤第一块SD卡的路径
                    if (files[i].isDirectory()
                            && files[i].canRead()
                            && files[i].length() > 0
                            && !files[i].getAbsolutePath().equals(
                            getSDCardPath())) {
                        return files[i].getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取指定目录的可用空间大小，如path为sd卡，通常为整个sd卡的可用空间，如path为内部存储空间，则返回指定目录受系统配额限制的可用空间。
     *
     * @param path 目录绝对路径
     * @return long 单位byte
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        StatFs statFs = new StatFs(path);
        return (long) statFs.getAvailableBlocks() * statFs.getBlockSize();
    }

    /**
     * 获得SD卡容量总大小
     *
     * @param ctx
     * @return SD卡容量大小  比如11GB
     */
    public static String getSDTotalSize(Context ctx) {
        return Formatter.formatFileSize(ctx, getSDTotalSizes(ctx));
    }

    /**
     * 获取SD卡容量总大小
     *
     * @param ctx
     * @return 单位 b
     */
    @SuppressWarnings("deprecation")
    public static long getSDTotalSizes(Context ctx) {
        if (!isSDValiable()) return -1;
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    /**
     * 获得SD卡剩余容量，即可用大小
     *
     * @return 可用大小 比如100MB
     */
    public static String getSDAvailableSize(Context ctx) {
        return Formatter.formatFileSize(ctx, getSDAvailableSizes(ctx));
    }

    /**
     * 获取SD卡剩余容量大小
     *
     * @param ctx
     * @return 单位b
     */
    @SuppressWarnings("deprecation")
    public static long getSDAvailableSizes(Context ctx) {
        if (!isSDValiable()) return -1;
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     * 获取SD卡图片储存路径
     *
     * @param context
     * @return
     */
    public static File getPicFileDir(Context context) {
        File fileDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        if (fileDir == null) {
            fileDir = new File(context.getFilesDir(), Environment.DIRECTORY_PICTURES);
        }
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            LogUtil.w("SDUtil", "Unable to create external cache directory");
        }
        return fileDir;
    }

    /**
     * 获取内部存储空间图片储存路径
     *
     * @param context
     * @return
     */

    public static File getInternalPicFileDir(Context context) {
        File fileDir = new File(context.getFilesDir(), Environment.DIRECTORY_PICTURES);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            LogUtil.w("SDUtil", "Unable to create internal pic directory");
        }
        return fileDir;
    }

    /**
     * 获得一张新的锁屏图片文件
     *
     * @param context
     * @return
     */

    public static File createNewLockImgFile(Context context) {
        File dir = getInternalPicFileDir(context);
        File imgFile = new File(dir, System.currentTimeMillis() + ".jpg");
        try {
            imgFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgFile;
    }

    /**
     * 第二块SD卡是否可用。
     *
     * @return boolean true表示可用
     */
    public static boolean isSecondSDCardAvailable() {
        return !TextUtils.isEmpty(getSecondSDCardPath());
    }

}
