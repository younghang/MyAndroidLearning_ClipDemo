package com.example.yanghang.clipboard.Fragment;


import android.os.Bundle;

/**
 * Created by yanghang on 2017/1/18.
 */
public abstract class FragmentEditAbstract extends android.support.v4.app.Fragment implements IEditText {

    protected static final String ARG_INFO = "information";
    protected static final String ARG_EDIT = "isedit";
    protected  String infoEdit;
    protected boolean isEdit = false;
    protected static void newInstance(FragmentEditAbstract fragmentEditAbstract,String information, boolean isEdit)
    {
        Bundle args = new Bundle();
        args.putString(ARG_INFO, information);
        args.putBoolean(ARG_EDIT, isEdit);
        fragmentEditAbstract.setArguments(args);
    }

    protected  void onICreate() {
        if (getArguments() != null) {
            infoEdit = getArguments().getString(ARG_INFO);
            isEdit = getArguments().getBoolean(ARG_EDIT);
        }
    }

}
