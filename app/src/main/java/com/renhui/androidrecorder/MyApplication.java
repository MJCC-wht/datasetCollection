package com.renhui.androidrecorder;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication sInstance;
    private Activity mCurrentActivity;

    public static MyApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }
}
