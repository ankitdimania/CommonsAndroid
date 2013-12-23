package com.grootcode.android.util;

import java.util.Locale;

import android.text.TextUtils;
import android.util.Log;

public class LogUtils {
    private static String LOG_PREFIX = "";
    private static int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static int MAX_LOG_TAG_LENGTH = 23;

    public static void init(String logPrefix) {
        LOG_PREFIX = logPrefix;
        LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    }

    public static void init(String logPrefix, int maxLogTagLength) {
        init(logPrefix);
        MAX_LOG_TAG_LENGTH = maxLogTagLength;
    }

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class<?> cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(final String tag, String format, Object... args) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfigUtils.BUILD_CONFIG_DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, buildMessage(format, args));
        }
    }

    public static void LOGD(final String tag, String message, Throwable cause) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfigUtils.BUILD_CONFIG_DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, buildMessage(message), cause);
        }
    }

    public static void LOGD(final String tag, Throwable cause, String format, Object... args) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfigUtils.BUILD_CONFIG_DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, buildMessage(format, args), cause);
        }
    }

    public static void LOGV(final String tag, String format, Object... args) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfigUtils.BUILD_CONFIG_DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, buildMessage(format, args));
        }
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfigUtils.BUILD_CONFIG_DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, buildMessage(message), cause);
        }
    }

    public static void LOGV(final String tag, Throwable cause, String format, Object... args) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfigUtils.BUILD_CONFIG_DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, buildMessage(format, args), cause);
        }
    }

    public static void LOGI(final String tag, String format, Object... args) {
        Log.i(tag, buildMessage(format, args));
    }

    public static void LOGI(final String tag, Throwable cause, String format, Object... args) {
        Log.i(tag, buildMessage(format, args), cause);
    }

    public static void LOGI(final String tag, String message, Throwable cause) {
        Log.i(tag, buildMessage(message), cause);
    }

    public static void LOGW(final String tag, String format, Object... args) {
        Log.w(tag, buildMessage(format, args));
    }

    public static void LOGW(final String tag, String message, Throwable cause) {
        Log.w(tag, buildMessage(message), cause);
    }

    public static void LOGW(final String tag, Throwable cause, String format, Object... args) {
        Log.w(tag, buildMessage(format, args), cause);
    }

    public static void LOGE(final String tag, String format, Object... args) {
        Log.e(tag, buildMessage(format, args));
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        Log.e(tag, buildMessage(message), cause);
    }

    public static void LOGE(final String tag, Throwable cause, String format, Object... args) {
        Log.e(tag, buildMessage(format, args), cause);
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(String format, Object... args) {
        String msg = (args == null || args.length == 0) ? format : String.format(Locale.US, format, args);
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] trace = currentThread.getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of VolleyLog.
        // It will be at least two frames up, so start there.
        for (int i = 2; i < trace.length; ++i) {
            String clazz = trace[i].getClassName();
            if (!TextUtils.equals(clazz, LogUtils.class.getName())) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName() + ":" + trace[i].getLineNumber();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", currentThread.getId(), caller, msg);
    }

    private LogUtils() {}
}
