package com.example.yanghang.clipboard.Task;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.ListPackage.DailyTaskList.DailyTaskData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * Created by young on 2017/6/19.
 */

public class TaskShowToDoList {
    private static final String DAILY_MISSION_CATALOGUE = "dailyMission";

    public interface IShowToDoList
    {
        public void showToDoList(String messageToDoList );
        public void showDailyList(List<DailyTaskData> mDailyList,ListData todayListData);
        public void showTodayMission(String todayMission);
    }
    private IShowToDoList showToDoList;
    private Context context;
    public TaskShowToDoList(Context context, IShowToDoList showToDoList)
    {
        this.showToDoList=showToDoList;
        this.context=context;
    }
    public void runToDoListCheck()
    {
        final StringBuilder stringBuilder = new StringBuilder();


        new Thread(new Runnable() {
            @Override
            public void run() {
                String todayMissionStr="";
                DBListInfoManager dbListInfoManager = new DBListInfoManager(context);
                List<ListData> listDatas=new DBListInfoManager(context).getDatas("待办事项");
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date nowDate= Calendar.getInstance().getTime();
                String todayString = DateFormat.format("yyyy-MM-dd", nowDate).toString();
                Date currentDate= null;
                try {
                    currentDate = sDateFormat.parse(todayString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                List<String> dailyTaskNames = new ArrayList<String>();
                for (int i=0;i<listDatas.size();i++)
                {
                    ToDoData toDoData=null;
                    try {
                        toDoData=JSON.parseObject(listDatas.get(i).getContent(), ToDoData.class);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (toDoData==null)
                    {
                        continue;
                    }
                    ToDoData boardData = ToDoData.normalizeBoard(toDoData);
                    if (boardData.isDeleted()) {
                        continue;
                    }

                    List<ToDoData> tasks = boardData.getTasks();
                    for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
                        ToDoData taskData = tasks.get(taskIndex);
                        if (taskData == null || taskData.isFinished() || taskData.isDeleted()) {
                            continue;
                        }
                        Date endDate=null;
                        try {
                            endDate = sDateFormat.parse(taskData.getEndTime());
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if (endDate == null) {
                            continue;
                        }
                        if (!endDate.before(currentDate))
                        {
                            if (boardData.isDailyTask() || taskData.isDailyTask())
                            {
                                addDailyTaskName(dailyTaskNames, taskData.getDisplayTitle());
                                continue;
                            }
//                        Log.d(TAG, "run: endDate"+endDate.toString()+"   current:"+currentDate.toString());
                            if (endDate.toString().equals(currentDate.toString())) {

                                if (taskData.isCurrentDay()) {
                                    todayMissionStr+=taskData.getDisplayTitle();
                                    stringBuilder.append("[今日提醒]:" + taskData.getDisplayTitle() + "\n");
                                }
                                else
                                {
                                    todayMissionStr+=taskData.getDisplayTitle();
                                    stringBuilder.append("[今日任务]:" + taskData.getDisplayTitle() + "\n");
                                }
                            }
                            else {
                                if (taskData.isCurrentDay())
                                {
                                    continue;
                                }
                                else
                                    stringBuilder.append("["+taskData.getEndTime()+"]:"+taskData.getDisplayTitle()+"\n");
                            }
                        }
                    }
                }
                if (!dailyTaskNames.isEmpty()) {
                    List<DailyTaskData> dailyList = getOrCreateDailyMissionRecords(dbListInfoManager, todayString, dailyTaskNames);
                    showToDoList.showDailyList(dailyList, null);
                }
                showToDoList.showToDoList(stringBuilder.toString());
                showToDoList.showTodayMission(todayMissionStr);
            }
        }).start();
    }

    public static String getTodayString() {
        return DateFormat.format("yyyy-MM-dd", Calendar.getInstance().getTime()).toString();
    }

    public static void updateDailyMissionList(DBListInfoManager dbListInfoManager, String date, List<DailyTaskData> dailyList) {
        if (dailyList == null) {
            return;
        }
        List<ListData> dailyMissionRecords = dbListInfoManager.getDatas(DAILY_MISSION_CATALOGUE);
        for (int i = 0; i < dailyList.size(); i++) {
            DailyTaskData dailyTaskData = dailyList.get(i);
            if (dailyTaskData == null || dailyTaskData.gettN() == null || dailyTaskData.gettN().trim().equals("")) {
                continue;
            }
            String taskName = dailyTaskData.gettN().trim();
            ListData dailyMissionRecord = findDailyMissionRecord(dailyMissionRecords, taskName);
            if (dailyMissionRecord == null) {
                dailyMissionRecord = createDailyMissionRecord(dbListInfoManager, taskName);
                dailyMissionRecords.add(dailyMissionRecord);
            }
            JSONObject dailyMissionContent = parseDailyMissionContent(dailyMissionRecord.getContent());
            dailyMissionContent.put(date, new DailyTaskData(taskName, dailyTaskData.gettP()));
            dailyMissionRecord.setContent(JSON.toJSONString(dailyMissionContent));
            dbListInfoManager.updateDataByOrderId(dailyMissionRecord.getOrderID(), dailyMissionRecord.getCatalogue(), taskName, dailyMissionRecord.getContent(), dailyMissionRecord.getCreateDate());
        }
    }

    private static void addDailyTaskName(List<String> taskNames, String taskName) {
        if (taskName == null) {
            return;
        }
        String currentTask = taskName.trim();
        if (currentTask.equals("") || currentTask.startsWith("#") || taskNames.contains(currentTask)) {
            return;
        }
        Log.d(TAG, "run: currentStr=" + currentTask);
        taskNames.add(currentTask);
    }

    private List<DailyTaskData> getOrCreateDailyMissionRecords(DBListInfoManager dbListInfoManager, String todayString, List<String> dailyTaskNames) {
        List<ListData> dailyMissionRecords = dbListInfoManager.getDatas(DAILY_MISSION_CATALOGUE);
        List<DailyTaskData> dailyList = new ArrayList<DailyTaskData>();
        for (int i = 0; i < dailyTaskNames.size(); i++) {
            String taskName = dailyTaskNames.get(i);
            ListData dailyMissionRecord = findDailyMissionRecord(dailyMissionRecords, taskName);
            if (dailyMissionRecord == null) {
                dailyMissionRecord = createDailyMissionRecord(dbListInfoManager, taskName);
                dailyMissionRecords.add(dailyMissionRecord);
            }

            JSONObject dailyMissionContent = parseDailyMissionContent(dailyMissionRecord.getContent());
            DailyTaskData dailyTaskData = parseDailyTaskData(dailyMissionContent.get(todayString), taskName);
            if (dailyTaskData == null) {
                dailyTaskData = new DailyTaskData(taskName, 0);
                dailyMissionContent.put(todayString, dailyTaskData);
                dailyMissionRecord.setContent(JSON.toJSONString(dailyMissionContent));
                dbListInfoManager.updateDataByOrderId(dailyMissionRecord.getOrderID(), dailyMissionRecord.getCatalogue(), taskName, dailyMissionRecord.getContent(), dailyMissionRecord.getCreateDate());
            }
            dailyList.add(dailyTaskData);
        }
        return dailyList;
    }

    private static ListData createDailyMissionRecord(DBListInfoManager dbListInfoManager, String taskName) {
        ListData dailyMissionRecord = new ListData(taskName, JSON.toJSONString(new JSONObject()), dbListInfoManager.getDataCount(), DAILY_MISSION_CATALOGUE);
        dbListInfoManager.insertData(dailyMissionRecord);
        return dailyMissionRecord;
    }

    private static ListData findDailyMissionRecord(List<ListData> dailyMissionRecords, String taskName) {
        if (dailyMissionRecords == null) {
            return null;
        }
        for (int i = 0; i < dailyMissionRecords.size(); i++) {
            ListData listData = dailyMissionRecords.get(i);
            if (listData != null && taskName.equals(listData.getRemarks())) {
                return listData;
            }
        }
        return null;
    }

    private static JSONObject parseDailyMissionContent(String content) {
        JSONObject dailyMissionContent = new JSONObject();
        if (content == null || content.trim().equals("")) {
            return dailyMissionContent;
        }
        try {
            Object object = JSON.parse(content);
            if (object instanceof JSONObject) {
                return (JSONObject) object;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dailyMissionContent;
    }

    private static DailyTaskData parseDailyTaskData(Object object, String taskName) {
        if (object == null) {
            return null;
        }
        try {
            DailyTaskData dailyTaskData = JSON.parseObject(JSON.toJSONString(object), DailyTaskData.class);
            if (dailyTaskData != null && (dailyTaskData.gettN() == null || dailyTaskData.gettN().trim().equals(""))) {
                dailyTaskData.settN(taskName);
            }
            return dailyTaskData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

