package com.example.yanghang.clipboard.Fragment;


import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yanghang.clipboard.ActivityCalendar;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemsData;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueInfos;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendarItem extends Fragment {


    public FragmentCalendarItem() {
        // Required empty public constructor
    }
    ActivityCalendar activityCalendar;


    private View mView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCalendar= (ActivityCalendar) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_calendar_item, container, false);
        initialView();
        return mView;
    }
    private Chronometer timer;
    private Button btnStart;
    private Button btnEnd;
    private CardView cardView;
    private RecyclerView recyclerView;
    boolean reset=false;
    private void initialView()
    {
        timer = mView.findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime());
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("0"+String.valueOf(hour)+":%s");
        btnEnd = mView.findViewById(R.id.end_timer);
        btnStart = mView.findViewById(R.id.start_timer);
        cardView = mView.findViewById(R.id.timer_CardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(timer.getText());
                Toast.makeText(getActivity(), timer.getContentDescription()+"复制到粘贴板", Toast.LENGTH_SHORT).show();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.start();
            }
        });
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reset)
                {
                    timer.setBase(SystemClock.elapsedRealtime());
                    btnEnd.setText("结束");
                }
                else {
                    timer.stop();
                    btnEnd.setText("重置");
                }
                reset=!reset;
            }
        });
        recyclerView = mView.findViewById(R.id.fragment_calendar_item_recycleView);
        List<CalendarItemsData> lists = activityCalendar.calendarImageManager.getLists();
        calendarItemAdapter = new CalendarItemAdapter(lists, getActivity(), new CalendarItemAdapter.OnCalendarItemVisibilityChanged() {
            @Override
            public void onChanged() {
                activityCalendar.calendarImageManager.setLists(calendarItemAdapter.getLists());
                activityCalendar.calendarImageManager.saveImageLists();
            }
        });
        calendarItemAdapter.setOnAddClickListener(new CalendarItemAdapter.OnAddClick() {
            @Override
            public void onAddClick() {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_calendar_item, null);
                final EditText edCalendarItemName = (EditText) view.findViewById(R.id.dialogue_add_calendar_item_name);

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("添加日历项")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = edCalendarItemName.getText().toString();
                                calendarItemAdapter.addItem(new CalendarItemsData(name,""));
                                activityCalendar.calendarImageManager.setLists(calendarItemAdapter.getLists());
                                activityCalendar.calendarImageManager.saveImageLists();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                alertDialog.show();
            }
        });
        calendarItemAdapter.setOnItemClickListener(new CalendarAddItemsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, final int position) {
                CalendarItemsData calendarItemsData=calendarItemAdapter.getLists().get(position);
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_calendar_item, null);
                final EditText edCalendarItemName = (EditText) view.findViewById(R.id.dialogue_add_calendar_item_name);
                edCalendarItemName.setText(calendarItemsData.getCalendarItemName());
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("修改日历项")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = edCalendarItemName.getText().toString();
                                calendarItemAdapter.editItem(new CalendarItemsData(name,""),position);
                                activityCalendar.calendarImageManager.setLists(calendarItemAdapter.getLists());
                                activityCalendar.calendarImageManager.saveImageLists();
                            }
                        })
                        .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                calendarItemAdapter.deleteItem(position);
                                activityCalendar.calendarImageManager.setLists(calendarItemAdapter.getLists());
                                activityCalendar.calendarImageManager.saveImageLists();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                alertDialog.show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(calendarItemAdapter);
    }
    CalendarItemAdapter calendarItemAdapter;


}
