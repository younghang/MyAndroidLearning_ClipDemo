package com.example.yanghang.myapplication;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.net.Socket;

/**
 * Created by yanghang on 2016/11/21.
 */
public class ConnectThread  extends Thread{
    public MessageInformation strMessage;
    OnConnect onConnect;
    public ConnectThread(OnConnect onConnect){
        this.onConnect=onConnect;
    }

    @Override
    public void run() {
        super.run();
        try {
            if (strMessage != null) {
                Log.v(MainFormActivity.MTTAG, "send message:" + strMessage.Message);
                SendMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        onConnect.OnSuccess();
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
        } catch (Exception e) {
            // TODO Auto-generated catch block
            onConnect.OnFail();
            e.printStackTrace();
        }

    }


}
