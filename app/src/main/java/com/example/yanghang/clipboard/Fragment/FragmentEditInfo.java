package com.example.yanghang.clipboard.Fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.OthersView.PerformEdit;
import com.example.yanghang.clipboard.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentEditInfo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentEditInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentEditInfo extends FragmentEditAbstract {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_INFO = "information";
    private static final String ARG_EDIT = "isedit";

    // TODO: Rename and change types of parameters
    private String infoEdit;
    EditText editInfo;
    PerformEdit mPerformEdit;
    private boolean isEdit = false;
    private OnFragmentInteractionListener mListener;

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
        Bundle args = new Bundle();
        args.putString(ARG_INFO, information);
        args.putBoolean(ARG_EDIT, isEdit);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentEditInfo() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            infoEdit = getArguments().getString(ARG_INFO);
            isEdit = getArguments().getBoolean(ARG_EDIT);
        }
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
        editInfo.requestFocus();


//        editInfo.setFocusableInTouchMode(isEdit);
//        editInfo.setKeyListener(null);
//        editInfo.setFocusable(isEdit);

        if (!isEdit)
        {
            keyListener=editInfo.getKeyListener();
            inputType=editInfo.getInputType();

            editInfo.setKeyListener(null);
        }
        editInfo.setText(infoEdit);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
        Log.v(MainFormActivity.MTTAG, "getString called in FragmentEditInfo :"+info);
        return info;
    }
    public void enableEdit() {
        Log.v(MainFormActivity.MTTAG, "enableEdit called in FragmentEditInfo");

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.v(MainFormActivity.MTTAG, "EditInfo Activity EditText click");
                editInfo.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        mListener = null;
    }
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null)
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
