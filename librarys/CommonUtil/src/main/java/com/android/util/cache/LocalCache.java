package com.android.util.cache;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 本地缓存
 * <p>
 * 默认使用的是context.getCacheDir();
 * </p>
 *
 * @author 张全
 */
public enum LocalCache {
    /**
     * 图片缓存
     */
    IMAGE("image", 20 * 1024 * 1024);

    private static File DIR = null;
    private String path; //路径
    private long maxSize; //最大大小

    private LocalCache(String name, long maxSize) {
        this.path = name;
        this.maxSize = maxSize;
    }

    public File getPath(Context ctx) {
        File dir = new File(getDir(ctx), path);
        dir.mkdirs();
        return dir;
    }

    /**
     * 清空缓存
     *
     * @param ctx
     */
    public static void clear(Context ctx) {
        for (LocalCache cache : values()) {
            List<File> fileList = fileList(new File(getDir(ctx), cache.path));
            for (File file : fileList) {
                file.delete();
            }
        }
    }

    /**
     * 检查缓存大小
     *
     * @param ctx
     */
    public static void checkSize(Context ctx) {
        if (ctx == null) return;
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        for (LocalCache cache : values()) {
            try {
                List<File> fileList = fileList(new File(getDir(ctx), cache.path));

                //文件最后修改时间排序
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return (int) (f2.lastModified() - f1.lastModified());
                    }
                });

                long size = 0;
                for (File file : fileList) {
                    if (size <= cache.maxSize) {
                        size += file.length();
                    } else file.delete();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 获得目录
     */
    private static File getDir(Context ctx) {
        if (DIR != null) return DIR;
        DIR = ctx.getCacheDir();
        DIR.mkdirs();
        return DIR;
    }

    /**
     * 获得文件列表
     */
    private static List<File> fileList(File dir) {
        List<File> fileList = new ArrayList<File>();
        if (dir == null || !dir.canWrite() || !dir.canRead()) return fileList;
        if (dir.isFile()) {
            fileList.add(dir);
            return fileList;
        }

        String fileArray[] = dir.list();
        for (String name : fileArray) {
            File file = new File(dir, name);
            if (file.isFile()) fileList.add(file);
            else fileList.addAll(fileList(file));//递归
        }

        return fileList;
    }
}
