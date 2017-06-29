package com.example.yanghang.clipboard.Task;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        public void showToDoList(String messageToDoList);
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
                List<ListData> listDatas=new DBListInfoManager(context).getDatas("待办事项");
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date nowDate= Calendar.getInstance().getTime();
                String time = DateFormat.format("yyyy-MM-dd", nowDate).toString();
                Date currentDate= null;
                try {
                    currentDate = sDateFormat.parse(time);
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
                    if (toDoData==null)
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
                    if (!endDate.before(currentDate)&&!toDoData.isFinished())
                    {
                        Log.d(TAG, "run: endDate"+endDate.toString()+"   current:"+currentDate.toString());
                        if (endDate.toString().equals(currentDate.toString())) {

                            if (toDoData.isCurrentDay()) {
                                stringBuilder.append("[今日提醒]:" + toDoData.getContent() + "\n");
                            }
                            else
                            {
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
            }
        }).start();
    }

}

