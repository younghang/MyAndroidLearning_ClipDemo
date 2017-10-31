package com.example.yanghang.clipboard.Fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.yanghang.clipboard.ActivityCalendar;
import com.example.yanghang.clipboard.ActivityEditInfo;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemsData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.OthersView.DateChooseWheelViewDialog;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.CalendarHelper;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.CalendarListView;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.sample.CalendarItemAdapter;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.sample.CustomCalendarItemModel;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.sample.ListDataAdapter;
import com.example.yanghang.clipboard.R;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.yanghang.clipboard.MainFormActivity.LIST_DATA;
import static com.example.yanghang.clipboard.MainFormActivity.LIST_DATA_POS;
import static com.example.yanghang.clipboard.MainFormActivity.REQUEST_TEXT_EDITE_BACK;
import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendar extends Fragment {


    public FragmentCalendar() {
        // Required empty public constructor
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgLoading = msg.getData().getInt(MSG_LOAD_DB_INFO);
            if (msgLoading == LOAD_DB_FINISHED) {
                Log.d(TAG, "handleMessage: Calendar load finished");
                listDataAdapter.setDateDataMap(activityCalendar.listTreeMap);
                listDataAdapter.notifyDataSetChanged();
//                calendarAdapter.notifyDataSetChanged();

            }
        }
    };

    private View mView;
    private Toolbar toolbar;
    private CalendarListView calendarGridView;
    private ListDataAdapter listDataAdapter;
    private CalendarItemAdapter calendarAdapter;
    public static final String CALENDAR_CATALOGUE_NAME = "collect_calendar_catalogue";
    private AlertDialog loadingDialog;
    private DBListInfoManager dbListInfoManager;

    public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat YEAR_MONTH_YUE_FORMAT = new SimpleDateFormat("yyyy年MM月");
    public static final String MSG_LOAD_DB_INFO = "msg_load_db_info";
    public static final int LOAD_DB_FINISHED = 789;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_calendar, container, false);
        dbListInfoManager=new DBListInfoManager(getActivity());
        initialView();
        initialCalendarView();


        //关键一步
        setHasOptionsMenu(true);

        return mView;
    }
    ActivityCalendar activityCalendar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCalendar= (ActivityCalendar) context;

    }

    private void showAddCalendarItemDialog() {
        View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.calendar_add_item, null);
        RecyclerView recyclerView = view.findViewById(R.id.calendar_items_recyclerView);
        // 设置布局，否则无法正常使用
        final List<CalendarItemsData> lists = activityCalendar.calendarImageManager.getVisibleLists();

        CalendarAddItemsAdapter calendarItemAdapter = new CalendarAddItemsAdapter(lists, getActivity());
        calendarItemAdapter.setOnItemClickListener(new CalendarAddItemsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                String remarkName=lists.get(position).getCalendarItemPic();
                addCalendarItem(remarkName);

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(calendarItemAdapter);

        loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("新建记录" + DAY_FORMAT.format(Calendar.getInstance().getTime())).show();
    }
    private void addCalendarItem(String strName)
    {
        List<ListData> todayLists=activityCalendar.listTreeMap.get(DAY_FORMAT.format(Calendar.getInstance().getTime()));
        if (todayLists!=null){
            for(int i=0;i<todayLists.size();i++)
            {
                if (todayLists.get(i).getRemarks().equals(strName)&&todayLists.get(i).getCatalogue().equals(FragmentCalendar.CALENDAR_CATALOGUE_NAME))
                {
                    Intent intent = new Intent(getActivity(), ActivityEditInfo.class);
                    intent.putExtra(LIST_DATA_POS, i);
                    intent.putExtra(LIST_DATA, todayLists.get(i));
                    startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                    loadingDialog.dismiss();
                    return;
                }
            }
        }

        int orderid =dbListInfoManager.getDataCount();
        Intent intent = new Intent(getActivity(), ActivityEditInfo.class);
        intent.putExtra(LIST_DATA_POS, -1);
        intent.putExtra(LIST_DATA, new ListData(strName, "", orderid, CALENDAR_CATALOGUE_NAME));
        startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
        loadingDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_calendar:
                DateChooseWheelViewDialog endDateChooseDialog = new DateChooseWheelViewDialog(getActivity(),DAY_FORMAT.format(Calendar.getInstance().getTime()),
                        new DateChooseWheelViewDialog.DateChooseInterface() {
                            @Override
                            public void getDateTime(String time, boolean longTimeChecked) {
                                calendarGridView.setDate(time);
                            }
                        });

                endDateChooseDialog.setTimePickerGone(true);
                endDateChooseDialog.setDateDialogTitle("跳转日期");
                endDateChooseDialog.showDateChooseDialog();

                break;
            case R.id.action_calendar_add_item:
                showAddCalendarItemDialog();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_calendar, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    private void initialView() {
        toolbar = (Toolbar) mView.findViewById(R.id.calendar_toolbar);
        toolbar.setTitle("Calendar");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.setTitleTextColor(getActivity().getColor(R.color.white));
        }
        ((ActivityCalendar) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        calendarGridView = mView.findViewById(R.id.fragment_calendar_listview);
        //声明CalendarView和ListView的Adapter，分别为上面1、2步自定义的样式的Adapter
        listDataAdapter = new ListDataAdapter(getActivity());
        listDataAdapter.setOnListDataLongClickListener(new ListDataAdapter.OnListDataLongClickListener() {
            @Override
            public void onLongClick(ListData data, int pos) {
                Intent intent;
                if (data.getCatalogue().equals("番剧")) {
                    Toast.makeText(getActivity(), "不能从此处修改", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    intent = new Intent(getActivity(), ActivityEditInfo.class);
                }

                intent.putExtra(LIST_DATA_POS, pos);
                intent.putExtra(LIST_DATA, data);
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
            }
        });
        calendarAdapter = new CalendarItemAdapter(getActivity());
        //在设置Adapter之前必须设置初始日期
        calendarGridView.setCurrentSelectedDate(DAY_FORMAT.format(Calendar.getInstance().getTime()));
        //给calendarListView设置Adapter
        calendarGridView.setCalendarListViewAdapter(calendarAdapter, listDataAdapter);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TEXT_EDITE_BACK:
                if (resultCode == RESULT_OK) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = data.getIntExtra(LIST_DATA_POS, 0);
//
//                    Log.v(TAG, "返回后 current pos=" + pos + " 数据为：  order=" + listData.getOrderID() + "  catalogue=" + listData.getCatalogue());
                    listDataAdapter.setDataItem(pos, listData.getCreateDate().split("\n")[0], listData);
                    dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(), listData.getRemarks(), listData.getContent(), listData.getCreateDate());

                }else
                if (resultCode == ActivityEditInfo.RESULT_ADD_NEW) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = 0;
//                    Log.v("TEM", pos + listData.getContent());
                    long result = dbListInfoManager.insertData(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());
                    listDataAdapter.addDataItem(listData.getCreateDate().split("\n")[0], listData);
                    loadCalendarData(YEAR_MONTH_FORMAT.format(Calendar.getInstance().getTime()));
                    if (result == -1)
                        Toast.makeText(getActivity(), "存储该行数据出错", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    private void initialCalendarView() {
        // set start time,just for test.
        Calendar calendar = Calendar.getInstance();
        loadNewList(DAY_FORMAT.format(calendar.getTime()));
        loadCalendarData(YEAR_MONTH_FORMAT.format(calendar.getTime()));
        toolbar.setTitle(YEAR_MONTH_YUE_FORMAT.format(calendar.getTime()));

        // deal with refresh and load more event.
        calendarGridView.setOnListPullListener(new CalendarListView.onListPullListener() {
            //就是这个弄了好久好久
            @Override
            public void onRefresh() {
                String date = calendarGridView.getCurrentSelectedDate();
                Log.d(TAG, "onFresh: current selected date=" + date);
                loadNewList(date);
            }

            @Override
            public void onLoadMore() {
                String date = calendarGridView.getCurrentSelectedDate();
                Log.d(TAG, "onLoadMore: current selected date=" + date);
//                loadNewList(date);
            }
        });

        //
        calendarGridView.setOnMonthChangedListener(new CalendarListView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(String yearMonth) {
                Calendar calendar = CalendarHelper.getCalendarByYearMonth(yearMonth);
                toolbar.setTitle(YEAR_MONTH_YUE_FORMAT.format(calendar.getTime()));
//                Log.d(TAG, "onMonthChanged: yearmonth" + yearMonth);
//                Log.d(TAG, "onMonthChanged: currentSelectedDate=" + calendarGridView.getCurrentSelectedDate());

                if (calendarGridView.getCurrentSelectedDate() != null && !calendarGridView.getCurrentSelectedDate().equals("")) {
                    String strDay = calendarGridView.getCurrentSelectedDate().split("-")[2];
                    calendarGridView.setCurrentSelectedDate(yearMonth + "-" + strDay);
                    loadNewList(yearMonth + "-" + strDay);
//                    calendarGridView.onChangeMonth();
                    loadCalendarData(yearMonth);
                }


            }
        });
        calendarGridView.setOnCalendarViewItemClickListener(new CalendarListView.OnCalendarViewItemClickListener() {
            @Override
            public void onDateSelected(View View, String selectedDate, int listSection, SelectedDateRegion selectedDateRegion) {
//                Log.d(TAG, "onDateSelected: Selected date="+selectedDate);
//                loadNewList(selectedDate);


            }
        });


    }

    /***
     *
     * @param date yyyy-MM
     */
    private void loadCalendarData(final String date) {
        new Thread()
        {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<String> visibleNames = activityCalendar.calendarImageManager.getVisibleNames();

                        for (String d : activityCalendar.listTreeMap.keySet()) {
//                            Log.d(TAG, "FragmentCalendar loadCalendarData run: currentDatekey"+d);
                            if (date.equals(d.substring(0, 7))) {
                                CustomCalendarItemModel itemCalendarModel = calendarAdapter.getDayModelList().get(d);
                                if (itemCalendarModel != null) {
                                    List<ListData> currentDayLists=activityCalendar.listTreeMap.get(d);
                                    itemCalendarModel.setNewsCount(currentDayLists.size());
                                    for (int i=0;i<currentDayLists.size();i++)
                                    {
                                        ListData listData = currentDayLists.get(i);
                                        if (!listData.getCatalogue().equals(CALENDAR_CATALOGUE_NAME))
                                            continue;
                                        String remarkName=listData.getRemarks();
//                                        Log.d(TAG, "run: Remark="+remarkName);
//                                        if(activityCalendar.calendarImageManager.containsTag(remarkName)&&activityCalendar.calendarImageManager.getTagVisibility(remarkName))
                                        //就这么一个优化就让程序由不能运行（就加了上面这句）到能运行
                                        if(visibleNames.contains(remarkName))
                                        itemCalendarModel.addImage(remarkName);
                                    }

                                }

                            }
                        }
                        Log.d(TAG, "run: loadCalendarItem");
                        calendarAdapter.notifyDataSetChanged();
                    }
                },500);
            }
        }.start();

    }

    //date:yyyy-MM-dd
    private void loadNewList(final String date) {
        Log.d(TAG, "loadNewList: loadDate=" + date);

        new Thread(new Runnable() {
            @Override
            public void run() {
                activityCalendar.loadDBToDataTree(date);
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt(MSG_LOAD_DB_INFO, LOAD_DB_FINISHED);
                msg.setData(data);
                handler.sendMessage(msg);

            }
        }).start();


    }



}
