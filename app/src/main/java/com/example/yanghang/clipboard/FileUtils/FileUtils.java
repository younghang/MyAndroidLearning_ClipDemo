package com.example.yanghang.clipboard.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.EncodeFile.AESUtils;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueInfos;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.MainFormActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;

/**
 * Created by yanghang on 2016/12/6.
 */
public class FileUtils {   //        getgetApplicationContext().getFilesDir().getAbsolutePath();
    public static String CATALOGUE_FILE_NAME = "catalogue";
    private static String CATALOGUE_NEW_FILE_NAME="new_catalogue";
    private static String CATALOGUE_JSON_NAME = "catalogue";
    private static String CATALOGUE_NAME="catalogue";
    private static String CATALOGUE_DESCRIPTION="catalogue_description";
    private static String LISTDATA_CLIPS_NAME = "clips";
    public static String SAVE_FILE_CATALOGUE_JSON_SUFFIX = ".json";
    private static String SAVE_FILE_CATALOGUE_ENCODE_SUFFIX = ".sphykey";
    public static String SEED="";


    public static boolean saveListData(List<ListData> mData, String filePath, String fileName,boolean encoded) {
        if (encoded){
            fileName = fileName + SAVE_FILE_CATALOGUE_ENCODE_SUFFIX;
        }else
        fileName = fileName + SAVE_FILE_CATALOGUE_JSON_SUFFIX;
        File file = createFile(fileName, filePath);
        if (file == null)
            return false;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < mData.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            ListData listData = mData.get(i);
            try {
                jsonObject.put(DBListInfoManager.KEY_CATALOGUE, listData.getCatalogue());
                jsonObject.put(DBListInfoManager.KEY_CONTENT, listData.getContent());
                jsonObject.put(DBListInfoManager.KEY_DATETIME, listData.getCreateDate());
                jsonObject.put(DBListInfoManager.KEY_REMARK, listData.getRemarks());
                jsonObject.put(DBListInfoManager.KEY_ORDERID, listData.getOrderID());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(LISTDATA_CLIPS_NAME, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return saveJsonObjectToDisk(file, jsonObject,encoded);


    }

    public static List<ListData> loadListDatas(String fileName,boolean encoded) throws Exception{
        List<ListData> listDatas = new ArrayList<>();
        File file = new File(fileName);

        JSONObject json;
        if (file.exists()) {
                json= loadJsonFromDisk(file,encoded);
            try {
                JSONArray array = json.getJSONArray(LISTDATA_CLIPS_NAME);
                for (int i = array.length()-1; i >-1; i--) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    ListData listData = new ListData(jsonObject.getString(DBListInfoManager.KEY_REMARK), jsonObject.getString(DBListInfoManager.KEY_CONTENT), jsonObject.getString(DBListInfoManager.KEY_DATETIME), jsonObject.getInt(DBListInfoManager.KEY_ORDERID), jsonObject.getString(DBListInfoManager.KEY_CATALOGUE));
                    listDatas.add(listData);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                throw new Exception("文本类型或密码不正确");
            }
        }

        return listDatas;

    }

    public static boolean saveJsonObjectToDisk(File file, JSONObject jsonObject,boolean encoded) {
        FileOutputStream fileOutputStream;
        try {

            fileOutputStream = new FileOutputStream(file, false);
            ByteArrayInputStream bis;
            if (encoded) {
                bis= new ByteArrayInputStream(AESUtils.encrypt(SEED,jsonObject.toString()).getBytes("utf-8"));
            }else
            {
                bis= new ByteArrayInputStream(jsonObject.toString().getBytes("utf-8"));
            }
            byte[] buffer = new byte[1024];
            int c = 0;
            while ((c = bis.read(buffer, 0, buffer.length)) != -1) {
                fileOutputStream.write(buffer, 0, c);
            }
            bis.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static JSONObject loadJsonFromDisk(File file,boolean encoded) throws BadPaddingException {
        if (file == null)
            return null;
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int c = 0;
            while ((c = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, c);
            }
            bos.close();
            fileInputStream.close();
            JSONObject json;
            if (encoded)
            {
                json = new JSONObject(AESUtils.decrypt(FileUtils.SEED,bos.toString("utf-8")));
            }else
                json = new JSONObject(bos.toString("utf-8"));
            return json;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static File createFile(String fileName, String filePath) {
        if (!new File(filePath).exists()) {
            boolean dirCreate = new File(filePath).mkdirs();
            if (!dirCreate) {
                Log.v(MainFormActivity.TAG, "create file dirs failed");
                return null;
            }
        }
        File file = new File(filePath + "/" + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;


        } else return file;

    }
    public static List<CatalogueInfos> loadCatalogueFromDisk(String fileAbsoluteName)
    {
        List<CatalogueInfos> lists=null  ;
        File file = new File(fileAbsoluteName);
        lists = loadCatalogueFile(file);
        return lists;
    }

    public static List<CatalogueInfos> loadCatalogue(String filePath)  {


        List<CatalogueInfos> mList ;
        File file = new File(filePath + "/" + CATALOGUE_NEW_FILE_NAME+SAVE_FILE_CATALOGUE_JSON_SUFFIX);

        if (file.exists()) {
            mList = loadCatalogueFile(file);
            file.delete();
        }
        else {
            try {
                file= new File(filePath + "/" + CATALOGUE_FILE_NAME+SAVE_FILE_CATALOGUE_JSON_SUFFIX);
                if (!file.exists())
                {
                    file.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mList = loadCatalogueFile(file);
        }
        return mList;

    }
    private static List<CatalogueInfos> loadCatalogueFile(File file)
    {
        List<CatalogueInfos> mList = new ArrayList<>();
        JSONArray jsonArray=null;
        try {
            JSONObject json = loadJsonFromDisk(file,false);
            if (json==null)
            {
                return mList;
            }
            jsonArray = json.getJSONArray(CATALOGUE_JSON_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                CatalogueInfos catalogueInfos = new CatalogueInfos(jsonArray.getJSONObject(i).getString(CATALOGUE_NAME), jsonArray.getJSONObject(i).getString(CATALOGUE_DESCRIPTION));
                mList.add(catalogueInfos);
            }

        } catch (JSONException e) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    CatalogueInfos catalogueInfos = new CatalogueInfos(jsonArray.getString(i),"");
                    mList.add(catalogueInfos);
                }
            }
            catch (JSONException ea)
            {
                ea.printStackTrace();
            }
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return mList;
    }

    public static boolean saveCatalogue(String filePath, List<CatalogueInfos> mList, boolean newFile,String fileName)  {
        //此为判断是否保存到其他位置，另存为，并非加载储存在默认位置的目录
        if (fileName.equals(""))//默认位置
        {

            if (newFile)
            {//因为正常退出的时候会直接保存内存中即加载之前的目录（目录只是文件改变，内存中未改变）
                fileName=CATALOGUE_NEW_FILE_NAME;
            }
            else{
                fileName=CATALOGUE_FILE_NAME;
            }
        }
        fileName = fileName + SAVE_FILE_CATALOGUE_JSON_SUFFIX;
        File file = createFile(fileName, filePath);
        if (file == null) {
            Log.v(MainFormActivity.TAG, "saveCatalogue: file is null");
            return false;
        }
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < mList.size(); i++) {
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put(CATALOGUE_NAME,mList.get(i).getCatalogue());
                jsonObject.put(CATALOGUE_DESCRIPTION, mList.get(i).getCatalogueDescription());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            jsonArray.put(jsonObject);
        }
        try {
            json.put(CATALOGUE_JSON_NAME, jsonArray);
            return saveJsonObjectToDisk(file, json,false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getFilePathFromContentUri(Context context, Uri selectedVideoUri) {
        Log.v(MainFormActivity.TAG, "  uri=   " + selectedVideoUri.toString());
        Log.v(MainFormActivity.TAG, selectedVideoUri.getAuthority());
        Log.v(MainFormActivity.TAG, "   path=  " + selectedVideoUri.getPath());
        Log.v(MainFormActivity.TAG, selectedVideoUri.getScheme());
        Log.v(MainFormActivity.TAG, selectedVideoUri.getPathSegments().toString());

        String filePath;
        String authority = selectedVideoUri.getAuthority();
        if (authority.equals("com.android.externalstorage.documents")) {
            String Path = selectedVideoUri.getPath();
            String[] ls = Path.split(":");
            if (ls[0].contains("primary")) {
                filePath = Environment.getExternalStorageDirectory() + "/" + ls[1];
//                Log.v(MainFormActivity.TAG, filePath);
                return filePath;
            } else {
                String s = ls[0].split("/")[2];
                filePath = "/storage" + "/" + s + "/" + ls[1];
//                Log.v(MainFormActivity.TAG, filePath);
                return filePath;
            }
        } else if (authority.equals("com.android.providers.media.documents")) {
            final String docId = DocumentsContract.getDocumentId(selectedVideoUri);
            final String[] split = docId.split(":");
            final String type = split[0];
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };
            Log.v(MainFormActivity.TAG, "id=" + split[1]);
            final String column = "_data";
            final String[] projection = {
                    column
            };
            return getQuery(context, projection, contentUri, selection, selectionArgs);

        } else if (authority.equals("media")) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            return getQuery(context, filePathColumn, selectedVideoUri, null, null);
        }
        return null;


    }

    private static String getQuery(Context context, String[] filePathColumn, Uri selectedVideoUri, String selection, String[] selectionArgs) {
        Cursor cursor = context.getContentResolver().query(selectedVideoUri, filePathColumn, selection, selectionArgs, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        Log.v(MainFormActivity.TAG, "index " + columnIndex + "   filePath=  " + filePath);
        cursor.close();
        return filePath;
    }





    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath
     *            文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return FormetFileSize(blockSize);
    }
    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }
    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }
    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

}

