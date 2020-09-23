package com.android.util.scheduler.task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.android.util.LContext;
import com.android.util.log.LogUtil;
import com.android.util.os.NetworkUtil;

/**
 * 定时任务
 *
 * @author zhangquan
 */
public abstract class ScheduleTask implements ScheduleTaskListener {
    private AlarmManager mAlarmManger = null;
    private PendingIntent pi = null;
    private String actionStr = getClass().getName();
    private PowerManager.WakeLock wl = null;
    public boolean isStarted;
    public boolean isStoped;

    public ScheduleTask() {
        PowerManager pm = (PowerManager) LContext.getContext().getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, actionStr);
        wl.setReferenceCounted(false);
    }

    /**
     * 开启定时定位工作
     */
    @Override
    public void start() {
        if (isStarted || isStoped) return;
        isStarted = true;
        isStoped = false;
        Context ctx = LContext.getContext();
        mAlarmManger = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        ctx.registerReceiver(timerReceiver, new IntentFilter(actionStr));
        pi = PendingIntent.getBroadcast(ctx, 0, new Intent(actionStr), PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManger.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, getScheduleTime(), pi);
        LogUtil.d(getClass().getName() + "................schedule......start");
    }

    /**
     * 停止定时定位工作
     */
    @Override
    public void stop() {
        isStarted = false;
        isStoped = true;
        try {
            LContext.getContext().unregisterReceiver(timerReceiver);
            mAlarmManger.cancel(pi);
            releaseWackLock();
            LogUtil.d(getClass().getName() + "................schedule......end");
        } catch (Exception e) {
        }
    }

    @Override
    public void onNetChange() {

    }

    protected void getWackLock() {
        wl.acquire();
    }

    protected void releaseWackLock() {
        if (wl != null && wl.isHeld())
            wl.release();
    }

    BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            LogUtil.d(getClass().getName() + "................doTask");
            if (NetworkUtil.isNetworkAvailable(LContext.getContext())) {
                doTask();
            }
        }
    };

    /**
     * 获取定时工作间隔时长
     */
    public abstract long getScheduleTime();

    /**
     * 执行任务
     */
    public abstract void doTask();

}
