package com.example.yanghang.clipboard;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;

import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.Fragment.FragmentCalendarItem;
import com.example.yanghang.clipboard.Fragment.FragmentCalendarTimeline;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.yanghang.clipboard.Fragment.FragmentCalendar.DAY_FORMAT;
import static com.example.yanghang.clipboard.Fragment.FragmentCalendar.YEAR_MONTH_FORMAT;

public class ActivityCalendar extends AppCompatActivity implements ViewPager.OnPageChangeListener {


    //key:date "yyyy-mm-dd" format.
    public TreeMap<String, List<ListData>> listTreeMap = new TreeMap<>();
    public CalendarImageManager calendarImageManager ;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.calendar:
                    onCalendarClick();
                    return true;
                case R.id.calendar_timeline:
                    onTimelineClick();
                    return true;
                case R.id.calendar_item:
                    onCalendarItemClick();
                    return true;
            }
            return false;
        }

    };

    private void onCalendarItemClick() {
        viewPager.setCurrentItem(2);
    }

    private void onTimelineClick() {
        viewPager.setCurrentItem(1);
    }

    private void onCalendarClick() {
        viewPager.setCurrentItem(0);
    }

    private ViewPager viewPager;
    BottomNavigationView navigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        initialView();



    }


    private void initialView() {
        //把初始化加载放在这个地方，就能够避免两个Fragment线程冲突，导致加载出来的 List 数据错乱
        //但是会非常卡。所以我又把它去掉了
//        loadDBToDataTree(DAY_FORMAT.format(Calendar.getInstance().getTime()));
        calendarImageManager = new CalendarImageManager(this);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager = (ViewPager) findViewById(R.id.calendar_viewPager);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                navigation.setSelectedItemId(R.id.calendar);
                break;
            case 1:
                navigation.setSelectedItemId(R.id.calendar_timeline);
                break;
            case 2:
                navigation.setSelectedItemId(R.id.calendar_item);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public class ViewPageAdapter extends FragmentPagerAdapter {

        public Fragment[] fragments = new Fragment[]{new FragmentCalendar(), new FragmentCalendarTimeline(),
                new FragmentCalendarItem()};

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
    //do it in a thread
    public void loadDBToDataTree(String date) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sDateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date nowDate = Calendar.getInstance().getTime();
        String nowDateString = DateFormat.format("yyyy-MM-dd", nowDate).toString();
        try {
            nowDate = sDateFormat.parse(nowDateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentDate = null;
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        for (int i = -30; i <= 30; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, +1);
            String key = sDateFormat.format(calendar.getTime());
            try {
                currentDate = sDateFormat.parse(key);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (currentDate.after(nowDate)) {
                continue;
            }
            if (listTreeMap.containsKey(key)) {
                continue;
            }
            List<ListData> listDatas = new DBListInfoManager(this).searchDataByDate(key);
//                    Log.d(TAG, "loadNewList: date=" + key + "  items=" + listDatas.size());
            if (listDatas.size() == 0) {
                continue;
            }
            listTreeMap.put(key, listDatas);
        }
    }
}
