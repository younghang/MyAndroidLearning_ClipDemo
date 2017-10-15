package com.example.yanghang.clipboard;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yanghang.clipboard.EncodeFile.AES;
import com.example.yanghang.clipboard.EncodeFile.AESKeyModel;
import com.example.yanghang.clipboard.EncodeFile.AESUtils;
import com.example.yanghang.clipboard.Notification.ActivityNotification;
import com.example.yanghang.clipboard.Notification.ServiceNotification;
import com.example.yanghang.clipboard.Test.HorizontalScrollListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.Key;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "mytag";
    EditText origin;
    EditText encode;
    EditText decode;
    Button test;
    String originString = "123456abc";
    File originFile;
    File encodeFile;
    File decodeFile;


    ImageButton fullScreenButton;
    Button masterItemButton;
    Button scrollingButton;
    Button buttonNavigationButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        origin = (EditText) findViewById(R.id.test_edit_origin);
        encode = (EditText) findViewById(R.id.test_edit_encode);
        decode = (EditText) findViewById(R.id.test_edit_decode);
        test = (Button) findViewById(R.id.test_button);
        origin.setText(originString);
        initialFile();

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            doAESUtils();
                doneWork();


            }
        });
        scrollingButton = (Button) findViewById(R.id.test_button_scrolling);
        fullScreenButton = (ImageButton) findViewById(R.id.test_button_fullScreen);
        masterItemButton = (Button) findViewById(R.id.test_button_masterItem);
        buttonNavigationButton = (Button) findViewById(R.id.test_button_buttonNavigation);

        scrollingButton.setOnClickListener(this);
        fullScreenButton.setOnClickListener(this);
        masterItemButton.setOnClickListener(this);
        buttonNavigationButton.setOnClickListener(this);

        setImage(fullScreenButton);
    }

    private void doAESUtils() {
        String enryptString = AESUtils.encrypt("5568646", originString);
        encode.setText(enryptString);
        decode.setText(AESUtils.decrypt("5568646",enryptString));
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBackground(View view ,boolean watched)
    {
        if (watched)
            view.setBackground(getDrawable(R.drawable.button_background_green));
        else
            view.setBackground(getDrawable(R.drawable.button_background_gray));
    }
    private void setImage(ImageButton imageButton)
    {
        VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(),R.drawable.ic_love,getTheme());
        //你需要改变的颜色
//        vectorDrawableCompat.setTint(getResources().getColor(R.color.light_gray));
        imageButton.setImageDrawable(vectorDrawableCompat);
    }
    void doneWork() {
        aesEncode(originFile.getAbsolutePath(), encodeFile.getAbsolutePath());
        try {
            byte[] bytes = new byte[1024];
            FileInputStream fis = new FileInputStream(encodeFile);
            fis.read(bytes);
            fis.close();
            encode.setText(new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        aesDecode(encodeFile.getAbsolutePath(), decodeFile.getAbsolutePath());
        try {
            byte[] bytes = new byte[1024];
            FileInputStream fis = new FileInputStream(decodeFile);
            fis.read(bytes);
            fis.close();
            decode.setText(new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialFile() {
        originFile = new File(getCacheDir().getAbsolutePath() + "/abc.txt");
        encodeFile = new File(getCacheDir().getAbsolutePath() + "/abc.sphykey");
        decodeFile = new File(getCacheDir().getAbsolutePath() + "/abcd.txt");
        try {
            originFile.createNewFile();
            encodeFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(originFile);
            fos.write(originString.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Key key;

    /**
     * 加密
     *
     * @param path
     * @param destionFile
     */
    private void aesEncode(String path, String destionFile) {
        try {
            Log.d(TAG, "aes Key: " + AES.INSTANCE.generateKey("5568646"));
            key = AES.INSTANCE.getKey();
            FileInputStream fis = new FileInputStream(new File(path));
            FileOutputStream fos = new FileOutputStream(new File(destionFile));
            AES.INSTANCE.encrypt(fis, fos);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }

    }

    private void aesDecode(String path, String destionFile) {
        try {

            Log.d(TAG, "aes Key: " + AES.INSTANCE.generateKey("5568646"));
            key = AES.INSTANCE.getKey();
            FileInputStream fis = new FileInputStream(new File(path));
            FileOutputStream fos = new FileOutputStream(new File(destionFile));
            AES.INSTANCE.decrypt(fis, fos);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }

    }

    /**
     * AES解密文件
     *
     * @param path 需要解密的文件目录
     */
    private void aesJieMi(String path) {
        File f = new File(path);
        if (!f.exists() || f.isDirectory())
            Toast.makeText(getApplicationContext(), "该文件不合法!", Toast.LENGTH_SHORT).show();
        else {

            String suffix = f.getName().split("\\.")[1];
            Log.d(TAG, "aesJieMi: " + suffix);
            if (!suffix.equals("sphykey")) {
                return;
            }
            AESKeyModel model_aes = new AESKeyModel();
            model_aes.setSrcFile(path);
            String decodeFile = getCacheDir().getAbsolutePath() + "/abcd.txt";
            model_aes.setDestionFile(decodeFile);

            try {
//                model_aes.descryptionFile(key_AES);
                model_aes.descryptionFile(key);
                // TODO: 加密后的文件
                RandomAccessFile raf = new RandomAccessFile(path, "rw");

                decode.setText(raf.readLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.test_button_scrolling:
                intent = new Intent(TestActivity.this, HorizontalScrollListView.class);
                startActivity(intent);

                break;
            case R.id.test_button_buttonNavigation:
                Intent serviceIntent = new Intent(TestActivity.this, ServiceNotification.class);
                startService(serviceIntent);

                break;
            case R.id.test_button_fullScreen:
                Intent fullScreenIntent = new Intent(TestActivity.this, ActivityNotification.class);
                startActivity(fullScreenIntent);

                break;
            case R.id.test_button_masterItem:

                break;

        }
    }
}
