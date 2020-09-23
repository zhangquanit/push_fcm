package com.android.util.os;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CPU状态
 */
public class CpuUtil {
    private static long idle = 0;
    private static long total = 0;
    private static double usage = 0;

    private CpuUtil() {
    }

    /**
     * 获取总内存大小
     *
     * @return
     */
    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();
        } catch (IOException e) {

        }

        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 获取可用内存
     *
     * @return
     */
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * 获取使用率
     *
     * @return
     */
    public static double getUsage() {
        readUsage();
        return usage;
    }

    /**
     * 使用内存占用百分比
     */
    private static void readUsage() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" ");

            long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
            long currIdle = Long.parseLong(toks[5]);

            usage = (currTotal - total) * 100.0f / (currTotal - total + currIdle - idle);
            total = currTotal;
            idle = currIdle;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
