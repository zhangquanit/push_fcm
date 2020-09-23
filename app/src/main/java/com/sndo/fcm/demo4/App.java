package com.sndo.fcm.demo4;

import android.app.Application;

import com.android.util.LContext;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LContext.init(this, BuildConfig.DEBUG);
    }
}
