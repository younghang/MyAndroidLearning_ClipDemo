package com.example.yanghang.clipboard.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.ListPackage.DailyTaskList.DailyTaskData;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.R;
import com.example.yanghang.clipboard.Task.TaskShowToDoList;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class ServiceNotification extends Service {
    public ServiceNotification() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private static final int NOTIFICATION_NOTIFY = 666;
    private static final String MSG_TODAY_MISSION = "msg_today_mission";
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initial();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mScreenOnReceiver);
    }

    void initial() {
        //动态注册广播接受者,监听周期与该注册者同生命周期
//        ScreenLockNotificationReceiver receiver = new ScreenLockNotificationReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.msgnotification");
//        this.registerReceiver(receiver, filter);
        //只能动态注册屏幕亮起广播
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("notification_screen_lock_switch",true))
        {
                    /*注册锁屏广播*/
            IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
//            if (!mScreenOnReceiver.isOrderedBroadcast())
            this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);

        }
        startMission();

    }
    String message;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            message = msg.getData().getString(MSG_TODAY_MISSION);
            //发送通知栏通知
            showTodayMissionNotification(message);
            //发送广播通知，显示锁屏通知,还不如直接启动


            Log.d("nihao", "ServiceNotification---handleMessage: ");
//            intent.setAction("android.intent.action.msgnotification");//action与接收器相同
//            getApplicationContext().sendBroadcast(intent);
            //


        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    void showTodayMissionNotification(String message) {
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
//                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.splash))
                .setSmallIcon(R.drawable.rest)
                .setTicker("今日任务")
//                    .setContentInfo(message)
                .setContentTitle("今日任务").setContentText(message)

//                .setAutoCancel(true)
                .setVisibility(VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_ALL)
//                    .setLights(Color.GREEN,0,0)
                .build();

//        notification.flags |= Notification.FLAG_INSISTENT;

        notificationManager.notify(NOTIFICATION_NOTIFY, notification);

    }

    void startMission() {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                new TaskShowToDoList(getApplicationContext(), new TaskShowToDoList.IShowToDoList() {
                    @Override
                    public void showToDoList(String messageToDoList) {
                        //nothing need to be done
                    }

                    @Override
                    public void showDailyList(List<DailyTaskData> mDailyList, ListData todayListData) {
                        //nothing need to be done
                    }

                    @Override
                    public void showTodayMission(String todayMission) {
                        //this is the message that will be shown when the device reboot
                        if (todayMission.equals(""))
                        {
                            final Intent intent = new Intent();
                            // 为Intent设置Action属性
                            intent.setAction("TodoNotification.ScreenLock.Service");
                            intent.setPackage(getPackageName());
                            stopService(intent);
                            return;
                        }
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString(MSG_TODAY_MISSION, todayMission);
                        msg.setData(data);
                        handler.sendMessage(msg);

                    }
                }).runToDoListCheck();
            }
        };
        handler.postDelayed(runnable,2000);
    }

    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentActivity = new Intent(context,ActivityNotification.class);
            intentActivity.putExtra("message", message);
            intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentActivity);
        }
    };

}