package com.example.yanghang.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yanghang.clipboard.ConnectToPC.ConnectThread;
import com.example.yanghang.clipboard.ConnectToPC.OnConnect;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app.SwipeBackActivity;

public class ActivityConnect extends SwipeBackActivity {

    private static final int CONNECT_SUCCESS = 1;
    private static final int CONNECT_FAILED = 0;
    private static final String IP_SETTING="this is ip key";
    private static final String Port_SETTING="this is port key";
    EditText editIP;
    EditText editPort;
    Button btnConnect;
    TextView tvProgressInfo;
    ProgressBar progressBar;
    Handler  handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int val = data.getInt("i");
            btnConnect.setEnabled(true);
            switch (val) {
                case ActivityConnect.CONNECT_FAILED:
                    String errorMessage = data.getString("msg");
                    tvProgressInfo.setText(errorMessage);
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case ActivityConnect.CONNECT_SUCCESS:
                    progressBar.setVisibility(View.INVISIBLE);
                    tvProgressInfo.setText("发送成功");
                    PreferenceManager.getDefaultSharedPreferences(ActivityConnect.this).edit().putString(IP_SETTING, editIP.getText().toString()).apply();
                    PreferenceManager.getDefaultSharedPreferences(ActivityConnect.this).edit().putString(Port_SETTING, editPort.getText().toString()).apply();

                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 将文本内容放到系统剪贴板里。
                    String strinfo = cm.getPrimaryClip().getItemAt(0).getText().toString();
                    Intent intent2 = new Intent(ActivityConnect.this, ActivityPCMessage.class);
                    intent2.putExtra(ActivityPCMessage.DIALOG_MESSAGE, strinfo);
                    startActivity(intent2);

                    break;
            }

        }
    };
    private SwipeBackLayout mSwipeBackLayout;
    private OnConnect onConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Initial();

    }

    void Initial() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.connect_toolbar);
        toolbar.setTitle("ConnectPC");
        toolbar.setTitleTextColor(getColor(R.color.white));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSwipeBackLayout = getSwipeBackLayout();
        //设置可以滑动的区域，推荐用屏幕像素的一半来指定
        mSwipeBackLayout.setEdgeSize(100);
        //设定滑动关闭的方向，SwipeBackLayout.EDGE_ALL表示向下、左、右滑动均可。EDGE_LEFT，EDGE_RIGHT，EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_BOTH);
        editIP = (EditText) findViewById(R.id.ed_ip);
        editPort = (EditText) findViewById(R.id.ed_port);
        String IP=PreferenceManager.getDefaultSharedPreferences(ActivityConnect.this).getString(IP_SETTING, "192.168.23.2") ;
        String Port=PreferenceManager.getDefaultSharedPreferences(ActivityConnect.this).getString(Port_SETTING, "20040") ;
        editIP.setText(IP);
        editPort.setText(Port);
        btnConnect = (Button) findViewById(R.id.btn_Connect);
        tvProgressInfo = (TextView) findViewById(R.id.tv_ConnnectInfo);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_Connect);
        onConnect = new OnConnect() {

            @Override
            public void OnSuccess() {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("i", ActivityConnect.CONNECT_SUCCESS);
                msg.setData(data);
                handler.sendMessage(msg);

            }

            @Override
            public void OnFail(String errorMessage) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("i", ActivityConnect.CONNECT_FAILED);
                data.putString("msg", errorMessage);
                msg.setData(data);
                handler.sendMessage(msg);

            }

        };

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                btnConnect.setBackground(getDrawable(R.drawable.corner_background_green_dark));
                btnConnect.setEnabled(false);
                tvProgressInfo.setText("连接发送中");
                ConnectThread connectThread=new ConnectThread(onConnect);
                String strinfo = "";
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                try {
                    ClipData.Item cp = cm.getPrimaryClip().getItemAt(0);
                    strinfo = cp.getText().toString();
                    connectThread.strMessage = new ConnectThread.MessageInformation(editIP.getText().toString(), Integer.parseInt(editPort.getText().toString()), strinfo);
                    connectThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityConnect.this, "没有信息可发送", Toast.LENGTH_SHORT).show();
                    onConnect.OnFail("无信息");
                }

            }
        });
    }


}

