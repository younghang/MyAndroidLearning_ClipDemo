package com.example.yanghang.myapplication.ClipInfosDB;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yanghang on 2016/11/27.
 */
public class MySQLDB extends SQLiteOpenHelper {

    private static final int VERSION = 2;
    private static final String DB_NAME = "clipdata.db";

    public MySQLDB(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public MySQLDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MySQLDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table clipinfos(_id  INTEGER PRIMARY KEY AUTOINCREMENT,remark text,content text,datetime text,orderid INTEGER unique,catalogue text default 'default')");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("alter table clipinfos rename to _temp_clipinfos");
        db.execSQL("create table clipinfos(_id  INTEGER PRIMARY KEY AUTOINCREMENT,remark text,content text,datetime text,orderid INTEGER unique,catalogue text default 'default')");
        db.execSQL("insert into clipinfos select * from _temp_clipinfos");
        db.execSQL("drop table _temp_clipinfos");

    }
}
