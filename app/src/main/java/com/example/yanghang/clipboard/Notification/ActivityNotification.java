package com.example.yanghang.clipboard.Notification;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app.SwipeBackActivity;
import com.example.yanghang.clipboard.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ActivityNotification extends SwipeBackActivity {

    String message;
    TextView tvMessage;
    private SwipeBackLayout mSwipeBackLayout;
    private LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON

        );

        //隐藏导航栏
        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
        }
        setContentView(R.layout.activity_notification);
        Bundle bundle=getIntent().getExtras();
        if (bundle!=null)
        message = bundle.getString("message","The world is not that one.");
        else
            message="The world is not that one.";
        Log.d("nihao", "ActivityNotification   message:" + message);
        initial();

    }


    void initial()
    {
        mSwipeBackLayout = getSwipeBackLayout();
        //设置可以滑动的区域，推荐用屏幕像素的一半来指定
        mSwipeBackLayout.setEdgeSize(100);
        //设定滑动关闭的方向，SwipeBackLayout.EDGE_ALL表示向下、左、右滑动均可。EDGE_LEFT，EDGE_RIGHT，EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_BOTH);

        linearLayout = (LinearLayout) findViewById(R.id.activity_notification_linearLayout);

        tvMessage= (TextView) findViewById(R.id.activity_notification_tv_message);
        tvMessage.setText(message);

    }



//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//        if (!pm.isScreenOn()) {
//            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
//                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
//            wl.acquire();
//            wl.release();
//        }
//    }


}
