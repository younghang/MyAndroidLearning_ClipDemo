package com.example.yanghang.clipboard.Fragment;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.ActivityCalendar;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemsData;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.OthersView.AutoFixText.AutofitTextView;
import com.example.yanghang.clipboard.OthersView.CalendarTrendView;
import com.example.yanghang.clipboard.R;
import com.linechart.DateString;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.sevenheaven.segmentcontrol.SegmentControl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendarTimeline extends Fragment {
    private View mView;
    private Toolbar toolbar;
    private TextView tvDateTime;
    private TextView tvTag;
    private TextView tvTotal;
    private TextView tvActiveDays;
    private TextView tvMax;
    private CalendarTrendView calendarTrendView;
    private ProgressBar statLoadingView;
    private Button selectedStatButton;
    private String currentSelectDate;
    private String currentPic;
    private Handler handler = new Handler()
    {

    };


    String currentTag;
    HorizontalScrollView horizontalScrollView;
    LinearLayout linearLayout;
    private SegmentControl mSegmentHorzontal;
    private SegmentControl chartTypeSegment;
    private int chartMode = CalendarTrendView.MODE_BAR;
    private int statMode = STAT_MODE_DAY;

    private static final int STAT_MODE_DAY = 0;
    private static final int STAT_MODE_MONTH = 1;
    private static final int STAT_MODE_YEAR = 2;
    private volatile int chartLoadVersion = 0;

    //x轴坐标对应的数据
    private List<String> xValue = new ArrayList<>();
    //y轴坐标对应的数据
    private List<Integer> yValue = new ArrayList<>();
    //折线对应的数据
    private Map<String, Float> value = new HashMap<>();
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DecimalFormat VALUE_FORMAT = new DecimalFormat("0.#");


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
        mSegmentHorzontal = mView.findViewById(R.id.segment_control2);
        mSegmentHorzontal.setSelectedIndex(0);
        mSegmentHorzontal.setOnSegmentControlClickListener(new SegmentControl.OnSegmentControlClickListener() {
            @Override
            public void onSegmentControlClick(int index) {
                statMode = index;
                loadTagChart();
            }
        });
        chartTypeSegment = mView.findViewById(R.id.calendar_chart_type_segment);
        chartTypeSegment.setSelectedIndex(0);
        chartTypeSegment.setOnSegmentControlClickListener(new SegmentControl.OnSegmentControlClickListener() {
            @Override
            public void onSegmentControlClick(int index) {
                chartMode = index == 1 ? CalendarTrendView.MODE_LINE : CalendarTrendView.MODE_BAR;
                updateChartView();
            }
        });
        tvTag = mView.findViewById(R.id.calendar_timeline_tag);
        tvTotal = mView.findViewById(R.id.calendar_stat_total);
        tvActiveDays = mView.findViewById(R.id.calendar_stat_active);
        tvMax = mView.findViewById(R.id.calendar_stat_max);
        setupStatValueAutoSize(tvTotal);
        setupStatValueAutoSize(tvActiveDays);
        setupStatValueAutoSize(tvMax);
        statLoadingView = mView.findViewById(R.id.calendar_stat_loading);
        calendarTrendView = mView.findViewById(R.id.calendar_trend_view);
        String todayStr = new DateString(Calendar.getInstance().getTime()).getDate();
        currentSelectDate = todayStr;
        updateDateText(todayStr);
        tvDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMonthPicker();
            }
        });
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object tag = view.getTag();
                if (tag instanceof CalendarItemsData) {
                    selectCalendarItem((CalendarItemsData) tag);
                    setSelectedStatButton((Button) view);
                } else {
                    currentTag = ((Button) view).getText().toString();
                    currentPic = tag == null ? "" : tag.toString();
                    if (currentPic.equals("")) {
                        currentPic = currentTag;
                    }
                    tvTag.setText(((Button) view).getText());
                    setSelectedStatButton((Button) view);
                }
//                Log.d(TAG, "onClick: Tag=" + currentTag);
                loadTagChart();


            }
        };


        horizontalScrollView = mView.findViewById(R.id.calendar_horizontalScrollView);
        linearLayout = mView.findViewById(R.id.calendar_timeline_linearLayout);
        List<CalendarItemsData> list=activityCalendar.calendarImageManager.getVisibleLists();
        for (CalendarItemsData item : list) {
            addTextView(item);
        }
        if (!list.isEmpty()) {
            selectCalendarItem(list.get(0));
            if (linearLayout.getChildCount() > 0 && linearLayout.getChildAt(0) instanceof Button) {
                setSelectedStatButton((Button) linearLayout.getChildAt(0));
            }
            loadTagChart();
        }



        //因为他的ChartView 有问题，必须在主线程中，先初始化的时候将几个数据存储大小设定好
        //不然改不了，而且会抖动,后来解决了
//        loadDataToChart();
//        testChart();

    }

    private void loadMonthTimeLine() {
        new Thread() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Float> valueTemp = new HashMap<>();
                        List<String> xTemp = new ArrayList<String>();
                        Integer maxY=0;
                        valueTemp.clear();
                        String year=new DateString(currentSelectDate).getYear()+"";
                        for (int i=1;i<=12;i++)
                        {
                            xTemp.add(i + "月");
                            loadDataToChart(year+(i>9?"-"+i:"-0"+i)+"-01");
                            float monthTotal=0f;

                            for (String key:value.keySet())
                            {

                                monthTotal+=value.get(key);
                                BigDecimal b = new BigDecimal(monthTotal);
                                monthTotal= b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                            }
                            if (monthTotal>maxY)
                                maxY=(int)monthTotal;
                            valueTemp.put(i + "月", monthTotal);
                            Log.d(TAG, "FragmentCalendar loadMonthTime run: "+i + "月  total="+ monthTotal);
                        }
                        value.clear();
                        value.putAll(valueTemp);
                        xValue.clear();
                        xValue.addAll(xTemp);

                        yValue.clear();
                        if (maxY == 0)
                            maxY = 1;
                        int Ylines = 0;
                        if (maxY < 6)
                            Ylines = maxY;
                        else Ylines = 6;

                        for (int i = 0; i < Ylines + 2; i++) {
                            int current = (int) (1.0f*maxY / Ylines * i);
                            if (!yValue.contains(current))
                                yValue.add(current);
                        }
                        Log.d(TAG, "FragmentCalendar loadMonthTime run:x0="+xValue.get(0)+ "  values 0()="+value.get(xValue.get(0))+"  y0="+yValue.get(0));
                        updateChartView();
                    }
                });
            }
        }.start();


    }

    private void showMonthPicker() {
        Date selectDate = Calendar.getInstance().getTime();
        try {
            selectDate = DAY_FORMAT.parse(currentSelectDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TimePickerDialog yearMonthDialog = new TimePickerDialog.Builder()
                .setType(Type.YEAR_MONTH)
                .setTitleStringId("选择年月")
                .setCurrentMillseconds(selectDate.getTime())
                .setCallBack(new OnDateSetListener() {
                    @Override
                    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
                        currentSelectDate = DAY_FORMAT.format(new Date(millseconds));
                        updateDateText(currentSelectDate);
                        loadTagChart();
                    }
                })
                .setThemeColor(getResources().getColor(R.color.colorPrimary))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.light_gray))
                .build();
        yearMonthDialog.show(((ActivityCalendar) getActivity()).getSupportFragmentManager(), "calendar_year_month");
    }

    private void updateDateText(String date) {
        String text = date.substring(0, 4) + "年"
                + date.substring(5, 7) + "月";
        tvDateTime.setText(text);
    }

    private void loadWeekTimeLine() {
        new Thread() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Float> valueTemp = new HashMap<>();
                        List<String> xTemp = new ArrayList<String>();
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(DAY_FORMAT.parse(currentSelectDate));
                        } catch (ParseException e) {
                            calendar.setTime(Calendar.getInstance().getTime());
                        }
                        calendar.add(Calendar.DAY_OF_MONTH, -6);
                        for (int i = 0; i < 7; i++) {
                            String date = DAY_FORMAT.format(calendar.getTime());
                            String label = date.substring(5).replace("-", ".");
                            xTemp.add(label);
                            valueTemp.put(label, loadSingleDayValue(date));
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        value.clear();
                        value.putAll(valueTemp);
                        xValue.clear();
                        xValue.addAll(xTemp);
                        yValue.clear();
                        updateChartView();
                    }
                });
            }
        }.start();
    }



    private void loadTagChart() {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        final int loadVersion = ++chartLoadVersion;
        final String selectedDate = currentSelectDate;
        final String selectedTag = currentTag;
        final String selectedPic = currentPic;
        final int selectedStatMode = statMode;
        showStatLoading(true);
        new Thread() {
            @Override
            public void run() {
                final List<ListData> sourceDatas = new DBListInfoManager(context.getApplicationContext()).getDatas("");
                final ChartData chartData = buildCurrentStatData(sourceDatas, selectedDate, selectedTag, selectedPic, selectedStatMode);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (loadVersion != chartLoadVersion || getActivity() == null) {
                            return;
                        }
                        value.clear();
                        value.putAll(chartData.value);
                        xValue.clear();
                        xValue.addAll(chartData.xValue);
                        yValue.clear();
                        updateChartView();
                        showStatLoading(false);
                    }
                });
            }
        }.start();


    }

    private void showStatLoading(boolean show) {
        if (statLoadingView != null) {
            statLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (calendarTrendView != null) {
            calendarTrendView.setAlpha(show ? 0.65f : 1f);
        }
    }

    private void setSelectedStatButton(Button button) {
        if (selectedStatButton != null) {
            selectedStatButton.setSelected(false);
            selectedStatButton.setTextColor(getActivity().getResources().getColor(R.color.black_80000000));
        }
        selectedStatButton = button;
        if (selectedStatButton != null) {
            selectedStatButton.setSelected(true);
            selectedStatButton.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
        }
    }

    private ChartData buildCurrentStatData(List<ListData> sourceDatas, String selectedDate, String selectedTag, String selectedPic, int selectedStatMode) {
        switch (selectedStatMode) {
            case STAT_MODE_MONTH:
                return buildMonthStatData(sourceDatas, selectedDate, selectedTag, selectedPic);
            case STAT_MODE_YEAR:
                return buildYearStatData(sourceDatas, selectedDate, selectedTag, selectedPic);
            case STAT_MODE_DAY:
            default:
                return buildDayStatData(sourceDatas, selectedDate, selectedTag, selectedPic);
        }
    }

    private ChartData buildDayStatData(List<ListData> sourceDatas, String selectedDate, String selectedTag, String selectedPic) {
        ChartData chartData = new ChartData();
        DateString dateString = new DateString(selectedDate);
        Map<String, String> dateToLabel = new HashMap<>();
        String monthPrefix = dateString.getYearMonth();
        for (int i = 1; i <= dateString.getDayCountOfMonth(); i++) {
            String label = dateString.getMonth() + "." + i;
            String day = monthPrefix + "-" + ((i > 9) ? i : "0" + i);
            chartData.xValue.add(label);
            chartData.value.put(label, 0f);
            dateToLabel.put(day, label);
        }
        if (sourceDatas == null) {
            return chartData;
        }
        if (isAccountSummaryItem(selectedTag, selectedPic)) {
            addAccountDayValues(chartData.value, dateToLabel, collectAccountData(sourceDatas), selectedTag, selectedPic);
            return chartData;
        }
        for (ListData listData : sourceDatas) {
            String label = dateToLabel.get(getRecordDay(listData));
            if (label != null) {
                addCalendarRecordValue(chartData, label, listData, selectedTag, selectedPic);
            }
        }
        chartData.finishAverageValues();
        return chartData;
    }

    private ChartData buildMonthStatData(List<ListData> sourceDatas, String selectedDate, String selectedTag, String selectedPic) {
        ChartData chartData = new ChartData();
        int selectedYear = new DateString(selectedDate).getYear();
        String yearPrefix = selectedYear + "-";
        for (int month = 1; month <= 12; month++) {
            String label = month + "\u6708";
            chartData.xValue.add(label);
            chartData.value.put(label, 0f);
        }
        if (sourceDatas == null) {
            return chartData;
        }
        if (isAccountSummaryItem(selectedTag, selectedPic)) {
            addAccountMonthValues(chartData.value, selectedYear, collectAccountData(sourceDatas), selectedTag, selectedPic);
            return chartData;
        }
        for (ListData listData : sourceDatas) {
            String recordDay = getRecordDay(listData);
            if (!recordDay.startsWith(yearPrefix) || recordDay.length() < 7) {
                continue;
            }
            int month = parseIntSafe(recordDay.substring(5, 7), -1);
            if (month < 1 || month > 12) {
                continue;
            }
            addCalendarRecordValue(chartData, month + "\u6708", listData, selectedTag, selectedPic);
        }
        chartData.finishAverageValues();
        return chartData;
    }

    private ChartData buildYearStatData(List<ListData> sourceDatas, String selectedDate, String selectedTag, String selectedPic) {
        ChartData chartData = new ChartData();
        int selectedYear = new DateString(selectedDate).getYear();
        int startYear = selectedYear - 9;
        for (int year = startYear; year <= selectedYear; year++) {
            String label = String.valueOf(year);
            chartData.xValue.add(label);
            chartData.value.put(label, 0f);
        }
        if (sourceDatas == null) {
            return chartData;
        }
        if (isAccountSummaryItem(selectedTag, selectedPic)) {
            addAccountYearValues(chartData.value, startYear, selectedYear, collectAccountData(sourceDatas), selectedTag, selectedPic);
            return chartData;
        }
        for (ListData listData : sourceDatas) {
            String recordDay = getRecordDay(listData);
            if (recordDay.length() < 4) {
                continue;
            }
            int year = parseIntSafe(recordDay.substring(0, 4), -1);
            if (year < startYear || year > selectedYear) {
                continue;
            }
            addCalendarRecordValue(chartData, String.valueOf(year), listData, selectedTag, selectedPic);
        }
        chartData.finishAverageValues();
        return chartData;
    }

    private void addCalendarRecordValue(ChartData chartData, String label, ListData listData, String selectedTag, String selectedPic) {
        if (isWeightItem(selectedTag, selectedPic)) {
            addAverageRecordValue(chartData, label, listData, selectedTag, selectedPic);
        } else {
            addRecordValue(chartData.value, label, listData, selectedTag, selectedPic);
        }
    }

    private void addRecordValue(Map<String, Float> target, String label, ListData listData, String selectedTag, String selectedPic) {
        if (!matchesCalendarItem(listData, selectedTag, selectedPic)) {
            return;
        }
        Float currentValue = target.get(label);
        target.put(label, (currentValue == null ? 0f : currentValue) + parseCurrentValue(listData, selectedTag, selectedPic));
    }

    private void addAverageRecordValue(ChartData chartData, String label, ListData listData, String selectedTag, String selectedPic) {
        if (!matchesCalendarItem(listData, selectedTag, selectedPic)) {
            return;
        }
        Float parsedValue = parseNumericCurrentValue(listData, selectedTag, selectedPic);
        if (parsedValue == null) {
            return;
        }
        Float currentValue = chartData.value.get(label);
        Integer currentCount = chartData.averageCounts.get(label);
        chartData.value.put(label, (currentValue == null ? 0f : currentValue) + parsedValue);
        chartData.averageCounts.put(label, (currentCount == null ? 0 : currentCount) + 1);
    }

    private void addAccountDayValues(Map<String, Float> target, Map<String, String> dateToLabel, List<AccountData> accounts, String selectedTag, String selectedPic) {
        for (AccountData accountData : accounts) {
            String label = dateToLabel.get(getAccountDay(accountData));
            if (label != null) {
                addAccountValue(target, label, accountData, selectedTag, selectedPic);
            }
        }
    }

    private void addAccountMonthValues(Map<String, Float> target, int selectedYear, List<AccountData> accounts, String selectedTag, String selectedPic) {
        for (AccountData accountData : accounts) {
            String accountDay = getAccountDay(accountData);
            if (accountDay.length() < 7) {
                continue;
            }
            int year = parseIntSafe(accountDay.substring(0, 4), -1);
            int month = parseIntSafe(accountDay.substring(5, 7), -1);
            if (year == selectedYear && month >= 1 && month <= 12) {
                addAccountValue(target, month + "\u6708", accountData, selectedTag, selectedPic);
            }
        }
    }

    private void addAccountYearValues(Map<String, Float> target, int startYear, int selectedYear, List<AccountData> accounts, String selectedTag, String selectedPic) {
        for (AccountData accountData : accounts) {
            String accountDay = getAccountDay(accountData);
            if (accountDay.length() < 4) {
                continue;
            }
            int year = parseIntSafe(accountDay.substring(0, 4), -1);
            if (year >= startYear && year <= selectedYear) {
                addAccountValue(target, String.valueOf(year), accountData, selectedTag, selectedPic);
            }
        }
    }

    private List<AccountData> collectAccountData(List<ListData> sourceDatas) {
        List<AccountData> result = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (ListData listData : sourceDatas) {
            if (!isAccountBookRecord(listData)) {
                continue;
            }
            List<AccountData> accounts = parseAccountList(listData);
            for (AccountData accountData : accounts) {
                String key = buildAccountUniqueKey(accountData);
                if (seenKeys.add(key)) {
                    result.add(accountData);
                }
            }
        }
        return result;
    }

    private List<AccountData> parseAccountList(ListData listData) {
        if (!isAccountBookRecord(listData)) {
            return new ArrayList<>();
        }
        try {
            List<AccountData> accounts = JSONArray.parseArray(listData.getContent(), AccountData.class);
            return accounts == null ? new ArrayList<AccountData>() : accounts;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private boolean isAccountBookRecord(ListData listData) {
        return listData != null && "记账".equals(listData.getCatalogue());
    }

    private String buildAccountUniqueKey(AccountData accountData) {
        if (accountData == null) {
            return "";
        }
        return safeString(accountData.getAccountTime()) + "|"
                + accountData.getMoney() + "|"
                + safeString(accountData.getType()) + "|"
                + safeString(accountData.getContent()) + "|"
                + safeString(accountData.getRemark());
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private void addAccountValue(Map<String, Float> target, String label, AccountData accountData, String selectedTag, String selectedPic) {
        double money = accountData.getMoney();
        float amount = 0f;
        if (isIncomeItem(selectedTag, selectedPic) && money > 0) {
            amount = (float) money;
        } else if (isExpenseItem(selectedTag, selectedPic) && money < 0) {
            amount = (float) Math.abs(money);
        }
        if (amount <= 0f) {
            return;
        }
        Float currentValue = target.get(label);
        target.put(label, (currentValue == null ? 0f : currentValue) + amount);
    }

    private String getAccountDay(AccountData accountData) {
        if (accountData == null || accountData.getAccountTime() == null || accountData.getAccountTime().length() < 10) {
            return "";
        }
        return accountData.getAccountTime().substring(0, 10);
    }

    private boolean isAccountSummaryItem(String selectedTag, String selectedPic) {
        return isIncomeItem(selectedTag, selectedPic) || isExpenseItem(selectedTag, selectedPic);
    }

    private boolean isIncomeItem(String selectedTag, String selectedPic) {
        return equalsValue(selectedTag, "收入") || equalsValue(selectedPic, "income") || equalsValue(selectedTag, "income");
    }

    private boolean isExpenseItem(String selectedTag, String selectedPic) {
        return equalsValue(selectedTag, "支出") || equalsValue(selectedPic, "cost") || equalsValue(selectedTag, "cost");
    }

    private boolean isWeightItem(String selectedTag, String selectedPic) {
        return equalsValue(selectedTag, "体重") || equalsValue(selectedPic, "weight") || equalsValue(selectedTag, "weight");
    }

    private String getRecordDay(ListData listData) {
        if (listData == null || listData.getCreateDate() == null || listData.getCreateDate().length() < 10) {
            return "";
        }
        return listData.getCreateDate().substring(0, 10);
    }

    private int parseIntSafe(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private boolean matchesCalendarItem(ListData listData, String selectedTag, String selectedPic) {
        return equalsValue(listData.getRemarks(), selectedTag)
                || equalsValue(listData.getRemarks(), selectedPic)
                || equalsValue(listData.getCatalogue(), selectedTag)
                || equalsValue(listData.getCatalogue(), selectedPic);
    }

    private float parseCurrentValue(ListData listData, String selectedTag, String selectedPic) {
        Float parsedValue = parseNumericCurrentValue(listData, selectedTag, selectedPic);
        if (parsedValue != null) {
            return parsedValue;
        }
        return 1f;
    }

    private Float parseNumericCurrentValue(ListData listData, String selectedTag, String selectedPic) {
        String content = getCurrentItemContent(listData, selectedTag, selectedPic);
        try {
            return Float.parseFloat(cleanNumberText(content));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String cleanNumberText(String content) {
        if (content == null) {
            return "";
        }
        String text = content.trim()
                .replace("kg", "")
                .replace("KG", "")
                .replace("公斤", "")
                .replace("￥", "")
                .replace("元", "")
                .replace(",", "")
                .trim();
        StringBuilder builder = new StringBuilder();
        boolean hasNumber = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.' || c == '-') {
                builder.append(c);
                hasNumber = true;
            } else if (hasNumber) {
                break;
            }
        }
        return builder.toString();
    }

    private String getCurrentItemContent(ListData listData, String selectedTag, String selectedPic) {
        if (equalsValue(listData.getCatalogue(), selectedTag) || equalsValue(listData.getCatalogue(), selectedPic)) {
            return listData.getRemarks();
        }
        return listData.getSimpleContent();
    }

    private static class ChartData {
        Map<String, Float> value = new HashMap<>();
        Map<String, Integer> averageCounts = new HashMap<>();
        List<String> xValue = new ArrayList<>();

        void finishAverageValues() {
            for (String label : averageCounts.keySet()) {
                Integer count = averageCounts.get(label);
                Float sum = value.get(label);
                if (count != null && count > 0 && sum != null) {
                    value.put(label, sum / count);
                }
            }
        }
    }

    private void loadCurrentStatData() {
        switch (statMode) {
            case STAT_MODE_MONTH:
                loadMonthStatData();
                break;
            case STAT_MODE_YEAR:
                loadYearStatData();
                break;
            case STAT_MODE_DAY:
            default:
                loadDataToChart(currentSelectDate);
                break;
        }
    }

    private void loadMonthStatData() {
        Map<String, Float> valueTemp = new HashMap<>();
        List<String> xTemp = new ArrayList<>();
        String year = new DateString(currentSelectDate).getYear() + "";
        for (int i = 1; i <= 12; i++) {
            String monthDate = year + (i > 9 ? "-" + i : "-0" + i) + "-01";
            String label = i + "月";
            xTemp.add(label);
            valueTemp.put(label, loadMonthTotal(monthDate));
        }
        value.clear();
        value.putAll(valueTemp);
        xValue.clear();
        xValue.addAll(xTemp);
        yValue.clear();
    }

    private void loadYearStatData() {
        Map<String, Float> valueTemp = new HashMap<>();
        List<String> xTemp = new ArrayList<>();
        int selectedYear = new DateString(currentSelectDate).getYear();
        int startYear = selectedYear - 9;
        for (int year = startYear; year <= selectedYear; year++) {
            String label = String.valueOf(year);
            float yearTotal = 0f;
            for (int month = 1; month <= 12; month++) {
                String monthDate = year + (month > 9 ? "-" + month : "-0" + month) + "-01";
                yearTotal += loadMonthTotal(monthDate);
            }
            xTemp.add(label);
            valueTemp.put(label, yearTotal);
        }
        value.clear();
        value.putAll(valueTemp);
        xValue.clear();
        xValue.addAll(xTemp);
        yValue.clear();
    }

    private float loadMonthTotal(String date) {
        loadDataToChart(date);
        float monthTotal = 0f;
        for (String key : value.keySet()) {
            monthTotal += value.get(key);
        }
        BigDecimal b = new BigDecimal(monthTotal);
        return b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private void updateChartView() {
        if (calendarTrendView != null) {
            calendarTrendView.setChartMode(chartMode);
            calendarTrendView.setAlwaysShowValueLabels(statMode == STAT_MODE_MONTH);
            calendarTrendView.setData(value, xValue, tvTag == null ? "" : tvTag.getText().toString());
        }
        updateSummary();
    }

    private void updateSummary() {
        float total = 0f;
        float max = 0f;
        int activeDays = 0;
        for (String key : xValue) {
            Float dayValue = value.get(key);
            float current = dayValue == null ? 0f : dayValue;
            total += current;
            if (current > 0f) {
                activeDays++;
            }
            if (current > max) {
                max = current;
            }
        }
        tvTotal.setText(formatCompactValue(total));
        tvActiveDays.setText(String.valueOf(activeDays));
        tvMax.setText(formatCompactValue(max));
    }

    private static String formatCompactValue(float value) {
        if (Math.abs(value) >= 10000f) {
            return VALUE_FORMAT.format(value / 10000f) + "w";
        }
        return VALUE_FORMAT.format(value);
    }

    private float loadSingleDayValue(String date) {
        activityCalendar.loadDBToDataTree(date);
        List<ListData> listDatas = activityCalendar.listTreeMap.get(date);
        if (listDatas == null) {
            return 0f;
        }
        for (ListData listData : listDatas) {
            if (matchesCurrentItem(listData)) {
                return parseCurrentValue(listData);
            }
        }
        return 0f;
    }

    //不负责数据获取工作，避免初始化的时候混乱，我又让他负责了
    private void loadDataToChart(String date) {
        activityCalendar.loadDBToDataTree(date);
        DateString dateString = new DateString(date);
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

            xValue.add(xi);
//
            List<ListData> listDatas = activityCalendar.listTreeMap.get(currentdayString);

            if (listDatas != null) {
//                Log.d(TAG, "loadDataToChart: fragment currentDay dataSize=" + listDatas.size());
                for (int j = 0; j < listDatas.size(); j++) {
                    //多加个或(||)就可以，选取该数据进行绘图
                    if (matchesCurrentItem(listDatas.get(j))) {
                        String content = getCurrentItemContent(listDatas.get(j));
//                        if (content.contains("."))
//                            content = content.substring(0, content.indexOf("."));
//                        Log.d(TAG, "loadDataToChart: content=" + content);
                        isAdd = true;
                        try {
                            currentY = Float.parseFloat(content);
                            if (currentY > maxY)
                                maxY = (int)currentY;
                            value.put(xi, currentY);

//                            Log.d(TAG, "loadDataToChart: yi=" + currentY);
                        } catch (NumberFormatException e) {

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

                        value.put(xi, 0f);
//                        Log.d(TAG, "loadDataToChart: not num yi=0");


                }
            } else {

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



    }

    private void selectCalendarItem(CalendarItemsData item) {
        currentTag = item.getCalendarItemName();
        currentPic = item.getCalendarItemPic();
        tvTag.setText(item.getCalendarItemName());
    }

    private boolean matchesCurrentItem(ListData listData) {
        return equalsValue(listData.getRemarks(), currentTag)
                || equalsValue(listData.getRemarks(), currentPic)
                || equalsValue(listData.getCatalogue(), currentTag)
                || equalsValue(listData.getCatalogue(), currentPic);
    }

    private float parseCurrentValue(ListData listData) {
        String content = getCurrentItemContent(listData);
        try {
            return Float.parseFloat(content);
        } catch (NumberFormatException e) {
            return 1f;
        }
    }

    private String getCurrentItemContent(ListData listData) {
        if (equalsValue(listData.getCatalogue(), currentTag) || equalsValue(listData.getCatalogue(), currentPic)) {
            return listData.getRemarks();
        }
        return listData.getSimpleContent();
    }

    private boolean equalsValue(String left, String right) {
        return left != null && right != null && left.equals(right);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addTextView(CalendarItemsData item) {
        Button tv = new Button(getActivity(),null,0,R.style.borderless);
        tv.setText(item.getCalendarItemName());
        tv.setTag(item);
        tv.setMinWidth(dpToPx(50));
        tv.setOnClickListener(onClickListener);
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5));
        tv.setTextColor(getActivity().getColor(R.color.black_80000000));


//        LinearLayout layout = new LinearLayout(getActivity());
//        layout.setLayoutParams( new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
//        layout.setGravity(Gravity. CENTER);

        linearLayout.addView(initViewParams(tv));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private View initViewParams(View view) {
        //  动态添加布局
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,dpToPx(40) );
        lp.setMargins(10, 5, 5,5);
        view.setLayoutParams(lp);

        view.setBackground(getActivity().getDrawable(R.drawable.bg_calendar_timeline_chip_selector));
        return view;
    }

    private void setupStatValueAutoSize(TextView textView) {
        if (textView == null) {
            return;
        }
        textView.setSingleLine(true);
        textView.setMaxLines(1);
        if (textView instanceof AutofitTextView) {
            AutofitTextView autofitTextView = (AutofitTextView) textView;
            autofitTextView.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
            autofitTextView.setMaxTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            autofitTextView.setSizeToFit(true);
        }
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }

}
