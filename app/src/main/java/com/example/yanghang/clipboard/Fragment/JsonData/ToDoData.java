package com.example.yanghang.clipboard.Fragment.JsonData;

/**
 * Created by young on 2017/6/18.
 */

public class ToDoData {
    public ToDoData(boolean isFinished, String content, String endTime) {
        this.isFinished = isFinished;
        this.content = content;
        this.endTime = endTime;
    }
    public ToDoData()
    {

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    private String content;
    private String endTime;
    private boolean isFinished;

}

