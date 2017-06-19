package com.example.yanghang.clipboard.Task;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
                Date currentDate= Calendar.getInstance().getTime();
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
                    if (endDate.after(currentDate)&&!toDoData.isFinished())
                    {
                        stringBuilder.append("["+toDoData.getEndTime()+"]:"+toDoData.getContent()+"\n");
                    }



                }
                showToDoList.showToDoList(stringBuilder.toString());
            }
        }).start();
    }

}

