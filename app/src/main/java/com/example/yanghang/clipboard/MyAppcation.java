package com.example.yanghang.clipboard;

import android.app.Application;

/**
 * Created by young on 2017/6/13.
 */

public class MyAppcation extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }

}