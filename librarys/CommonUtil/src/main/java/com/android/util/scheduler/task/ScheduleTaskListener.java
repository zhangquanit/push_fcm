package com.android.util.scheduler.task;

/**
 * 后台任务
 *
 * @author 张全
 */
public interface ScheduleTaskListener {
    /**
     * 开始任务
     */
    void start();

    /**
     * 停止任务
     */
    void stop();

    /**
     * 网络从断开变为可用
     */
    void onNetChange();

}
