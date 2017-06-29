package com.example.yanghang.clipboard.Fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.OthersView.PerformEdit;
import com.example.yanghang.clipboard.R;


public class FragmentEditInfo extends FragmentEditAbstract {



    EditText editInfo;
    PerformEdit mPerformEdit;

    //very important
    private View mView;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param information Parameter 1.
     * @param isEdit Parameter 2.
     * @return A new instance of fragment FragmentEditInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentEditInfo newInstance(String information, boolean isEdit) {
        FragmentEditInfo fragment = new FragmentEditInfo();
        newInstance(fragment, information, isEdit);
        return fragment;
    }

    public FragmentEditInfo() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onICreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView=inflater.inflate(R.layout.fragment_edit_info,null);
        initView();
        return mView;
    }
    private KeyListener keyListener=null;
    private int inputType=0;
    private void initView() {
        editInfo = (EditText)  mView.findViewById(R.id.tv_ShowInfo);
        editInfo.setText(infoEdit);
        mPerformEdit = new PerformEdit(editInfo) {
            @Override
            protected void onTextChanged(Editable s) {
                //文本发生改变,可以是用户输入或者是EditText.setText触发.(setDefaultText的时候不会回调)
                super.onTextChanged(s);
            }
        };

        if (!isEdit)//修改的时候
        {
            keyListener=editInfo.getKeyListener();
            inputType=editInfo.getInputType();

            editInfo.setKeyListener(null);
        }
        else {//新建的时候
            editInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editInfo.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editInfo, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            });
            editInfo.requestFocus();

        }
        editInfo.setText(infoEdit);
    }


    public void undo()
    {
        mPerformEdit.undo();
    }
    public void redo()
    {
        mPerformEdit.redo();
    }
    public String getString()
    {
        String info=editInfo.getText().toString();
//        Log.v(MainFormActivity.TAG, "getString called in FragmentEditInfo :"+info);
        return info;
    }
    public void enableEdit() {
//        Log.v(MainFormActivity.TAG, "enableEdit called in FragmentEditInfo");

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.v(MainFormActivity.TAG, "EditInfo Activity EditText click");
                editInfo.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //用HIDE_IMPLICIT_ONLY当Activity退出的时候，会自动收起键盘，SHOW_FORCED 不会
                imm.showSoftInput(editInfo, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        editInfo.requestFocus();
//   editInfo.setEnabled();进去看他是怎么做的，对键盘
        editInfo.setKeyListener(keyListener);
        editInfo.setInputType(inputType);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null)
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
    }



}
