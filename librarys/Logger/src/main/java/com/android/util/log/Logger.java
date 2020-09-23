package com.android.util.log;

/**
 * 1、Logger统一对外提供API和入口
 * 2、Printer负责打印逻辑，最后调用logAdapter打印信息。默认实现是LoggerPrinter，可扩展
 * 3、LogAdapter负责实际打印，默认实现是AndroidLogAdapter，可自由扩展
 * 4、Settings负责配置信息，比如配置LogAdapter、是否显示线程信息等。
 */
public final class Logger {
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    private static final String DEFAULT_TAG = "PRETTYLOGGER";

    private static boolean logEnabled = true;

    private static Printer printer = new LoggerPrinter();

    //no instance
    private Logger() {
    }

    public static void logEnabled(boolean logEnabled) {
        Logger.logEnabled = logEnabled;
    }

    /**
     * It is used to get the settings object in order to change settings
     *
     * @return the settings object
     */
    public static Settings init() {
        return init(DEFAULT_TAG);
    }

    /**
     * It is used to change the tag
     *
     * @param tag is the given string which will be used in Logger as TAG
     */
    public static Settings init(String tag) {
        printer = new LoggerPrinter();
        return printer.init(tag);
    }

    public static void resetSettings() {
        printer.resetSettings();
    }

    public static Printer t(String tag) {
        return printer.t(tag, printer.getSettings().getMethodCount());
    }

    public static Printer t(int methodCount) {
        return printer.t(null, methodCount);
    }

    public static Printer t(String tag, int methodCount) {
        return printer.t(tag, methodCount);
    }

    //========================Log Api=================================

    public static void log(int priority, String tag, String message, Throwable throwable) {
        if (logEnabled) printer.log(priority, tag, message, throwable);
    }

    public static void d(String message, Object... args) {
        if (logEnabled) printer.d(message, args);
    }

    public static void d(Object object) {
        if (logEnabled) printer.d(object);
    }

    public static void d(String tag, String message) {
        if (logEnabled) printer.log(DEBUG, tag, message, null);
    }

    public static void e(String message, Object... args) {
        if (logEnabled) printer.e(message, args);
    }

    public static void e(Throwable throwable, Object... args) {
        if (logEnabled) printer.e(throwable, "message", args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        if (logEnabled) printer.e(throwable, message, args);
    }

    public static void e(String tag, String message) {
        if (logEnabled) printer.log(ERROR, tag, message, null);
    }

    public static void e(String tag, Throwable throwable) {
        if (logEnabled) printer.log(ERROR, tag, "message", throwable);
    }

    public static void i(String message, Object... args) {
        if (logEnabled) printer.i(message, args);
    }

    public static void i(String tag, String message) {
        if (logEnabled) printer.log(INFO, tag, message, null);
    }

    public static void v(String message, Object... args) {
        if (logEnabled) printer.v(message, args);
    }

    public static void v(String tag, String message) {
        if (logEnabled) printer.log(VERBOSE, tag, message, null);
    }

    public static void w(String message, Object... args) {
        if (logEnabled) printer.w(message, args);
    }

    public static void w(String tag, String message) {
        if (logEnabled) printer.log(WARN, tag, message, null);
    }

    public static void wtf(String message, Object... args) {
        if (logEnabled) printer.wtf(message, args);
    }

    public static void json(String json) {
        if (logEnabled) printer.json(json);
    }

    public static void json(String tag, String json) {
        if (logEnabled) printer.json(tag, json);
    }

    public static void xml(String xml) {
        if (logEnabled) printer.xml(xml);
    }

}
