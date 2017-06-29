package com.example.yanghang.clipboard.Fragment.JsonData;

/**
 * Created by young on 2017/6/29.
 */

public class DiaryData {
    private String morningString;
    private String afternoonString;
    private String eveningString;
    public DiaryData()
    {

    }

    public DiaryData(String morningString, String afternoonString, String eveningString) {
        this.morningString = morningString;
        this.afternoonString = afternoonString;
        this.eveningString = eveningString;
    }

    public String getMorningString() {
        return morningString;
    }

    public void setMorningString(String morningString) {
        this.morningString = morningString;
    }

    public String getAfternoonString() {
        return afternoonString;
    }

    public void setAfternoonString(String afternoonString) {
        this.afternoonString = afternoonString;
    }

    public String getEveningString() {
        return eveningString;
    }

    public void setEveningString(String eveningString) {
        this.eveningString = eveningString;
    }
}
