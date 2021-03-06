package com.example.yanghang.clipboard.Fragment.JsonData;

/**
 * Created by young on 2017/6/18.
 */

public class ToDoData {
    public ToDoData(String content, String endTime, boolean isFinished, boolean isCurrentDay) {
        this.content = content;
        this.endTime = endTime;
        this.isFinished = isFinished;
        this.isCurrentDay = isCurrentDay;
    }
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

    public boolean isCurrentDay() {
        return isCurrentDay;
    }

    public void setCurrentDay(boolean currentDay) {
        isCurrentDay = currentDay;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    private String content;
    //格式就是yyyy-mm-dd
    private String endTime;
    private boolean isFinished;
    //是否仅当日显示
    private boolean isCurrentDay;
    //是否为日常任务
    private boolean isDailyTask;

    public boolean isDailyTask() {
        return isDailyTask;
    }

    public void setDailyTask(boolean dailyTask) {
        isDailyTask = dailyTask;
    }

    public ToDoData(String content, String endTime, boolean isFinished, boolean isCurrentDay, boolean isDailyTask) {
        this.content = content;
        this.endTime = endTime;
        this.isFinished = isFinished;
        this.isCurrentDay = isCurrentDay;
        this.isDailyTask = isDailyTask;
    }
}

