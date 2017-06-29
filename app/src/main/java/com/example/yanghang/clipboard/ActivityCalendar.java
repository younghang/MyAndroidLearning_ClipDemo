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
import android.view.MenuItem;
import android.view.View;

import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.Fragment.FragmentCalendarItem;
import com.example.yanghang.clipboard.Fragment.FragmentCalendarTimeline;

public class ActivityCalendar extends AppCompatActivity implements ViewPager.OnPageChangeListener {
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
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager = (ViewPager) findViewById(R.id.calendar_viewPager);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager()));
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

        private Fragment[] fragments = new Fragment[]{new FragmentCalendar(), new FragmentCalendarTimeline(),
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
}
