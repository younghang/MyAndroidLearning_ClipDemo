package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import android.text.format.DateFormat;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.example.yanghang.clipboard.ActivityAccountBook;
import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.Fragment.JsonData.DiaryData;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountData;
import com.example.yanghang.clipboard.ListPackage.BangumiList.BangumiData;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * Created by yanghang on 2016/11/22.
 */
public class ListData implements Serializable {
    private String Remarks = "";
    private String Content = "";
    private String CreateDate = "";
    private int OrderID = 0;
    private String Catalogue = "default";

    public ListData(String remarks, String content, int orderID, String catalogue) {
        Remarks = remarks;
        Content = content;
        OrderID = orderID;
        Catalogue = catalogue;
        CreateDate = GetDate();
    }

    public ListData(String remarks, String content, String createDate, int orderID, String catalogue) {
        Remarks = remarks;
        Content = content;
        CreateDate = createDate;
        OrderID = orderID;
        Catalogue = catalogue;
    }

    public static String GetDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    public String getCatalogue() {
        return Catalogue;
    }

    public void setCatalogue(String catalogue) {
        Catalogue = catalogue;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public int getOrderID() {
        return OrderID;
    }

    public void setOrderID(int orderID) {
        OrderID = orderID;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String remarks) {
        Remarks = remarks;
    }

    public String getContent() {
        return Content;
    }
    public String getSimpleContent()
    {

        String strMessage=Content;
        switch (Catalogue)
        {
            case "待办事项":
                String endDate="";
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDate = sDateFormat.format(new java.util.Date());
                Date endDateObject=null;
                Date todayDateObject=null;
                try {
                    todayDateObject= sDateFormat.parse(DateFormat.format("yyyy-MM-dd", Calendar.getInstance().getTime()).toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ToDoData toDoData =null;
                try {
                    toDoData= JSON.parseObject(strMessage, ToDoData.class);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                String extraMessage="";
                if (toDoData!=null)
                {
                    strMessage=toDoData.getContent();
                    strMessage=strMessage.replace('\n',' ');
                    endDate=toDoData.getEndTime();
                    try {
                        endDateObject=sDateFormat.parse(endDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    extraMessage=(toDoData.isDailyTask()?"\n<日常任务>":"")+(toDoData.isDailyTask()?(endDateObject.before(todayDateObject)?"\nMission Out of Date":(toDoData.isFinished()?"\n****Finished****":"\n****Running****")):"")+(toDoData.isDailyTask()||toDoData.isFinished()?"":"\n***** Not  Finished *****");
                }
                else
                {

                    endDate=todayDate;
                    extraMessage="\n[数据格式化出错,非待办事项数据,请删除！]";
                }


                strMessage=strMessage+"\n截止日期["+endDate+"]"+extraMessage;
                break;
            case "番剧":
                try {
                    List<BangumiData> list = JSONArray.parseArray(Content, BangumiData.class);

                    strMessage="《";
                    for (int i=0; i<list.size()-1;i++) {
                        strMessage+=list.get(i).getName()+"》，《";
                    }
                    strMessage+=list.get(list.size()-1).getName()+"》";
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    strMessage="Error Data！";
                }
                break;
            case "记账":
                double expenditure=0;
                double income=0;
                List<AccountData> list = JSONArray.parseArray(Content, AccountData.class);
                if (list==null)
                {
                    list= new ArrayList<>();
                }
                for (AccountData data : list) {
                    if (data.getMoney()<0)
                    {
                        expenditure += data.getMoney();
                    }else
                        income+=data.getMoney();
                }
                DecimalFormat df = new DecimalFormat("0.00");
                strMessage = "收入=" + df.format(income) + "     支出=" + df.format(Math.abs(expenditure)) + "     总算=" + df.format(income + expenditure);
                break;
            case FragmentCalendar.CALENDAR_CATALOGUE_NAME:
                switch (Remarks){
                    case "diary":
                        String morningDiary="";
                        String afternoonDiary="";
                        String eveningDiary="";
                        DiaryData diaryData=null;
                        try {
                            diaryData = JSON.parseObject(strMessage, DiaryData.class);

                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if (diaryData != null) {
                            morningDiary=diaryData.getMorningString();
                            afternoonDiary = diaryData.getAfternoonString();
                            eveningDiary = diaryData.getEveningString();
                        }
                        strMessage="morning:"+morningDiary+
                                "afternoon:"+afternoonDiary+
                                "evening:"+eveningDiary;
                        break;

                }
                break;

        }
        return strMessage;

    }


    public void setContent(String content) {
        Content = content;
    }
}
