package com.example.yanghang.clipboard.Task;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.JsonData.DiaryData;
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
                for (int i=0;i<listDatas.size();i++)
                {
                    ToDoData toDoData=null;
                    try {
                        toDoData=JSON.parseObject(listDatas.get(i).getContent(), ToDoData.class);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (toDoData==null||toDoData.isFinished())
                    {
                        continue;
                    }

                    Date endDate=null;
                    try {
                        endDate = sDateFormat.parse(toDoData.getEndTime());
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (endDate == null) {
                        continue;
                    }
                    if (!endDate.before(currentDate))
                    {
                        if (toDoData.isDailyTask())
                        {
//                            Log.d(TAG, "run: "+toDoData.getContent());
                            String[] dailyListString = new String[]{};
                            dailyListString=toDoData.getContent().split("\n");
                            List<ListData> lists = new DBListInfoManager(context).searchDataByDate(todayString);
                            List<DailyTaskData> dailyList=new ArrayList<DailyTaskData>();
                            boolean hasSaved=false;
                            ListData listData=null;
                            for (int j=0;j<lists.size();j++)
                            {
                                if (lists.get(j).getCatalogue().equals("dailyMission"))
                                {
                                    hasSaved=true;
                                    listData = lists.get(j);
                                    try{
                                        dailyList = JSONArray.parseArray(lists.get(j).getContent(),DailyTaskData.class);

                                    }catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                     }
                            }

                            if (hasSaved==false)
                            {
                                for (int k=0;k<dailyListString.length;k++) {
                                    if (dailyListString[k].equals("")||dailyListString[k].startsWith("#"))
                                        continue;
                                    Log.d(TAG, "run: currentStr="+dailyListString[k]);
                                    dailyList.add(new DailyTaskData(dailyListString[k], 0));
                                }
                                DBListInfoManager dbListInfoManager=new DBListInfoManager(context);
                                listData=new ListData("",JSONArray.toJSONString(dailyList),dbListInfoManager.getDataCount(),"dailyMission");
                                dbListInfoManager .insertData(listData);

                            }
                            showToDoList.showDailyList(dailyList,listData);
                            continue;
                        }
//                        Log.d(TAG, "run: endDate"+endDate.toString()+"   current:"+currentDate.toString());
                        if (endDate.toString().equals(currentDate.toString())) {

                            if (toDoData.isCurrentDay()) {
                                todayMissionStr+=toDoData.getContent();
                                stringBuilder.append("[今日提醒]:" + toDoData.getContent() + "\n");
                            }
                            else
                            {
                                todayMissionStr+=toDoData.getContent();
                                stringBuilder.append("[今日任务]:" + toDoData.getContent() + "\n");
                            }
                        }
                        else {
                            if (toDoData.isCurrentDay())
                            {
                                continue;
                            }
                            else
                                stringBuilder.append("["+toDoData.getEndTime()+"]:"+toDoData.getContent()+"\n");
                        }
                    }
                }
                showToDoList.showToDoList(stringBuilder.toString());
                showToDoList.showTodayMission(todayMissionStr);
            }
        }).start();
    }


}

