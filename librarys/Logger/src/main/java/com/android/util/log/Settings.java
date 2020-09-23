package com.android.util.log;

public final class Settings {

    private int methodCount = 2;
    private boolean showThreadInfo = true; //是否显示线程信息
    private int methodOffset = 0;
    private LogAdapter logAdapter; //Log打印实现
    private LogLevel logLevel = LogLevel.FULL; //日志级别

    public Settings hideThreadInfo() {
        showThreadInfo = false;
        return this;
    }

    public Settings methodCount(int methodCount) {
        if (methodCount < 0) {
            methodCount = 0;
        }
        this.methodCount = methodCount;
        return this;
    }

    public Settings logLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public Settings methodOffset(int offset) {
        this.methodOffset = offset;
        return this;
    }

    public Settings logAdapter(LogAdapter logAdapter) {
        this.logAdapter = logAdapter;
        return this;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public boolean isShowThreadInfo() {
        return showThreadInfo;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public int getMethodOffset() {
        return methodOffset;
    }

    public LogAdapter getLogAdapter() {
        if (logAdapter == null) {
            logAdapter = new AndroidLogAdapter();
        }
        return logAdapter;
    }

    public void reset() {
        methodCount = 2;
        methodOffset = 0;
        showThreadInfo = true;
        logLevel = LogLevel.FULL;
    }
}
