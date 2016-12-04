package com.example.yanghang.myapplication;

import android.app.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghang on 2016/12/3.
 */
public class MyApplication extends Application {


    private static String FILE_NAME = "catalogue.json";
    private static String CATALOGUE_NAME = "catalogue";

    public static List<String> loadCatalogue(String filePath) {


//        getgetApplicationContext().getFilesDir().getAbsolutePath();

        List<String> mList = new ArrayList<>();
        File file = new File(filePath + "/" + FILE_NAME);
        FileInputStream fileInputStream;

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {

                fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int c = 0;
                while ((c = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, c);
                }
                JSONObject json = new JSONObject(bos.toString("utf-8"));
                JSONArray jsonArray = json.getJSONArray(CATALOGUE_NAME);
                for (int i = 0; i < jsonArray.length(); i++) {
                    mList.add(jsonArray.getString(i));
                }
                bos.close();
                fileInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

    public static void setCatalogue(String filePath, List<String> mList) {
        File file = new File(filePath + "/" + FILE_NAME);
        FileOutputStream fileOutputStream;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {

                fileOutputStream = new FileOutputStream(file, false);

                JSONObject json = new JSONObject();

                JSONArray jsonArray = new JSONArray();

                for (int i = 0; i < mList.size(); i++) {
                    jsonArray.put(mList.get(i));
                }
                json.put(CATALOGUE_NAME, jsonArray);
                ByteArrayInputStream bis = new ByteArrayInputStream(json.toString().getBytes("utf-8"));
                byte[] buffer = new byte[1024];
                int c = 0;
                while ((c = bis.read(buffer, 0, buffer.length)) != -1) {
                    fileOutputStream.write(buffer, 0, c);
                }
                bis.close();
                fileOutputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
