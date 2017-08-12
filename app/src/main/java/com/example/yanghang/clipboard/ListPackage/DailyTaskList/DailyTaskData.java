package com.example.yanghang.clipboard.ListPackage.DailyTaskList;

/**
 * Created by young on 2017/8/11.
 */

public class DailyTaskData {
    String taskName;
    int taskProgress;

    public DailyTaskData() {
    }

    public DailyTaskData(String taskName, int taskProgress) {
        this.taskName = taskName;
        this.taskProgress = taskProgress;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getTaskProgress() {
        return taskProgress;
    }

    public void setTaskProgress(int taskProgress) {
        this.taskProgress = taskProgress;
    }
}
