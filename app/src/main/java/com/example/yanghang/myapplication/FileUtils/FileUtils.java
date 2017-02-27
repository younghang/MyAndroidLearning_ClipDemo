package com.example.yanghang.myapplication.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.example.yanghang.myapplication.DBClipInfos.DBListInfoManager;
import com.example.yanghang.myapplication.ListPackage.CatalogueList.CatalogueInfos;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.myapplication.MainFormActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghang on 2016/12/6.
 */
public class FileUtils {   //        getgetApplicationContext().getFilesDir().getAbsolutePath();
    private static String CATALOGUE_FILE_NAME = "catalogue.json";
    private static String CATALOGUE_NEW_FILE_NAME="new_catalogue.json";
    private static String CATALOGUE_JSON_NAME = "catalogue";
    private static String CATALOGUE_NAME="catalogue";
    private static String CATALOGUE_DESCRIPTION="catalogue_description";
    private static String LISTDATA_CLIPS_NAME = "clips";
    private static String SAVE_FILE_CATALOGUE_JSON_SUFFIX = ".json";
    private static String SAVE_FILE_CATALOGUE_BAK_SUFFIX = ".bak";

    public static boolean saveListDatas(List<ListData> mDatas, String filePath, String fileName) {
        fileName = fileName + SAVE_FILE_CATALOGUE_JSON_SUFFIX;
        File file = createFile(fileName, filePath);
        if (file == null)
            return false;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < mDatas.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            ListData listData = mDatas.get(i);
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
        return saveJsonObjectToDisk(file, jsonObject);


    }

    public static List<ListData> loadListDatas(String fileName) {
        List<ListData> listDatas = new ArrayList<>();
        File file = new File(fileName);
        if (file.exists()) {
            JSONObject json = loadJsonFromDisk(file);
            try {
                JSONArray array = json.getJSONArray(LISTDATA_CLIPS_NAME);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    ListData listData = new ListData(jsonObject.getString(DBListInfoManager.KEY_REMARK), jsonObject.getString(DBListInfoManager.KEY_CONTENT), jsonObject.getString(DBListInfoManager.KEY_DATETIME), jsonObject.getInt(DBListInfoManager.KEY_ORDERID), jsonObject.getString(DBListInfoManager.KEY_CATALOGUE));
                    listDatas.add(listData);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return listDatas;

    }

    private static boolean saveJsonObjectToDisk(File file, JSONObject jsonObject) {
        FileOutputStream fileOutputStream;
        try {

            fileOutputStream = new FileOutputStream(file, false);
            ByteArrayInputStream bis = new ByteArrayInputStream(jsonObject.toString().getBytes("utf-8"));
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

    private static JSONObject loadJsonFromDisk(File file) {
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
            JSONObject json = new JSONObject(bos.toString("utf-8"));
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File createFile(String fileName, String filePath) {
        if (!new File(filePath).exists()) {
            boolean dirCreate = new File(filePath).mkdirs();
            if (!dirCreate) {
                Log.v(MainFormActivity.MTTAG, "create file dirs failed");
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
        List<CatalogueInfos> lists  ;
        File file = new File(fileAbsoluteName);
        lists = loadCatalogueFile(file);

        return lists;
    }

    public static List<CatalogueInfos> loadCatalogue(String filePath) {


        List<CatalogueInfos> mList ;
        File file = new File(filePath + "/" + CATALOGUE_NEW_FILE_NAME);

        if (file.exists()) {
            mList = loadCatalogueFile(file);
            file.delete();
        }
        else {
            try {
                file= new File(filePath + "/" + CATALOGUE_FILE_NAME);
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
            JSONObject json = loadJsonFromDisk(file);
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
        }
        return mList;
    }

    public static boolean saveCatalogue(String filePath, List<CatalogueInfos> mList, boolean newFile,String fileName)  {
        //此为保存到其他位置，另存为，并非加载储存在默认位置的目录
        if (fileName.equals(""))
        {

            if (newFile)
            {//因为退出的时候会直接保存为加载之前的目录，覆盖掉加载后的目录文件
                fileName=CATALOGUE_NEW_FILE_NAME;
            }
            else{
                fileName=CATALOGUE_FILE_NAME;
            }
        }
        File file = createFile(fileName, filePath);
        if (file == null) {
            Log.v(MainFormActivity.MTTAG, "saveCatalogue: file is null");
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
            return saveJsonObjectToDisk(file, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getFilePathFromContentUri(Context context, Uri selectedVideoUri) {
        Log.v(MainFormActivity.MTTAG, "  uri=   " + selectedVideoUri.toString());
        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getAuthority());
        Log.v(MainFormActivity.MTTAG, "   path=  " + selectedVideoUri.getPath());
        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getScheme());
        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getPathSegments().toString());

        String filePath;
        String authority = selectedVideoUri.getAuthority();
        if (authority.equals("com.android.externalstorage.documents")) {
            String Path = selectedVideoUri.getPath();
            String[] ls = Path.split(":");
            if (ls[0].contains("primary")) {
                filePath = Environment.getExternalStorageDirectory() + "/" + ls[1];
//                Log.v(MainFormActivity.MTTAG, filePath);
                return filePath;
            } else {
                String s = ls[0].split("/")[2];
                filePath = "/storage" + "/" + s + "/" + ls[1];
//                Log.v(MainFormActivity.MTTAG, filePath);
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
            Log.v(MainFormActivity.MTTAG, "id=" + split[1]);
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
        Log.v(MainFormActivity.MTTAG, "index " + columnIndex + "   filePath=  " + filePath);
        cursor.close();
        return filePath;
    }

//    public static String getPhotoPathFromContentUri(final Context context, final Uri uri) {
//
//        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
//
//        // DocumentProvider
//        if (isKitKat &&( DocumentsContract.isDocumentUri(context, uri))) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }
//

//            }
//            // DownloadsProvider
//            else if (isDownloadsDocument(uri)) {
//
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//
//                return getDataColumn(context, contentUri, null, null);
//            }
//            // MediaProvider
//            else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[] {
//                        split[1]
//                };
//
//                return getDataColumn(context, contentUri, selection, selectionArgs);
//            }
//        }
//        // MediaStore (and general)
//        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            return getDataColumn(context, uri, null, null);
//        }
//        // File
//        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//
//        return null;
//    }
//

//    public static String getDataColumn(Context context, Uri uri, String selection,
//                                       String[] selectionArgs) {
//
//        Cursor cursor = null;
//        final String column = "_data";
//        final String[] projection = {
//                column
//        };
//
//        try {
//            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
//                    null);
//            if (cursor != null && cursor.moveToFirst()) {
//                final int column_index = cursor.getColumnIndexOrThrow(column);
//                return cursor.getString(column_index);
//            }
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//        return null;
//    }
//
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    public static boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    public static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    public static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }


}

