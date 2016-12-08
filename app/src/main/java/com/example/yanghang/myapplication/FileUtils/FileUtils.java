package com.example.yanghang.myapplication.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.yanghang.myapplication.DBClipInfos.MyDBManager;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghang on 2016/12/6.
 */
public class FileUtils {   //        getgetApplicationContext().getFilesDir().getAbsolutePath();
    private static String CATALOGUE_FILE_NAME = "catalogue.json";
    private static String CATALOGUE_NAME = "catalogue";
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
                jsonObject.put(MyDBManager.KEY_CATALOGUE, listData.getCatalogue());
                jsonObject.put(MyDBManager.KEY_CONTENT, listData.getContent());
                jsonObject.put(MyDBManager.KEY_DATETIME, listData.getCreateDate());
                jsonObject.put(MyDBManager.KEY_REMARK, listData.getRemarks());
                jsonObject.put(MyDBManager.KEY_ORDERID, listData.getOrderID());
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
                    ListData listData = new ListData(jsonObject.getString(MyDBManager.KEY_REMARK), jsonObject.getString(MyDBManager.KEY_CONTENT), jsonObject.getString(MyDBManager.KEY_DATETIME), jsonObject.getInt(MyDBManager.KEY_ORDERID), jsonObject.getString(MyDBManager.KEY_CATALOGUE));
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

    public static List<String> loadCatalogue(String filePath) {


        List<String> mList = new ArrayList<>();
        File file = new File(filePath + "/" + CATALOGUE_FILE_NAME);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject json = loadJsonFromDisk(file);
                JSONArray jsonArray = json.getJSONArray(CATALOGUE_NAME);
                for (int i = 0; i < jsonArray.length(); i++) {
                    mList.add(jsonArray.getString(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mList;

    }

    public static void saveCatalogue(String filePath, List<String> mList) {
        File file = createFile(CATALOGUE_FILE_NAME, filePath);
        if (file == null) {
            Log.v(MainFormActivity.MTTAG, "saveCatalogue: file null");
            return;
        }
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < mList.size(); i++) {
            jsonArray.put(mList.get(i));
        }
        try {
            json.put(CATALOGUE_NAME, jsonArray);
            saveJsonObjectToDisk(file, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Gets the corresponding path to a file from the given content:// URI
     *
     * @param selectedVideoUri The content:// URI to find the file path from
     *                         .
     * @return the file path as a string
     */
    public static String getFilePathFromContentUri(Context context, Uri selectedVideoUri) {

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
        } else if (authority.equals("media")) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};

//        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
//      也可用下面的方法拿到cursor
            Cursor cursor = context.getContentResolver().query(selectedVideoUri, filePathColumn, null, null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
//            Log.v(MainFormActivity.MTTAG, columnIndex+"\n"+filePath);
            cursor.close();
            return filePath;
        }
        return null;
//        Log.v(MainFormActivity.MTTAG, "  uri=   "+selectedVideoUri.toString());
//        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getAuthority() );
//        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getPath() );
//        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getScheme() );
//        Log.v(MainFormActivity.MTTAG, selectedVideoUri.getPathSegments().toString());


    }
//    /**
//     * Get a file path from a Uri. This will get the the path for Storage Access
//     * Framework Documents, as well as the _data field for the MediaStore and
//     * other file-based ContentProviders.
//     *
//     * @param context The context.
//     * @param uri The Uri to query.
//     * @author paulburke
//     */
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
    // TODO handle non-primary volumes
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
//    /**
//     * Get the value of the data column for this Uri. This is useful for
//     * MediaStore Uris, and other file-based ContentProviders.
//     *
//     * @param context The context.
//     * @param uri The Uri to query.
//     * @param selection (Optional) Filter used in the query.
//     * @param selectionArgs (Optional) Selection arguments used in the query.
//     * @return The value of the _data column, which is typically a file path.
//     */
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

