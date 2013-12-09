package com.grootcode.android.util;

import android.content.Context;

public class BuildConfigUtils {
    public static boolean BUILD_CONFIG_DEBUG = false;

    public static void init(Context context) throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        Class<?> buildConfigClass = Class.forName(context.getPackageName() + ".BuildConfig");
        BUILD_CONFIG_DEBUG = buildConfigClass.getField("DEBUG").getBoolean(null);
    }
}
