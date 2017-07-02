package com.example.yanghang.clipboard.Fragment;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yanghang.clipboard.ActivityCalendar;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.OthersView.DateChooseWheelViewDialog;
import com.example.yanghang.clipboard.R;
import com.linechart.ChartView;
import com.linechart.DateString;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendarTimeline extends Fragment {
    private View mView;
    private Toolbar toolbar;
    private TextView tvDateTime;
    private TextView tvTag;
    private String currentSelectDate;
    private Handler handler = new Handler();
    String currentTag;
    HorizontalScrollView horizontalScrollView;
    LinearLayout linearLayout;


    //x轴坐标对应的数据
    private List<String> xValue = new ArrayList<>();
    //y轴坐标对应的数据
    private List<Integer> yValue = new ArrayList<>();
    //折线对应的数据
    private Map<String, Float> value = new HashMap<>();


    public FragmentCalendarTimeline() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_calendar_timeline, container, false);
        initialView();
        return mView;
    }

    ActivityCalendar activityCalendar;
    private View.OnClickListener onClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCalendar = (ActivityCalendar) context;
    }

    private void initialView() {
        toolbar = (Toolbar) mView.findViewById(R.id.calendar_toolbar_timeLine);
        toolbar.setTitle("");
//        ((ActivityCalendar) getActivity()).setSupportActionBar(toolbar);
        tvDateTime = mView.findViewById(R.id.calendar_timeline_date_tv);
        tvTag = mView.findViewById(R.id.calendar_timeline_tag);
        String todayStr = new DateString(Calendar.getInstance().getTime()).getDate();
        tvDateTime.setText(todayStr);
        currentSelectDate = todayStr;
        tvDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateChooseWheelViewDialog endDateChooseDialog = new DateChooseWheelViewDialog(getActivity(), new DateString(Calendar.getInstance().getTime()).getDate(),
                        new DateChooseWheelViewDialog.DateChooseInterface() {
                            @Override
                            public void getDateTime(String time, boolean longTimeChecked) {
                                currentSelectDate = time;
                                tvDateTime.setText(time.substring(0, 7).replace("-", "年") + "月");
                            }
                        });

                endDateChooseDialog.setTimePickerGone(true);
                endDateChooseDialog.setDateDialogTitle("选择日期");
                endDateChooseDialog.showDateChooseDialog();
            }
        });
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTag = ((TextView) view).getTag().toString();
                tvTag.setText(((TextView) view).getText());
                Log.d(TAG, "onClick: Tag=" + currentTag);
                loadTagChart();
            }
        };


        horizontalScrollView = mView.findViewById(R.id.calendar_horizontalScrollView);
        linearLayout = mView.findViewById(R.id.calendar_timeline_linearLayout);
        addTextView("luser", "luser");
        addTextView("日记", "diary");
        addTextView("体重", "weight");
        addTextView("日语", "jp");
        addTextView("火焰", "fire");
        addTextView("编程", "code");
        addTextView("画画", "paint");
        addTextView("爱心", "like");
        addTextView("完成", "check");
        addTextView("标记", "star");
        //因为他的ChartView 有问题，必须在主线程中，先初始化的时候将几个数据存储大小设定好
        //不然改不了，而且会抖动
//        loadDataToChart();
//        testChart();

    }

    void testChart() {
        for (int i = 0; i < 12; i++) {
            xValue.add((i + 1) + "月");
            value.put((i + 1) + "月", (float) (Math.random() * 181 + 60));//60--240
        }

        for (int i = 0; i < 6; i++) {
            yValue.add(i * 60);
        }

        ChartView chartView = (ChartView) mView.findViewById(R.id.chartView);
        chartView.setValue(value, xValue, yValue);
    }


    private void loadTagChart() {
        new Thread() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadDataToChart();
                    }
                });
            }
        }.start();


    }

    //不负责数据获取工作，避免初始化的时候混乱，我又让他负责了
    private void loadDataToChart() {
        activityCalendar.loadDBToDataTree(currentSelectDate);
        DateString dateString = new DateString(currentSelectDate);
        yValue.clear();
        xValue.clear();
        value.clear();

        int maxY = 0;
        float currentY = 0;

        for (int i = 1; i <= dateString.getDayCountOfMonth(); i++) {
            String xi = dateString.getMonth() + "." + i;
            String currentdayString = dateString.getYearMonth() + "-" + ((i > 9) ? i : "0" + i);
//            Log.d(TAG, "loadDataToChart: currentdayString"+currentdayString);
//            Log.d(TAG, "loadDataToChart: xi=" + xi);
            boolean isAdd = false;
            boolean isNum = false;
            xValue.add(xi);
//
            List<ListData> listDatas = activityCalendar.listTreeMap.get(currentdayString);

            if (listDatas != null) {
//                Log.d(TAG, "loadDataToChart: fragment currentDay dataSize=" + listDatas.size());
                for (int j = 0; j < listDatas.size(); j++) {

                    if (listDatas.get(j).getRemarks().equals(currentTag)) {
                        String content = listDatas.get(j).getSimpleContent();
//                        if (content.contains("."))
//                            content = content.substring(0, content.indexOf("."));
//                        Log.d(TAG, "loadDataToChart: content=" + content);
                        isAdd = true;
                        try {
                            currentY = Float.parseFloat(content);
                            if (currentY > maxY)
                                maxY = (int)currentY;
                            value.put(xi, currentY);
                            isNum = true;
//                            Log.d(TAG, "loadDataToChart: yi=" + currentY);
                        } catch (NumberFormatException e) {
                            isNum = false;
//                            e.printStackTrace();
                            maxY = 2;
                            value.put(xi, 1f);
//                            Log.d(TAG, "loadDataToChart: not num yi=1");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        break;

                    }

                }
                if (isAdd == false) {
                    if (!isNum) {
                        value.put(xi, 0f);
//                        Log.d(TAG, "loadDataToChart: not num yi=0");
                    }

                }
            } else {
                if (!isNum)
                    value.put(xi, 0f);//60--240
//                Log.d(TAG, "loadDataToChart: fragment currentDay data is null");
            }


        }
        if (maxY == 0)
            maxY = 1;
        int Ylines = 0;
        if (maxY < 6)
            Ylines = maxY;
        else Ylines = 6;

        for (int i = 0; i < Ylines + 2; i++) {
            int current = (int) (maxY / Ylines * i);
            if (!yValue.contains(current))
                yValue.add(current);
        }
        ChartView chartView = (ChartView) mView.findViewById(R.id.chartView);
        chartView.setValue(value, xValue, yValue);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addTextView(String name, String tag) {
        Button tv = new Button(getActivity());
        tv.setText(name);
        tv.setTag(tag);
        tv.setOnClickListener(onClickListener);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
        tv.setTextColor(getActivity().getColor(R.color.white));
        tv.setTextAppearance(R.style.borderless);

//        LinearLayout layout = new LinearLayout(getActivity());
//        layout.setLayoutParams( new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
//        layout.setGravity(Gravity. CENTER);

        linearLayout.addView(initViewParams(tv));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private View initViewParams(View view) {
        //  动态添加布局
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,dpToPx(40) );
        lp.setMargins(10, 5, 5, 5);
        view.setLayoutParams(lp);

        view.setBackground(getActivity().getDrawable(R.drawable.button_background));
        return view;
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }

}
