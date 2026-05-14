package com.example.yanghang.clipboard.Log;

import android.app.Application;
import android.content.Context;

import com.example.yanghang.clipboard.Log.CrashHandler;


/**
 * Created by young on 2017/6/13.
 */

public class MyApplication extends Application {
    private static Context appContext;
    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;//存储引用
        appContext = getApplicationContext();  // 初始化全局 Context
//        CrashHandler.getInstance().init(this);
//     no use    ActivitySwitcher.getInstance().init(this);
    }
    public static MyApplication getInstance(){
        return instance;
    }
    public static Context getAppContext() {
        return appContext;
    }
}