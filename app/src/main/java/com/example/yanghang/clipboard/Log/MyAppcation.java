package com.example.yanghang.clipboard.Log;

import android.app.Application;

import com.example.yanghang.clipboard.Log.CrashHandler;


/**
 * Created by young on 2017/6/13.
 */

public class MyAppcation extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
//     no use    ActivitySwitcher.getInstance().init(this);
    }

}