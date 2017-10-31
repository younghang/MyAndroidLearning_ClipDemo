package com.example.yanghang.clipboard.OthersView.calendarlistview.library.sample;

import android.util.Log;

import com.example.yanghang.clipboard.OthersView.calendarlistview.library.BaseCalendarItemModel;

import java.util.ArrayList;
import java.util.List;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * Created by kelin on 16-7-20.
 */
public class CustomCalendarItemModel  extends BaseCalendarItemModel{
    private int newsCount;

    private boolean isJP;
    private boolean isDiary;
    private boolean isLuser;
    private boolean isWeight;
    private boolean isCheck;
    private boolean isFire;
    private boolean isLike;
    private boolean isRest;
    private boolean isStar;
    private boolean isCode;
    private boolean isPaint;
    private boolean isMoneySpend;
    private boolean isMoneyIncome;

    public int imageCount=0;
    public List<String> pics = new ArrayList<>();
    public void addImage(String tag)
    {
        if (!pics.contains(tag))
        {
            pics.add(tag);
            imageCount++;
        }

    }


    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }


}
