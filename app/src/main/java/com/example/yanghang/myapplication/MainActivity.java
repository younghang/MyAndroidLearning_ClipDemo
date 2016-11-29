package com.example.yanghang.myapplication;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int CONNECT_SUCCESS = 1;
    private static final int CONNECT_FAILED = 0;
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
                case MainActivity.CONNECT_FAILED:
                    tvProgressInfo.setText("连接失败");
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case MainActivity.CONNECT_SUCCESS:
                    progressBar.setVisibility(View.INVISIBLE);
                    tvProgressInfo.setText("发送成功");
//                    Intent intent = new Intent(MainActivity.this, ActivityEditInfo.class);
//                    startActivity(intent);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Initial();

    }

    void Initial() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.connect_toolbar);
        toolbar.setTitle("ConnectPC");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        editIP = (EditText) findViewById(R.id.ed_ip);
        editPort = (EditText) findViewById(R.id.ed_port);
        btnConnect = (Button) findViewById(R.id.btn_Connect);
        tvProgressInfo = (TextView) findViewById(R.id.tv_ConnnectInfo);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_Connect);
        final OnConnect onConnect= new OnConnect() {
            @Override
            void OnSuccess() {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("i", MainActivity.CONNECT_SUCCESS);
                msg.setData(data);
                handler.sendMessage(msg);

            }

            @Override
            void OnFail() {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("i", MainActivity.CONNECT_FAILED);
                msg.setData(data);
                handler.sendMessage(msg);

            }
        };

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                btnConnect.setEnabled(false);
                tvProgressInfo.setText("连接发送中");
                ConnectThread connectThread=new ConnectThread(onConnect);
                String strinfo = "";
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                strinfo = cm.getPrimaryClip().getItemAt(0).getText().toString();
                connectThread.strMessage = new MessageInformation(editIP.getText().toString(), Integer.parseInt(editPort.getText().toString()), strinfo);
                connectThread.start();
            }
        });
    }


}

class MessageInformation {
    public String IP;
    public int Port;
    public String Message;

    public MessageInformation(String IP, int port, String message) {
        this.IP = IP;
        Port = port;
        Message = message;
    }
}
