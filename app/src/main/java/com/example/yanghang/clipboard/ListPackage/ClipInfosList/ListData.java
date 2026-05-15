package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
                    ToDoData boardData = ToDoData.normalizeBoard(toDoData);
                    List<ToDoData> tasks = boardData.getTasks();
                    int total = 0;
                    int done = 0;
                    int doing = 0;
                    int high = 0;
                    int urgent = 0;
                    int trash = 0;
                    int progressTotal = 0;
                    String nearestDate = "";
                    for (int i = 0; i < tasks.size(); i++) {
                        ToDoData task = tasks.get(i);
                        if (task == null) {
                            continue;
                        }
                        if (task.isDeleted()) {
                            trash++;
                            continue;
                        }
                        total++;
                        progressTotal += task.getProgress();
                        if (task.isFinished()) {
                            done++;
                        }
                        if (ToDoData.STATUS_DOING.equals(task.getStatus())) {
                            doing++;
                        }
                        if (ToDoData.PRIORITY_HIGH.equals(task.getPriority())) {
                            high++;
                        }
                        if (ToDoData.LEVEL_HIGH.equals(task.getUrgency())) {
                            urgent++;
                        }
                        if (!task.getEndTime().trim().equals("") && (nearestDate.equals("") || task.getEndTime().compareTo(nearestDate) < 0)) {
                            nearestDate = task.getEndTime();
                        }
                    }
                    int progress = total == 0 ? 0 : progressTotal / total;
                    strMessage = total == 0 ? "暂无任务" : "阶段任务 " + total + " 项";
                    extraMessage = "\n完成 " + done + "  进行中 " + doing + "  进度 " + progress + "%";
                    extraMessage += "\n高优先级 " + high + "  紧急 " + urgent + "  回收站 " + trash;
                    if (!nearestDate.equals("")) {
                        extraMessage += "\n最近截止 " + nearestDate;
                    }
                    if (boardData.isDailyTask()) {
                        extraMessage += "\n<日常阶段>";
                    }
                    if (boardData.isDeleted()) {
                        extraMessage += "\n<回收站>";
                    }
                }
                else
                {
                    extraMessage="\n[数据格式化出错,非待办事项数据,请删除！]";
                }

                strMessage=strMessage+extraMessage;
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
                BigDecimal expenditure=BigDecimal.ZERO;
                BigDecimal income=BigDecimal.ZERO;
                List<AccountData> list = JSONArray.parseArray(Content, AccountData.class);
                if (list==null)
                {
                    list= new ArrayList<>();
                }
                for (AccountData data : list) {
                    if (data.getMoney()<0)
                    {
                        expenditure = expenditure.add(moneyOf(data.getMoney()));
                    }else
                        income = income.add(moneyOf(data.getMoney()));
                }
                strMessage = "收入=" + formatMoney(income) + "     支出=" + formatMoney(expenditure.abs()) + "     总算=" + formatMoney(income.add(expenditure));
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

    private static BigDecimal moneyOf(double money) {
        return BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatMoney(BigDecimal money) {
        return money.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
