package com.example.yanghang.myapplication;

/**
 * Created by yanghang on 2016/11/21.
 */
public class ConnectThread  extends Thread{
    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onConnect.OnSuccess();
    }
    OnConnect onConnect;
    public ConnectThread(OnConnect onConnect){
        this.onConnect=onConnect;
    }
}
