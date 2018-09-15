package com.example.yanghang.clipboard.ConnectToPC;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by young on 2017/2/27.
 */

public class PhoneServer {

    public enum MESSAGE_TYPE{
        FILE,MESSAGE
    }

    public interface IUpdateMessage{
        void updateMessage(String message,MESSAGE_TYPE message_type);
        void failedReceive();
        void disconnectPCtoServer();
    };
    IUpdateMessage updateMessage;
    private ServerSocket ss;
    private Socket socket;
    private BufferedInputStream bi;
    private BufferedOutputStream bo;
    private int serverPort=20600;
    private byte[] receiveHello ;
    private byte[] receiveSize=new byte[4];
    int receiveFileLength;//最大两个G左右
    private Context context;

    public static boolean isRunning=false;
    public PhoneServer(IUpdateMessage dealMessage, Context mContext) {
        this.updateMessage=dealMessage;
        this.context=mContext;
        try {
            receiveHello= "hello".getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void setUpServer() {

        try {
            ss = new ServerSocket(serverPort);
            socket = ss.accept();
            bi = new BufferedInputStream(socket.getInputStream());
            bo = new BufferedOutputStream(socket.getOutputStream());
            receiveData();
        } catch (IOException e) {
            updateMessage.updateMessage("启动端口失败，请退出程序",MESSAGE_TYPE.MESSAGE);
            Toast.makeText(context,"启动端口失败，请退出程序",Toast.LENGTH_SHORT).show();
            updateMessage.disconnectPCtoServer();
            e.printStackTrace();
        }
    }




    private void receiveData()
    {
        try {

            byte []receiveType=new byte[1];
            bi.read(receiveType);
            bo.write(receiveHello);
            bo.flush();
            switch (receiveType[0])
            {
                case (byte)0xfc:
                    Log.v(MainFormActivity.TAG,"PhoneServer  receiveData: closeType");
                    close();
                    break;
                case (byte)0x66:
                    Log.v(MainFormActivity.TAG,"PhoneServer  receiveData: fileType");
                    receiveFile();
                    break;
                case (byte)0x60:
                    Log.v(MainFormActivity.TAG,"PhoneServer  receiveData: messageType");
                    receiveMessage();
                    break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void receiveMessage() {
        isRunning=true;
        try {
            bi.read(receiveSize);
            bo.write(receiveHello);
            bo.flush();
            receiveFileLength=byteArrayToInt(receiveSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int c=0;
        int to=0;
        byte[] buffer = new byte[1024];
        String outPutString="";
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            while (to<receiveFileLength&&(c = bi.read(buffer, 0, buffer.length))!=-1) {
                bas.write(buffer,0,c);
                to+=c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outPutString=bas.toString("utf-8");
            updateMessage.updateMessage(outPutString,MESSAGE_TYPE.MESSAGE);
            Log.v(MainFormActivity.TAG,"PhoneServer  receiveMessage:  "+outPutString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        isRunning=false;
        receiveData();
    }
    private class NetWorkThread extends Thread{
        @Override
        public void run() {
            super.run();

        }
    }


    private void receiveFile() {
        isRunning=true;
        FileInfo fileInfo=null;
        try {
            bi.read(receiveSize);
            bo.write(receiveHello);
            bo.flush();
            receiveFileLength=byteArrayToInt(receiveSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getResources().getString(R.string.dataFilePath), context.getExternalFilesDir(null).getAbsolutePath() );
        if (filePath==null||filePath.equals(""))
        {
            filePath = context.getExternalFilesDir(null).getAbsolutePath();
        }
        Log.v(MainFormActivity.TAG,"PhoneServer- receiveFile -filePath:"+ filePath);

        int c=0;
        int tol=0;
        byte [] buffer=new byte[1024];
//        try {
//            c=bi.read(buffer);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            while (tol<receiveFileLength&&(c=bi.read(buffer,0,buffer.length))!=-1)
            {
                bas.write(buffer,0,c);
                tol+=c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String fileInfoJson=bas.toString("utf-8");
            Log.v(MainFormActivity.TAG, "PhoneServer receiceFile: fileInfo" + fileInfoJson);
//            String fileInfoJson=new String(buffer,0,c,"utf-8");
            fileInfo =  JSON.parseObject(fileInfoJson,FileInfo.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            bo.write(receiveHello);
            bo.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int cc=0;
        int total=0;
        byte[] buffers = new byte[1024 * 4];
        File file=new File(filePath,fileInfo.getFileName());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            while (total<fileInfo.getFileSize()&&(cc = bi.read(buffers, 0, buffers.length))!=-1) {
                fos.write(buffers,0,cc);
                total+=cc;
            }
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateMessage.updateMessage(filePath+"/"+fileInfo.getFileName(),MESSAGE_TYPE.FILE);
        isRunning=false;
        receiveData();

    }
    public void close() {
        try {
            if (socket!=null&&ss!=null)
            {
                bo.close();
                bi.close();
                socket.close();
                ss.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        updateMessage.updateMessage("PC 端已经关闭",MESSAGE_TYPE.MESSAGE);



    }
    /**
     * @param b
     * @return int
     */
    public static int byteArrayToInt(byte[] b){
        byte[] a = new byte[4];
        int i = a.length - 1,j = b.length - 1;
        for (; i >= 0 ; i--,j--) {//从b的尾部(即int值的低位)开始copy数据
            if(j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff);//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff)<< 8 ;
        int v2 = (a[2] & 0xff)<< 16 ;
        int v3 = (a[3] & 0xff)  << 24;
        return v0 + v1 + v2 + v3;
    }


    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

}
