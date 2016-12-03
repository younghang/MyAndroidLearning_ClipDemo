package com.example.yanghang.myapplication;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghang on 2016/12/3.
 */
public class MyApplication extends Application {

    private static List<String> mlist;

    public static List<String> getCatalogue() {

        mlist = new ArrayList<String>();
        mlist.add("default");
        mlist.add("dialog");
        mlist.add("carpet");
        mlist.add("coding");
        mlist.add("cat");
        mlist.add("making");
        mlist.add("dialog");
        mlist.add("d");
        mlist.add("f");
        mlist.add("df");
        mlist.add("a");
        return mlist;
    }

    public static void setCatalogue(List<String> list) {
        mlist = list;
    }
}
