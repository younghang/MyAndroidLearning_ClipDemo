package com.example.yanghang.myapplication;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.yanghang.myapplication.ListPackage.MessageList.MessageAdapter;
import com.example.yanghang.myapplication.ListPackage.MessageList.MessageData;
import com.example.yanghang.myapplication.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.myapplication.OthersView.swipebacklayout.lib.app.SwipeBackActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityMessage extends SwipeBackActivity {

    public static String DIALOG_MESSAGE = "dialog_message";

    Toolbar toolbar;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<MessageData> messageData;
    private SwipeBackLayout mSwipeBackLayout;
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

    }
}
