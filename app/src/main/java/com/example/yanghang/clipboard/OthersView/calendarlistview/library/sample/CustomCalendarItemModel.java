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

    public int imageCount=0;
    public List<String> pics = new ArrayList<>();

    public void setStar(boolean star) {

        if (star==true&&isStar==false)
        {
            imageCount++;
            pics.add("star");
        }
        isStar = star;
    }



    public void setCode(boolean code) {
        if (code==true&&isCode==false)
        {
            imageCount++;
            pics.add("code");
        }
        isCode = code;
    }


    public void setPaint(boolean paint) {
        if (paint==true&&isPaint==false)
        {
            imageCount++;
            pics.add("paint");
        }
        isPaint = paint;
    }

    public void setCheck(boolean check) {

        if (check==true&&isCheck==false)
        {
            imageCount++;
            pics.add("check");
        }
        isCheck = check;
    }

    public void setFire(boolean fire) {

        if (fire==true&&isFire==false)
        {
            imageCount++;
            pics.add("fire");
        }
        isFire = fire;

    }

    public void setLike(boolean like) {
        if (like==true&&isLike==false)
        {
            imageCount++;
            pics.add("like");
        }

        isLike = like;

    }

    public void setRest(boolean rest) {
        if (rest==true&&isRest==false)
        {
            imageCount++;
            pics.add("rest");
        }
        isRest = rest;

    }


    public void setJP(boolean JP) {
        if (JP==true&&isJP==false)
        {
            imageCount++;
            pics.add("jp");
        }
        isJP = JP;

    }



    public void setDiary(boolean diary) {
        Log.d(TAG, "setDiary: ");
        if (diary==true&&isDiary==false)
        {
            imageCount++;
            pics.add("diary");
        }
        isDiary = diary;

    }


    public void setLuser(boolean luser) {
        if (luser==true&&isLuser==false)
        {
            imageCount++;
            pics.add("luser");
        }
        isLuser = luser;
    }



    public void setWeight(boolean weight) {
        if (weight==true&&isWeight==false)
        {
            imageCount++;
            pics.add("weight");
        }
        isWeight = weight;

    }

    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }


}
