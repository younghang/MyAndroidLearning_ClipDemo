package com.example.yanghang.clipboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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
import com.example.yanghang.clipboard.FileUtils.AndroidFileUtil;
import com.example.yanghang.clipboard.ListPackage.MessageList.MessageAdapter;
import com.example.yanghang.clipboard.ListPackage.MessageList.MessageData;
import com.example.yanghang.clipboard.OthersView.CircleProgressBar;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app.SwipeBackActivity;

import java.io.Closeable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ActivityPCMessage extends SwipeBackActivity {

    private final static int REQUEST_FILE = 678;
    private static final int SEND_MESSAGE_FINISH = 123;
    private static final int SEND_MESSAGE_FAIL = 145;
    private static final int MESSAGE_FROM_PC=4512;
    private static final int FILE_FROM_PC=4502;
    private static final String  SEND_MESSAGE_PROGRESS="message progress";
    public static String DIALOG_MESSAGE = "dialog_message";
    private static String SEND_MESSAGE = "send_message";
    private static String RECEIVE_MESSAGE_FROM_PC="receive_message_from_pc";
    private static String RECEIVE_FILE_FROM_PC = "receive_file_from_pc";
    Toolbar toolbar;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<MessageData> messageData;
    byte[] hello;
    byte[] endBytes;
    String file;
    String fileName;
    Uri fileUri;
    long fileSize = -1;
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
                        messageAdapter.addItem(new MessageData(MessageData.MessageType.COMPUTER,message, MessageData.MessageKind.MESSAGE));
                        break;
                    case SEND_MESSAGE_FAIL:
                        Toast.makeText(ActivityPCMessage.this, "发送文件失败", Toast.LENGTH_SHORT).show();
                        messageAdapter.addItem(new MessageData(MessageData.MessageType.YOU, fileName+"  发送失败"));
                        break;
                    case SEND_MESSAGE_FINISH:
                        Toast.makeText(ActivityPCMessage.this, "发送文件" + fileName + "完成", Toast.LENGTH_SHORT).show();
                        messageAdapter.addItem(new MessageData(MessageData.MessageType.YOU, fileName));
                        break;
                    case FILE_FROM_PC:
                        String file = data.getString(RECEIVE_FILE_FROM_PC);
                        MessageData messageData = new MessageData(MessageData.MessageType.COMPUTER, file, MessageData.MessageKind.FILE);
                        messageAdapter.addItem(messageData);
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
                MessageData messageData=messageAdapter.getItem(pos);
                if (messageData.getMessageKind()== MessageData.MessageKind.MESSAGE)
                {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 将文本内容放到系统剪贴板里。
                    cm.setText(messageAdapter.getItemMessageAt(pos));
                    Toast.makeText(ActivityPCMessage.this, "复制到粘贴板", Toast.LENGTH_LONG).show();
                }
                else if(messageData.getMessageKind()== MessageData.MessageKind.FILE&&messageData.getMessageType()== MessageData.MessageType.COMPUTER)
                {
                    Intent intent=AndroidFileUtil.openFile(messageData.getMessageText());
                    startActivity(intent);
                }

            }

            @Override
            public boolean onItemLongClick(View v, int pos) {
                MessageData messageData=messageAdapter.getItem(pos);
                if (messageData.getMessageKind() == MessageData.MessageKind.MESSAGE) {
                    showMessageDialog(messageData.getMessageText());
                }
                return true;

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);
        initialButtons();
        initialServer();

    }

    private void showMessageDialog(String message) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.loading, null);
        final EditText editText = (EditText) view.findViewById(R.id.loadingEditText);
        final ProgressBar progress = (ProgressBar) view.findViewById((R.id.loadingProgressBar));
//          去掉即可复制，用于解决textView不能滑动的问题
//            editText.setMovementMethod(new ScrollingMovementMethod());
        loadingDialog = new AlertDialog.Builder(ActivityPCMessage.this).setView(view)
                .setTitle("详情")
                  .create();

        editText.setText(message);
        editText.setKeyListener(null);
        editText.setBackground(null);
        editText.setSingleLine(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextColor(getColor(R.color.message_text));
        } else {
            editText.setTextColor(getResources().getColor(R.color.message_text));
        }
        editText.setTextSize(15);
        progress.setVisibility(View.INVISIBLE);
        loadingDialog.show();
    }

    PhoneServer phoneServer;
    private void initialServer() {

        phoneServer=new PhoneServer(new PhoneServer.IUpdateMessage() {
            @Override
            public void updateMessage(String message, PhoneServer.MESSAGE_TYPE message_type) {
                Message msg = new Message();
                Bundle data = new Bundle();


                if (message_type== PhoneServer.MESSAGE_TYPE.MESSAGE)
                {
                    data.putInt(SEND_MESSAGE, MESSAGE_FROM_PC);
                    data.putString(RECEIVE_MESSAGE_FROM_PC,message);
                }
                else {
                    data.putInt(SEND_MESSAGE, FILE_FROM_PC);
                    data.putString(RECEIVE_FILE_FROM_PC,message);
                }
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, REQUEST_FILE);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fileUri != null && ConnectThread.socket != null && bi != null && bo != null) {
                    btnSend.setEnabled(false);
                    loadingDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendFileToPC();
                        }
                    }).start();
                } else
                    Toast.makeText(ActivityPCMessage.this, "未选择文件或连接出错", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            endBytes = "end".getBytes("utf-8");
            hello = new byte["hello".getBytes("utf-8").length];
            ConnectThread.socket.setSoTimeout(30000);
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
        InputStream fileInputStream = null;
        try {
            if (fileUri == null) {
                throw new IOException("file uri is null");
            }
            fileInputStream = getContentResolver().openInputStream(fileUri);
            if (fileInputStream == null) {
                throw new IOException("can not open selected file");
            }
            bo.write(0x66);
            bo.flush();
            bi.read(hello);
            FileInfo fileInfo = new FileInfo(fileName, this.fileSize);
            String sendInfo = JSON.toJSONString(fileInfo);

            byte[] infoJson = sendInfo.getBytes("utf-8");
            bo.write(infoJson);
            bo.flush();
            bi.read(hello);
            if (!isHello(hello)) {
                throw new IOException("pc refused file: " + new String(hello, "utf-8"));
            }
            int c = 0;
            byte[] buffer = new byte[4 * 1024];
            int currentProgress=0;
            long sendLength=0;
            int percent=0;
            while ((c = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                sendLength+=c;
                percent= fileSize <= 0 ? 100 : (int) (100*1.0f*sendLength/fileSize);
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
        } finally {
            closeQuietly(fileInputStream);
        }

    }

    private String getFileName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.trim().equals("")) {
                        return sanitizeFileName(name);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(cursor);
        }
        String name = uri.getLastPathSegment();
        if (name == null || name.trim().equals("")) {
            name = "file";
        }
        int split = name.lastIndexOf('/');
        if (split >= 0 && split < name.length() - 1) {
            name = name.substring(split + 1);
        }
        return sanitizeFileName(name);
    }

    private long getFileSize(Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme()) && uri.getPath() != null) {
            return new File(uri.getPath()).length();
        }
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0 && !cursor.isNull(index)) {
                    long size = cursor.getLong(index);
                    if (size >= 0) {
                        return size;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(cursor);
        }
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = getContentResolver().openAssetFileDescriptor(uri, "r");
            if (descriptor != null && descriptor.getLength() >= 0) {
                return descriptor.getLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(descriptor);
        }
        return -1;
    }

    private void cacheSelectedFile(Uri uri) throws IOException {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        File tempFile = new File(getCacheDir(), "pc_send_" + System.currentTimeMillis() + "_" + fileName);
        long total = 0;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("can not open selected file");
            }
            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8 * 1024];
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
                total += count;
            }
            outputStream.flush();
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
        fileUri = Uri.fromFile(tempFile);
        file = tempFile.getAbsolutePath();
        fileSize = total;
    }

    private String sanitizeFileName(String name) {
        return name.replace("\\", "_").replace("/", "_").replace(":", "_");
    }

    private void takeReadPermission(Intent data, Uri uri) {
        try {
            int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            if (takeFlags != 0) {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void clearSelectedFile() {
        file = null;
        fileName = null;
        fileUri = null;
        fileSize = -1;
    }

    private boolean isHello(byte[] bytes) throws IOException {
        return "hello".equals(new String(bytes, "utf-8"));
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


            Log.v(MainFormActivity.TAG, "onStop send disconnect");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (bo != null) {
                            bo.write(0xfc);
                            bo.flush();
                        }
                        if (ConnectThread.socket != null) {
                            ConnectThread.socket.close();
                        }
                        if (phoneServer != null) {
                            phoneServer.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FILE) {//是否选择，没选择就不会继续
            Uri uri = data == null ? null : data.getData();
            if (uri == null) {
                clearSelectedFile();
                Toast.makeText(ActivityPCMessage.this, "文件选择出错，请重新选择", Toast.LENGTH_SHORT).show();
                return;
            }
            takeReadPermission(data, uri);
            try {
                fileName = getFileName(uri);
                fileSize = getFileSize(uri);
                fileUri = uri;
                if (fileSize < 0) {
                    cacheSelectedFile(uri);
                }
                if (fileSize > Integer.MAX_VALUE) {
                    clearSelectedFile();
                    Toast.makeText(ActivityPCMessage.this, "文件超过2GB，暂时不能发送", Toast.LENGTH_SHORT).show();
                    return;
                }
                file = fileUri.toString();
                Toast.makeText(ActivityPCMessage.this, "已选择：" + fileName, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                clearSelectedFile();
                Toast.makeText(ActivityPCMessage.this, "文件读取失败，请重新选择", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
