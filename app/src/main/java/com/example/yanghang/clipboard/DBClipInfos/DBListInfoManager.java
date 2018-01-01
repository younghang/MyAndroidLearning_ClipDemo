package com.example.yanghang.clipboard.DBClipInfos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.MainFormActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghang on 2016/11/27.
 */
public class DBListInfoManager {
    public static final String DB_TABLE = "clipinfos";
    public static final String KEY_REMARK = "remark";
    public static final String KEY_DATETIME = "datetime";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_ID = "_id";
    public static final String KEY_ORDERID = "orderid";
    public static final String KEY_CATALOGUE = "catalogue";


    //换太麻烦了，有空再考虑
    public static final String ACCOUNT_TABLE_NAME = "accounts";
    public static final String ACCOUNT_TIME = "accounttime";
    public static final String ACCOUNT_MONEY = "money";
    public static final String ACCOUNT_REMARK= "remark";
    public static final String ACCOUNT_CONTENT = "content";
    public static final String ACCOUNT_COLOR = "color";
    public static final String ACCOUNT_TYPE= "type";

    // 执行open（）打开数据库时，保存返回的数据库对象
    private SQLiteDatabase mSQLiteDatabase = null;


    // 本地Context对象
    private Context mContext = null;
    // 由SQLiteOpenHelper继承过来
    private ListInfoDB mDatabaseHelper = null;

    public DBListInfoManager(Context context) {

        mContext = context;
    }

    // 打开数据库，返回数据库对象
    private void open() throws SQLException {

        mDatabaseHelper = new ListInfoDB(mContext);
        mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
    }


    // 关闭数据库
    private void close() {

//        mDatabaseHelper.close();
        mSQLiteDatabase.close();
    }

    //获取orderId
    public int getDataCount() {
        open();
        int count = 0;
        Cursor cursor = mSQLiteDatabase.query(DB_TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                count++;
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
        return count;
    }
    public List<ListData> searchDataByDate(String date)
    {
        open();
        String queryStr = "select * from " + DB_TABLE + " where " + KEY_DATETIME + " like '" + date + "%'"  + " order by " + KEY_ORDERID + " desc";

        Cursor cursor=mSQLiteDatabase.rawQuery(queryStr, null);
        List<ListData> mDatas = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int remarkIndex = cursor.getColumnIndex(DBListInfoManager.KEY_REMARK);
            int contentIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CONTENT);
            int datetimeIndex = cursor.getColumnIndex(DBListInfoManager.KEY_DATETIME);
            int orderIdIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ORDERID);
            int catalogueIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CATALOGUE);
            while (!cursor.isAfterLast()) {
                String remark = cursor.getString(remarkIndex);
                String content = cursor.getString(contentIndex);
                String datetime = cursor.getString(datetimeIndex);
                String catalogue = cursor.getString(catalogueIndex);
                int orderID = cursor.getInt(orderIdIndex);
                ListData listData = new ListData(remark, content, datetime, orderID, catalogue);
                mDatas.add(listData);
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
        return mDatas;
    }

    public List<ListData> searchData(String query) {
        open();
        Cursor cursor = searchDataInContent(query);
        List<ListData> mDatas = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int remarkIndex = cursor.getColumnIndex(DBListInfoManager.KEY_REMARK);
            int contentIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CONTENT);
            int datetimeIndex = cursor.getColumnIndex(DBListInfoManager.KEY_DATETIME);
            int orderIdIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ORDERID);
            int catalogueIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CATALOGUE);
            while (!cursor.isAfterLast()) {
                String remark = cursor.getString(remarkIndex);
                String content = cursor.getString(contentIndex);
                String datetime = cursor.getString(datetimeIndex);
                String catalogue = cursor.getString(catalogueIndex);
                int orderID = cursor.getInt(orderIdIndex);
                ListData listData = new ListData(remark, content, datetime, orderID, catalogue);
                mDatas.add(listData);
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
        return mDatas;
    }

    //no use
    public int getCatalogueCount(String catalogue) {
        int count = 0;
        Cursor cursor = mSQLiteDatabase.query(DB_TABLE, null, KEY_CATALOGUE + "=" + catalogue, null, null, null, KEY_ORDERID);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                count++;
                cursor.moveToNext();
            }
        }
        cursor.close();
        return count;
    }

    public void insertDatas(List<ListData> listDatas) {
        for (int i = 0; i < listDatas.size(); i++) {
            ListData listData = listDatas.get(i);
            insertData(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), getDataCount(), listData.getCatalogue());
        }
    }

    public long insertData(ListData listData) {
        return insertData(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());

    }


    public long insertData(String remark, String content, String datetime, int orderid, String catalogue) {
        open();
        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_REMARK, remark);
        initialValues.put(KEY_CONTENT, content);
        initialValues.put(KEY_DATETIME, datetime);
        initialValues.put(KEY_ORDERID, orderid);
        initialValues.put(KEY_CATALOGUE, catalogue);

        long count = mSQLiteDatabase.insert(DB_TABLE, "_id", initialValues);
        close();
        return count;
    }

    public void cancelDelete(String remark, String content, String datetime, int orderID, String catalogue) {
        open();
        //注意一下顺序，“加” 返回倒序，先加大的  “减”返回顺序，先减小的， 避免重复
        Cursor cursor = mSQLiteDatabase.query(DB_TABLE, null, KEY_ORDERID + " >= " + orderID, null, null, null, KEY_ORDERID + " desc");
        if (cursor.moveToFirst()) {

            int orderIdIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ORDERID);
            int idIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ID);
            while (!cursor.isAfterLast()) {

                int order = cursor.getInt(orderIdIndex);
                int id = cursor.getInt(idIndex);
                Log.v(MainFormActivity.TAG, "db cancle delete :" + orderID + " 查询到大于该orderid 的 orderid=" + order + "  id=" + id);
                ContentValues args = new ContentValues();
                order = order + 1;
                args.put(KEY_ORDERID, order);
                mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + id, null);
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
        insertData(remark, content, datetime, orderID, catalogue);


    }


    public void deleteDataByOrderID(int orderID) {
        open();
        int result = mSQLiteDatabase.delete(DB_TABLE, KEY_ORDERID + "=" + orderID, null);
        if (result == 0) {
            Log.v(MainFormActivity.TAG, "找不到orderid=" + orderID + "没有删除任何东西");
            return;
        }
        Log.v(MainFormActivity.TAG, "删除orderid:" + orderID);
        Cursor cursor = mSQLiteDatabase.query(DB_TABLE, null, KEY_ORDERID + ">=" + orderID, null, null, null, KEY_ORDERID);
        if (cursor.moveToFirst()) {

            int orderIdIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ORDERID);
            int idIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ID);
            while (!cursor.isAfterLast()) {
                int order = cursor.getInt(orderIdIndex);
                int id = cursor.getInt(idIndex);
                Log.v(MainFormActivity.TAG, "db delete :" + orderID + " 查询到大于该orderid 的 orderid=" + order + "  id=" + id);
                ContentValues args = new ContentValues();
                order = order - 1;
                args.put(KEY_ORDERID, order);
                mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + id, null);
                cursor.moveToNext();
            }

        }
        cursor.close();
        close();
    }


    private Cursor fetchAllDataDescByOrderID() {

        return mSQLiteDatabase.query(DB_TABLE, null, null, null, null, null, KEY_ORDERID + " desc");
    }

    //use make sure open before
    public Cursor fetchAllDataByCatalogue(String catalogue) {
        if (catalogue.equals(""))
            return fetchAllDataDescByOrderID();
        return mSQLiteDatabase.query(DB_TABLE, null, KEY_CATALOGUE + "='" + catalogue + "'", null, null, null, KEY_ORDERID + " desc");
    }

    private Cursor searchDataInContent(String content) {
        String queryStr = "select * from " + DB_TABLE + " where " + KEY_CONTENT + " like '%" + content + "%'" + " or " + KEY_REMARK + " like '%" + content + "%'" + " order by " + KEY_ORDERID + " desc";

        return mSQLiteDatabase.rawQuery(queryStr, null);
    }
    public List<ListData> getDatas(List<String> catalogues)
    {
        List<ListData> mDatas = new ArrayList<ListData>();
        open();
        Cursor cursor = fetchAllDataDescByOrderID();
        if (cursor == null) {
//            mDatas.add(new ListData("ceshi", "magnet:?xt=urn:btih:4fc4a218aca38d73147585ff51773fc834e08810", 0, currentCatalogue));

        } else {
            if (cursor.moveToFirst()) {
                int remarkIndex = cursor.getColumnIndex(DBListInfoManager.KEY_REMARK);
                int contentIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CONTENT);
                int datetimeIndex = cursor.getColumnIndex(DBListInfoManager.KEY_DATETIME);
                int orderIdIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ORDERID);
                int catalogueIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CATALOGUE);
                while (!cursor.isAfterLast()) {
                    String remark = cursor.getString(remarkIndex);
                    String content = cursor.getString(contentIndex);
                    String datetime = cursor.getString(datetimeIndex);
                    int orderID = cursor.getInt(orderIdIndex);
                    String catalogue = cursor.getString(catalogueIndex);
                    if (catalogues.contains(catalogue)) {
                        ListData listData = new ListData(remark, content, datetime, orderID, catalogue);
                        mDatas.add(listData);
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        close();
        return mDatas;
    }


    public List<ListData> getDatas(String currentCatalogue) {
        List<ListData> mDatas = new ArrayList<ListData>();
        open();
        Cursor cursor = fetchAllDataByCatalogue(currentCatalogue);
        if (cursor == null) {
            mDatas.add(new ListData("ceshi", "magnet:?xt=urn:btih:4fc4a218aca38d73147585ff51773fc834e08810", 0, currentCatalogue));

        } else {
            if (cursor.moveToFirst()) {
                int remarkIndex = cursor.getColumnIndex(DBListInfoManager.KEY_REMARK);
                int contentIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CONTENT);
                int datetimeIndex = cursor.getColumnIndex(DBListInfoManager.KEY_DATETIME);
                int orderIdIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ORDERID);
                int catalogueIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CATALOGUE);
                while (!cursor.isAfterLast()) {
                    String remark = cursor.getString(remarkIndex);
                    String content = cursor.getString(contentIndex);
                    String datetime = cursor.getString(datetimeIndex);
                    int orderID = cursor.getInt(orderIdIndex);
                    String catalogue = cursor.getString(catalogueIndex);
//                    Log.v(TAG, "GetData : content=" + content + "  catalogue=" + catalogue);
                    ListData listData = new ListData(remark, content, datetime, orderID, catalogue);
                    mDatas.add(listData);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        close();
        return mDatas;
    }


    public boolean updateDataOrder(int OldOrderID, int newOrderId) {
        open();
        Log.v(MainFormActivity.TAG, "update: oldid=" + OldOrderID + "   newid=" + newOrderId);
        ContentValues args = new ContentValues();
        args.put(KEY_ORDERID, newOrderId);
        int count = mSQLiteDatabase.update(DB_TABLE, args, KEY_ORDERID + "=" + OldOrderID, null);
        close();
        return count > 0;
    }
// i don't know it , I've forgot this
    private boolean updateDataOrder(int OldOrderID, ListData newData) {
        Log.v(MainFormActivity.TAG, "update: oldid=" + OldOrderID + "  newdata: orderid=" + newData.getOrderID() + "  catalogue=" + newData.getCatalogue());
        ContentValues args = new ContentValues();
        args.put(KEY_DATETIME, newData.getCreateDate());
        args.put(KEY_CONTENT, newData.getContent());
        args.put(KEY_REMARK, newData.getRemarks());
        args.put(KEY_CATALOGUE, newData.getCatalogue());
        return mSQLiteDatabase.update(DB_TABLE, args, KEY_ORDERID + "=" + OldOrderID, null) > 0;
    }

    public boolean updateDataByOrderId(int OrderID, String catalogue, String remark, String content, String datetime) {
        open();
        ContentValues args = new ContentValues();
        args.put(KEY_REMARK, remark);
        args.put(KEY_CONTENT, content);
        args.put(KEY_DATETIME, datetime);
        args.put(KEY_CATALOGUE, catalogue);

        int count = mSQLiteDatabase.update(DB_TABLE, args, KEY_ORDERID + "=" + OrderID, null);
        close();
        return count > 0;
    }
    public void deleteDatabase()
    {
        open();
        mSQLiteDatabase.deleteDatabase(mContext.getDatabasePath(ListInfoDB.DB_NAME));
        close();
    }

    public void changeCatalogue(String oldCatalogue, String newCatalogue) {
        open();
        Cursor cursor = fetchAllDataByCatalogue(oldCatalogue);
//        Log.v(MainFormActivity.TAG, "DB: oldCatalogue=" + oldCatalogue);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBListInfoManager.KEY_ID);
            int contentIndex = cursor.getColumnIndex(DBListInfoManager.KEY_CONTENT);
            while (!cursor.isAfterLast()) {
                int id = cursor.getInt(idIndex);
                String content = cursor.getColumnName(contentIndex);
                ContentValues args = new ContentValues();
                args.put(KEY_CATALOGUE, newCatalogue);
                mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + id, null);
//                Log.v(MainFormActivity.TAG, "DB: oldCatalogue=" + oldCatalogue + "   newCatalogue=" + newCatalogue+"  content="+content);
                cursor.moveToNext();
            }
        }
        cursor.close();
        close();
    }
}
