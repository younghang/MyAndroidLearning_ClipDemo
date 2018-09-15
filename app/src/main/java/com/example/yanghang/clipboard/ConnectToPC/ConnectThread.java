package com.example.yanghang.clipboard.ConnectToPC;

import android.util.Log;

import com.example.yanghang.clipboard.MainFormActivity;

import java.io.BufferedOutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by yanghang on 2016/11/21.
 */
public class ConnectThread extends Thread {
    public static Socket socket = null;
    public MessageInformation strMessage;
    OnConnect onConnect;

    public ConnectThread(OnConnect onConnect) {
        this.onConnect = onConnect;
    }

    public static void closeSocket() {
        try {
            if (socket!=null)
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        if (strMessage != null) {
//            Log.v(MainFormActivity.TAG, "send message:" + strMessage.Message);
            SendMessage();
        }

    }

    private void SendMessage() {

        try {
//            Log.v(MainFormActivity.TAG, "开始连接Server IP= "+strMessage.IP);
            socket = new Socket(strMessage.IP, strMessage.Port);
//            Log.v(MainFormActivity.TAG, "本地IP= "+socket.getLocalAddress());
            socket.setSoTimeout(2000);
            String strinfo = strMessage.Message;
            byte[] buffers = strinfo.getBytes("UTF-8");
//            Log.v(MainFormActivity.TAG, "转换字节准备发送");
            BufferedOutputStream bf = new BufferedOutputStream(socket.getOutputStream());
            bf.write(buffers);
            bf.flush();
            onConnect.OnSuccess();

        } catch (ConnectException e) {
            e.printStackTrace();
            Log.v(MainFormActivity.TAG, "连接失败");
            onConnect.OnFail("连接失败");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.v(MainFormActivity.TAG, "发送失败");
            onConnect.OnFail("发送失败");
            e.printStackTrace();
        }


    }

    public static class MessageInformation {
        public String IP;
        public int Port;
        public String Message;

        public MessageInformation(String IP, int port, String message) {
            this.IP = IP;
            Port = port;
            Message = message;
        }
    }



}
