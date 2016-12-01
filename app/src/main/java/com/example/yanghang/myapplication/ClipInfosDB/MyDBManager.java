package com.example.yanghang.myapplication.ClipInfosDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.yanghang.myapplication.ListPackage.ListData;
import com.example.yanghang.myapplication.MainFormActivity;

/**
 * Created by yanghang on 2016/11/27.
 */
public class MyDBManager {
    public static final String DB_TABLE = "clipinfos";
    public static final String KEY_REMARK = "remark";
    public static final String KEY_DATETIME = "datetime";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_ID = "_id";
    public static final String KEY_ORDERID = "orderid";

    // 执行open（）打开数据库时，保存返回的数据库对象
    private SQLiteDatabase mSQLiteDatabase = null;


    // 本地Context对象
    private Context mContext = null;
    // 由SQLiteOpenHelper继承过来
    private MySQLDB mDatabaseHelper = null;

    public MyDBManager(Context context) {

        mContext = context;
    }

    // 打开数据库，返回数据库对象
    public void open() throws SQLException {

        mDatabaseHelper = new MySQLDB(mContext);
        mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
    }


    // 关闭数据库
    public void close() {

        mDatabaseHelper.close();
    }

    public long insertData(String remark, String content, String datetime, int orderid) {

        ContentValues initialValues = new ContentValues();

        initialValues.put(KEY_REMARK, remark);
        initialValues.put(KEY_CONTENT, content);
        initialValues.put(KEY_DATETIME, datetime);
        initialValues.put(KEY_ORDERID, orderid);

        return mSQLiteDatabase.insert(DB_TABLE, "_id", initialValues);
    }

    public void cancleDelete(String remark, String content, String datetime, int orderID) {
        Cursor cursor = mSQLiteDatabase.query(DB_TABLE, null, KEY_ORDERID + " >= " + orderID, null, null, null, KEY_ORDERID + " desc");
        if (cursor.moveToFirst()) {

            int orderIdIndex = cursor.getColumnIndex(MyDBManager.KEY_ORDERID);
            int idIndex = cursor.getColumnIndex(MyDBManager.KEY_ID);
            while (!cursor.isAfterLast()) {

                int order = cursor.getInt(orderIdIndex);
                int id = cursor.getInt(idIndex);
                Log.v(MainFormActivity.MTTAG, "db delete :" + orderID + " 查询到大于该orderid 的 orderid=" + order + "  id=" + id);
                ContentValues args = new ContentValues();
                order = order + 1;
                args.put(KEY_ORDERID, order);
                mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + id, null);
                cursor.moveToNext();
            }
        }
        cursor.close();
        insertData(remark, content, datetime, orderID);

    }


    public boolean deleteDataByID(long rowId) {

        return mSQLiteDatabase.delete(DB_TABLE, "_id" + "=" + rowId, null) > 0;
    }

    public void deleteDataByOrderID(int orderID) {

        mSQLiteDatabase.delete(DB_TABLE, KEY_ORDERID + "=" + orderID, null);
        Cursor cursor = mSQLiteDatabase.query(DB_TABLE, null, KEY_ORDERID + " > " + orderID, null, null, null, KEY_ORDERID + " desc");
        if (cursor.moveToFirst()) {

            int orderIdIndex = cursor.getColumnIndex(MyDBManager.KEY_ORDERID);
            int idIndex = cursor.getColumnIndex(MyDBManager.KEY_ID);
            while (!cursor.isAfterLast()) {

                int order = cursor.getInt(orderIdIndex);
                int id = cursor.getInt(idIndex);
                Log.v(MainFormActivity.MTTAG, "db delete :" + orderID + " 查询到大于该orderid 的 orderid=" + order + "  id=" + id);
                ContentValues args = new ContentValues();
                order = order - 1;
                args.put(KEY_ORDERID, order);
                mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + id, null);
                cursor.moveToNext();
            }

        }
        cursor.close();
    }


    public Cursor fetchAllDataDescByOrderID() {

        return mSQLiteDatabase.query(DB_TABLE, null, null, null, null, null, KEY_ORDERID + " desc");
    }


    public Cursor fetchData(long rowId) throws SQLException {

        Cursor mCursor = mSQLiteDatabase.query(true, DB_TABLE, null, KEY_ID + "=" + rowId, null, null, null, null, null);

        if (mCursor != null) {

            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean updateDataOrder(int OldOrderID, int newOrderId) {
        Log.v(MainFormActivity.MTTAG, "update: oldid=" + OldOrderID + "   newid=" + newOrderId);
        ContentValues args = new ContentValues();
        args.put(KEY_ORDERID, newOrderId);
        return mSQLiteDatabase.update(DB_TABLE, args, KEY_ORDERID + "=" + OldOrderID, null) > 0;
    }
    public boolean updateDataOrder(int OldOrderID, ListData newData) {
        Log.v(MainFormActivity.MTTAG, "update: oldid=" + OldOrderID + "  newdata: orderid=" + newData.getOrderID() + "  message=" + newData.getInformation());
        ContentValues args = new ContentValues();
        args.put(KEY_DATETIME, newData.getCreateDate());
        args.put(KEY_CONTENT, newData.getInformation());
        args.put(KEY_REMARK, newData.getRemarks());
        return mSQLiteDatabase.update(DB_TABLE, args, KEY_ORDERID + "=" + OldOrderID, null) > 0;
    }

    public boolean updateData(int OrderID, String remark, String content, String datetime) {

        ContentValues args = new ContentValues();
        args.put(KEY_REMARK, remark);
        args.put(KEY_CONTENT, content);
        args.put(KEY_DATETIME, datetime);

        return mSQLiteDatabase.update(DB_TABLE, args, KEY_ORDERID + "=" + OrderID, null) > 0;
    }
}
