package com.android.util.os;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 进程工具
 */
public class ProcessUtil {
    private static final Pattern pattern = Pattern.compile("[\\s]+");

    /**
     * 杀死进程
     *
     * @param pid
     */
    public static final void kill(int pid) {
        try {
            android.os.Process.killProcess(pid);
        } catch (Exception e) {
        }

        Process process = null;
        try {
            process = Runtime.getRuntime().exec("kill -9 " + pid);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null && process.exitValue() != 0) {
                process.destroy();
                process = null;
            }
        }
    }

    /**
     * 获得进程pid列表
     *
     * @return List<Integer>
     */
    public static final List<Integer> getPids(String name) {
        List<Integer> pidList = new ArrayList<Integer>();
        if (TextUtils.isEmpty(name)) return pidList;
        Process process = null;
        InputStream input = null;
        try {
            process = Runtime.getRuntime().exec("ps");
            input = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            int rowIndex = 0;
            while ((line = br.readLine()) != null) {
                if ((rowIndex++) <= 0) continue;
                String[] split = pattern.split(line);
                try {
                    if (split != null && split.length == 9) {
                        if (name.equals(split[8])) {
                            pidList.add(Integer.valueOf(split[1]));
                        }
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) try {
                input.close();
            } catch (Exception e) {
            }

            if (process != null && process.exitValue() != 0) {
                process.destroy();
                process = null;
            }
        }

        Collections.sort(pidList, new Comparator<Integer>() {
            @Override
            public int compare(Integer obj1, Integer obj2) {
                return obj1 - obj2;
            }
        });

        return pidList;
    }
}
