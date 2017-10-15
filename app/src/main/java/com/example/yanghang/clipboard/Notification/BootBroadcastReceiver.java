package com.example.yanghang.clipboard.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by young on 2017/10/10.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("nihao", "BootBroadcastReceiver---onReceive: ");
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_todo_switch",true))
        return;
        //后边的XXX.class就是要启动的服务
        Intent service = new Intent(context,ServiceNotification.class);
        //启动应用，参数为需要自动启动的应用的包名
        intent.setAction("TodoNotification.ScreenLock.Service");
        context.startService(service);
    }

}