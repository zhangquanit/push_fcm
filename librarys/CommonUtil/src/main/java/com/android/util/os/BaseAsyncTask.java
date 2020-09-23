package com.android.util.os;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

/**
 * @ClassName: BaseAsyncTask
 * @Description: 屏蔽掉系统版本10之后异步任务串行执行。注意：执行该异步任务时请调用 executeParallel 方法，不要直接调用 父类execute 方法
 */
public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    @SuppressLint("NewApi")
    public final AsyncTask<Params, Progress, Result> executeParallel(Params... params) {
        if (Build.VERSION.SDK_INT > 10) {
            return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            return super.execute(params);
        }
    }
}
