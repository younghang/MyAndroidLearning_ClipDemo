package com.linechart;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by young on 2017/6/30.
 */

public class DateString {
    //yyyy-MM-dd
    private String date;
    private int day;
    private int month;
    private int year;

    //yyyy-MM-dd
    public DateString(String date) {
        this.date = date;
        day=Integer.parseInt(date.split("-")[2]);
        month=Integer.parseInt(date.split("-")[1]);
        year=Integer.parseInt(date.split("-")[0]);
    }
    public DateString(Date dateTime) {
        this.date =  new SimpleDateFormat("yyyy-MM-dd").format(dateTime);
        day=Integer.parseInt(date.split("-")[2]);
        month=Integer.parseInt(date.split("-")[1]);
        year=Integer.parseInt(date.split("-")[0]);
    }
    public String getYearMonth()
    {
        return date.substring(0,7);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDayCountOfMonth()
    {
        int dayCount=1;
        switch (month){
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                dayCount=31;
                break;

            case 4:
            case 6:
            case 9:
            case 11:
                dayCount=30;
                break;
            case 2:
                if(year % 4 == 0 && year % 100 != 0 || year % 400 == 0){
                    dayCount=29;
                }else{
                    dayCount=28;
                }
                break;
        }
        return dayCount;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
