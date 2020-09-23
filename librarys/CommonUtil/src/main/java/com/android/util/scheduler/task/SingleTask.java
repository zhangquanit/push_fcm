package com.android.util.scheduler.task;

/**
 * 同一时间，只有一个任务会执行
 */

public abstract class SingleTask implements Runnable, ScheduleTaskListener {
    protected String TAG = getClass().getSimpleName();
    private static boolean isRunning;
    public boolean isStarted;
    public boolean isStoped;

    public SingleTask() {
    }

    @Override
    public void run() {
        try {
            doWork();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (getClass()) {
                isRunning = false;
            }
        }
    }

    @Override
    public void start() {
        if (isStarted || isStoped) return;
        isStarted = true;
        isStoped = false;
        synchronized (getClass()) {
            if (isRunning) {
                return;
            }
            isRunning = true;
        }
        new Thread(this).start();
    }

    @Override
    public void stop() {
        isStarted = false;
        isStoped = true;
        isRunning = false;
    }

    @Override
    public void onNetChange() {

    }

    protected abstract void doWork();

}
