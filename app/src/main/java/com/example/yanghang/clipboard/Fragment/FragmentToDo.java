package com.example.yanghang.clipboard.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.chiemy.cardview.MainActivity;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.OthersView.DateChooseWheelViewDialog;
import com.example.yanghang.clipboard.R;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;


public class FragmentToDo extends FragmentEditAbstract {



    public FragmentToDo() {
        // Required empty public constructor
    }
    private FragmentEditInfo fragmentEditInfo;
    private View mView;
    private String informationEdited;
    private String endDate;
    private boolean isFinished;
    private boolean isCurrentDay;
    private boolean isDailyWork;

    private Switch finishSwitch;
    private Switch currentDaySwitch;
    private TextView endDateTextView;


    public static FragmentToDo newInstance(String information, boolean isEdit) {
        FragmentToDo fragment = new FragmentToDo();
        newInstance(fragment, information, isEdit);
        return fragment;
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
        mView= inflater.inflate(R.layout.fragment_to_do, null);
        initialView();
        return mView;
    }

    private void initialView() {

        ToDoData toDoData =null;
        try {
            toDoData=JSON.parseObject(infoEdit, ToDoData.class);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (toDoData!=null)
        {
            isFinished=toDoData.isFinished();
            isCurrentDay=toDoData.isCurrentDay();
            informationEdited=toDoData.getContent();
            isDailyWork = toDoData.isDailyTask();
            endDate=toDoData.getEndTime();
        }
        else
        {
            isFinished=false;
            isCurrentDay=false;
            isDailyWork=false;
            informationEdited=infoEdit;
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = sDateFormat.format(new java.util.Date());
            endDate=date;
        }
        fragmentEditInfo = FragmentEditInfo.newInstance(informationEdited, isEdit);
        FragmentTransaction fragmentTransaction= getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_todo_editInfo, fragmentEditInfo);
        //下面这句会导致回退不彻底
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        finishSwitch = (Switch)mView.findViewById(R.id.fragment_todo_finished_switch);
        endDateTextView = (TextView) mView.findViewById(R.id.fragment_todo_choose_date_tv);
        currentDaySwitch = (Switch)mView.findViewById(R.id.fragment_todo_current_day_switch);

        finishSwitch.setChecked(isFinished);
        finishSwitch.setEnabled(isEdit);
        currentDaySwitch.setChecked(isCurrentDay);
        currentDaySwitch.setEnabled(isEdit);
        endDateTextView.setEnabled(isEdit);
        endDateTextView.setText(endDate);



        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateChooseWheelViewDialog endDateChooseDialog = new DateChooseWheelViewDialog(getActivity(),endDate,
                        new DateChooseWheelViewDialog.DateChooseInterface() {
                            @Override
                            public void getDateTime(String time, boolean longTimeChecked) {
                                endDateTextView.setText(time);
                            }
                        });

                endDateChooseDialog.setTimePickerGone(true);
                endDateChooseDialog.setDateDialogTitle("结束时间");
                endDateChooseDialog.showDateChooseDialog();
            }
        });

        if (isEdit&&MainFormActivity.isDailyTask)
        {
            isDailyWork=true;
            MainFormActivity.isDailyTask=false;
        }
        if (isDailyWork)
        {
            currentDaySwitch.setVisibility(View.GONE);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void redo() {
        fragmentEditInfo.redo();

    }

    @Override
    public void undo() {
        fragmentEditInfo.undo();

    }

    @Override
    public String getString() {
        isFinished=finishSwitch.isChecked();
        isCurrentDay=currentDaySwitch.isChecked();
        ToDoData toDoData = new ToDoData(fragmentEditInfo.getString(), endDateTextView.getText().toString(), isFinished, isCurrentDay, isDailyWork);
        return JSON.toJSONString(toDoData);
    }

    @Override
    public void enableEdit() {
        fragmentEditInfo.enableEdit();
        finishSwitch.setEnabled(true);
        currentDaySwitch.setEnabled(true);
        endDateTextView.setEnabled(true);

    }

}
