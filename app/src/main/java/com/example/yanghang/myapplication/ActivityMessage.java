package com.example.yanghang.myapplication;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yanghang.myapplication.FileUtils.FileUtils;
import com.example.yanghang.myapplication.ListPackage.MessageList.MessageAdapter;
import com.example.yanghang.myapplication.ListPackage.MessageList.MessageData;
import com.example.yanghang.myapplication.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.myapplication.OthersView.swipebacklayout.lib.app.SwipeBackActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityMessage extends SwipeBackActivity {

    private final static int REQUEST_FILE = 678;
    public static String DIALOG_MESSAGE = "dialog_message";
    Toolbar toolbar;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<MessageData> messageData;
    private SwipeBackLayout mSwipeBackLayout;
    private ImageButton btnSend;
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
        messageData.add(new MessageData(MessageData.MessageType.COMPUTER, "nihaohsfldsfhalshfasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjfl"));
        messageData.add(new MessageData(MessageData.MessageType.YOU, "asdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjflasdfasfalskdfjlasjfsdjfl"));
        recyclerView = (RecyclerView) findViewById(R.id.rv_message);
        messageAdapter = new MessageAdapter(ActivityMessage.this, messageData);
        messageAdapter.setOnItemClickListener(new MessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(messageAdapter.getItemAt(pos));
                Toast.makeText(ActivityMessage.this, "复制到粘贴板", Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean onItemLongClick(View v, int pos) {
                return false;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        initialButtons();

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FILE) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String file = FileUtils.getFilePathFromContentUri(ActivityMessage.this, uri);
            String s;
            if (file == null)
                s = "file null";
            else s = file;
            if (!new File(file).exists())
                s = "wrong path";
            Toast.makeText(ActivityMessage.this, s, Toast.LENGTH_SHORT).show();
        }
    }
}
