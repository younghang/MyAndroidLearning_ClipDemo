package com.example.yanghang.myapplication.ConnectToPC;

import android.util.Log;

import com.example.yanghang.myapplication.MainFormActivity;

import java.io.BufferedOutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by yanghang on 2016/11/21.
 */
public class ConnectThread extends Thread {
    public MessageInformation strMessage;
    OnConnect onConnect;

    public ConnectThread(OnConnect onConnect) {
        this.onConnect = onConnect;
    }

    @Override
    public void run() {
        super.run();

        if (strMessage != null) {
            Log.v(MainFormActivity.MTTAG, "send message:" + strMessage.Message);
            SendMessage();
        }

    }

    private void SendMessage() {
        Socket socket = null;
        try {
            socket = new Socket(strMessage.IP, strMessage.Port);
            String strinfo = strMessage.Message;
            byte[] buffers = strinfo.getBytes("UTF-8");
            BufferedOutputStream bf = new BufferedOutputStream(socket.getOutputStream());
            bf.write(buffers);
            bf.close();
            socket.close();
            onConnect.OnSuccess();

        } catch (ConnectException e) {
            Log.v(MainFormActivity.MTTAG, "连接失败");
            onConnect.OnFail("连接失败");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.v(MainFormActivity.MTTAG, "发送失败");
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
