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
            socket = new Socket(strMessage.IP, strMessage.Port);
            socket.setSoTimeout(5000);
            String strinfo = strMessage.Message;
            byte[] buffers = strinfo.getBytes("UTF-8");
            BufferedOutputStream bf = new BufferedOutputStream(socket.getOutputStream());
            bf.write(buffers);
            bf.flush();
            onConnect.OnSuccess();

        } catch (ConnectException e) {
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
