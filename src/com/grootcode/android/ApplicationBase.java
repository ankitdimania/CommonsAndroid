package com.grootcode.android;

import android.app.Application;


public class ApplicationBase extends Application {
    private static ApplicationBase sApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        sApplication = this;
    }
    synchronized public static Application getApplication() {
        return sApplication;
    }
}
