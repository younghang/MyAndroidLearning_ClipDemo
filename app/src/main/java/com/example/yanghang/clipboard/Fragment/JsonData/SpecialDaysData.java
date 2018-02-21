package com.example.yanghang.clipboard.Fragment.JsonData;

/**
 * Created by young on 2018/2/18.
 */

public class SpecialDaysData {
    String date;
    String info;

    public SpecialDaysData() {
    }

    public SpecialDaysData(String date, String info) {
        this.date = date;
        this.info = info;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
