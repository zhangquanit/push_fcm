package com.android.util.scheduler.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台任务管理器
 * <p>
 * <ul>
 * <li>
 * 1、主要负责管理后台任务(目前运行在CoreService中)，需要添加后台任务时，请继承ScheduleTask(定时任务)或SingleTask
 * (非定时任务,在CoreService的onCreate中运行一次).</li>
 * <li>2、默认在CoreService的onCreate中开始所有注册过的任务，
 * 之后注册的任务只能在CoreService的onStartCommand中检测执行
 * ，也可以通过startTask开始指定任务，stopTask停止指定任务，当断网重连时，会调用onNetChange，
 * 当CoreService销毁时，自动停止所有任务。</li>
 * <li>3、默认在CoreService中添加了WeatherScheduleTask和ClearCacheTask，请无需再次添加</li>
 * <li>4、本任务管理器并不是一定非依赖于CoreService，而是可以运行在任何地方</li>
 * </ul>
 * </p>
 *
 * @author 张全
 */
public class ScheduleTaskManager {
    private Map<String, ScheduleTaskListener> taskMap = Collections.synchronizedMap(new HashMap<String, ScheduleTaskListener>());
    private static ScheduleTaskManager manager = null;

    private ScheduleTaskManager() {
    }

    public static ScheduleTaskManager getInstance() {
        if (null == manager) {
            synchronized (ScheduleTaskManager.class) {
                if (null == manager) {
                    manager = new ScheduleTaskManager();
                }
            }
        }
        return manager;
    }

    /**
     * 开始所有任务
     */
    public void startAll() {
        for (Map.Entry<String, ScheduleTaskListener> taskEntry : taskMap.entrySet()) {
            taskEntry.getValue().start();
        }
    }

    /**
     * 停止所有任务
     */
    public void stopAll() {
        for (Map.Entry<String, ScheduleTaskListener> taskEntry : taskMap.entrySet()) {
            taskEntry.getValue().stop();
        }
    }

    /**
     * 网络重连
     */
    public void onNetChange() {
        for (Map.Entry<String, ScheduleTaskListener> taskEntry : taskMap.entrySet()) {
            taskEntry.getValue().onNetChange();
        }
    }

    /**
     * 开始某个任务
     *
     * @param task
     */
    public void startTask(Class<ScheduleTaskListener> task) {
        String taskName = task.getSimpleName();
        if (taskMap.containsKey(taskName)) {
            taskMap.get(taskName).start();
        }
    }

    /**
     * 停止某个任务
     *
     * @param task
     */
    public void stopTask(Class<ScheduleTaskListener> task) {
        String taskName = task.getSimpleName();
        if (taskMap.containsKey(taskName)) {
            taskMap.get(taskName).stop();
        }
    }

    /**
     * 移除任务
     *
     * @param taskCls
     */
    public void removeTask(Class<ScheduleTaskListener> taskCls) {
        String taskName = taskCls.getSimpleName();
        if (taskMap.containsKey(taskName)) {
            taskMap.remove(taskName);
        }
    }

    /**
     * 添加任务
     *
     * @param task
     */
    public void addTask(ScheduleTaskListener task) {
        String taskName = task.getClass().getSimpleName();
        taskMap.put(taskName, task);
    }

    public void release() {
        stopAll();
        taskMap.clear();
        taskMap = null;
        manager = null;
    }
}
