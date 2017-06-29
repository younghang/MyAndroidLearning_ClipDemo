package com.example.yanghang.clipboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.ConnectToPC.ConnectThread;
import com.example.yanghang.clipboard.ConnectToPC.FileInfo;
import com.example.yanghang.clipboard.ConnectToPC.PhoneServer;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.ListPackage.MessageList.MessageAdapter;
import com.example.yanghang.clipboard.ListPackage.MessageList.MessageData;
import com.example.yanghang.clipboard.OthersView.CircleProgressBar;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app.SwipeBackActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityPCMessage extends SwipeBackActivity {

    private final static int REQUEST_FILE = 678;
    private static final int SEND_MESSAGE_FINISH = 123;
    private static final int SEND_MESSAGE_FAIL = 145;
    private static final int MESSAGE_FROM_PC=4512;
    private static final String  SEND_MESSAGE_PROGRESS="message progress";
    public static String DIALOG_MESSAGE = "dialog_message";
    private static String SEND_MESSAGE = "send_message";
    private static String RECEIVE_MESSAGE_FROM_PC="receive_message_from_pc";
    Toolbar toolbar;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<MessageData> messageData;
    byte[] hello;
    byte[] endBytes;
    String file;
    String fileName;
    BufferedInputStream bi;
    BufferedOutputStream bo;
    AlertDialog loadingDialog;
    private SwipeBackLayout mSwipeBackLayout;
    private ImageButton btnSend;
    private CircleProgressBar circleLoadingProgress;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int percent=data.getInt(SEND_MESSAGE_PROGRESS, -1);
            Log.v(MainFormActivity.TAG, "handle message:percent= " + percent);
            if (percent>=0&& circleLoadingProgress !=null)
            {
                Log.v(MainFormActivity.TAG, "loadingBar percent= " + percent);
                circleLoadingProgress.setProgress(percent);
            }
            int val = data.getInt(SEND_MESSAGE,0);
            if (val!=0)
            {
                switch (val) {
                    case MESSAGE_FROM_PC:
                        String message = data.getString(RECEIVE_MESSAGE_FROM_PC);
                        messageAdapter.addItem(new MessageData(MessageData.MessageType.COMPUTER,message));
                        break;
                    case SEND_MESSAGE_FAIL:
                        Toast.makeText(ActivityPCMessage.this, "发送文件失败", Toast.LENGTH_SHORT).show();
                        messageAdapter.addItem(new MessageData(MessageData.MessageType.YOU, fileName+"  发送失败"));
                        break;
                    case SEND_MESSAGE_FINISH:
                        Toast.makeText(ActivityPCMessage.this, "发送文件" + fileName + "完成", Toast.LENGTH_SHORT).show();
                        messageAdapter.addItem(new MessageData(MessageData.MessageType.YOU, fileName));
                        break;
                }
                loadingDialog.hide();
                btnSend.setEnabled(true);
            }


        }
    };
    private ImageButton btnImage;
    private ImageButton btnFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        inital();
    }

    private void inital() {


        Intent intent = getIntent();
        String dialogmessage = intent.getStringExtra(DIALOG_MESSAGE);
        if (dialogmessage == null) {
            dialogmessage = "";
        }
        toolbar = (Toolbar) findViewById(R.id.messageToolbar);
        toolbar.setTitle("Message");
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


        messageData = new ArrayList<MessageData>();
        messageData.add(new MessageData(MessageData.MessageType.YOU, dialogmessage));
//        messageData.add(new MessageData(MessageData.MessageType.COMPUTER, "nihaohsfldsfhalshfasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjfl"));
//        messageData.add(new MessageData(MessageData.MessageType.YOU, "asdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjfl"));
        recyclerView = (RecyclerView) findViewById(R.id.rv_message);
        messageAdapter = new MessageAdapter(ActivityPCMessage.this, messageData);
        messageAdapter.setOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(messageAdapter.getItemAt(pos));
                Toast.makeText(ActivityPCMessage.this, "复制到粘贴板", Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean onItemLongClick(View v, int pos) {
                return false;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        initialButtons();
        initialServer();

    }
    PhoneServer phoneServer;
    private void initialServer() {

        phoneServer=new PhoneServer(new PhoneServer.IUpdateMessage() {
            @Override
            public void updateMessage(String message) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt(SEND_MESSAGE, MESSAGE_FROM_PC);
                data.putString(RECEIVE_MESSAGE_FROM_PC,message);
                msg.setData(data);
                handler.sendMessage(msg);
            }

            @Override
            public void failedReceive() {

            }

            @Override
            public void disconnectPCtoServer() {
                phoneServer.close();

            }
        },ActivityPCMessage.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                phoneServer.setUpServer();
            }
        }).start();
    }

    private void initialButtons() {
        btnFile = (ImageButton) findViewById(R.id.btn_message_file);
        btnImage = (ImageButton) findViewById(R.id.btn_message_image);
        btnSend = (ImageButton) findViewById(R.id.btn_message_send);
        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_FILE);
            }
        });
        //     intent.setType(“image/*”);
//intent.setType(“audio/*”); //选择音频
//intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
//intent.setType(“video/*;image/*”);//同时选择视频和图片
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_FILE);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (file != null && ConnectThread.socket != null) {
                    btnSend.setEnabled(false);
                    loadingDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendFileToPC();
                        }
                    }).start();
                } else
                    Toast.makeText(ActivityPCMessage.this, "连接出错", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            endBytes = "end".getBytes("utf-8");
            hello = new byte["hello".getBytes("utf-8").length];
            bi = new BufferedInputStream(ConnectThread.socket.getInputStream());
            bo = new BufferedOutputStream(ConnectThread.socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        View view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.loading, null);
        circleLoadingProgress = (CircleProgressBar) view.findViewById(R.id.loadingCircleProgressBar);
        ProgressBar loadingProgressBar = (ProgressBar) view.findViewById(R.id.loadingProgressBar);
        EditText editeText = (EditText) view.findViewById(R.id.loadingEditText);
        editeText.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
        loadingDialog = new AlertDialog.Builder(ActivityPCMessage.this).setView(view)
                .setTitle("Loading")
                .setCancelable(false).create();


    }

    private void sendFileToPC() {
        try {
            bo.write(0x66);
            bo.flush();
            bi.read(hello);
            File sendFile = new File(file);
            String[] filePart = file.split("/");
            fileName = filePart[filePart.length - 1];
            long fileSize = sendFile.length();
            FileInfo fileInfo = new FileInfo(fileName, fileSize);
            String sendInfo = JSON.toJSONString(fileInfo);

            byte[] infoJson = sendInfo.getBytes("utf-8");
            bo.write(infoJson);
            bo.flush();
            bi.read(hello);
            int c = 0;
            byte[] buffer = new byte[4 * 1024];
            int currentProgress=0;
            long sendLength=0;
            int percent=0;
            FileInputStream fileInputStream = new FileInputStream(sendFile);
            while ((c = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                sendLength+=c;
                percent= (int) (100*1.0f*sendLength/fileSize);
                if (currentProgress<percent) {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putInt(SEND_MESSAGE_PROGRESS,percent);
                    Log.v(MainFormActivity.TAG, "percent= " + percent);
                    msg.setData(data);
                    handler.sendMessage(msg);
                    currentProgress=percent;
                }
                bo.write(buffer, 0, c);

            }
            bo.flush();

            bi.read(hello);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt(SEND_MESSAGE, SEND_MESSAGE_FINISH);
            msg.setData(data);
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt(SEND_MESSAGE, SEND_MESSAGE_FAIL);
            msg.setData(data);
            handler.sendMessage(msg);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            Log.v(MainFormActivity.TAG, "onStop send disconnect");
            bo.write(0xfc);
            bo.flush();
            ConnectThread.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FILE) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            file = FileUtils.getFilePathFromContentUri(ActivityPCMessage.this, uri);
            String s;
            if (file == null)
                s = "file null";
            else {
                s = file;
                if (!new File(file).exists())
                    s = "wrong path" + "文件选择出错，请重新选择";
            }
            Toast.makeText(ActivityPCMessage.this, s, Toast.LENGTH_SHORT).show();
        }
    }

}
