package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.example.yanghang.clipboard.ActivityAccountBook;
import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.Fragment.JsonData.AssetData;
import com.example.yanghang.clipboard.Fragment.JsonData.DiaryData;
import com.example.yanghang.clipboard.Fragment.JsonData.RichTextData;
import com.example.yanghang.clipboard.Fragment.JsonData.ResearchTopicData;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountData;
import com.example.yanghang.clipboard.ListPackage.BangumiList.BangumiData;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
                    strMessage = buildTodoMainView(boardData);
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
            case AssetData.CATALOGUE_NAME:
                strMessage = AssetData.parse(Content).buildSimpleContent();
                break;
            case ResearchTopicData.CATALOGUE_NAME:
                strMessage = ResearchTopicData.parse(Content).buildSimpleContent();
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
            default:
                strMessage = RichTextData.toPlainText(Content);
                break;

        }
        return strMessage;

    }


    public void setContent(String content) {
        Content = content;
    }

    private String buildTodoMainView(ToDoData boardData) {
        List<ToDoData> tasks = boardData.getTasks();
        int total = 0;
        int done = 0;
        int doing = 0;
        int high = 0;
        int urgent = 0;
        int trash = 0;
        int progressTotal = 0;
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
        }
        int progress = total == 0 ? 0 : progressTotal / total;
        String today = dateOffset(0);
        String weekEnd = dateOffset(7);

        StringBuilder builder = new StringBuilder();
        builder.append(total == 0 ? "暂无任务" : "阶段任务 " + total + " 项");
        builder.append(" · 完成 ").append(done).append(" · 进行中 ").append(doing).append(" · 进度 ").append(progress).append("%");
        if (boardData.isDailyTask()) {
            builder.append(" · 日常阶段");
        }
        if (boardData.isDeleted()) {
            builder.append(" · 回收站");
        }
        builder.append("\n高优先级 ").append(high).append(" · 紧急 ").append(urgent).append(" · 回收站 ").append(trash);

        appendTodoGroup(builder, "逾期", tasks, today, weekEnd, 5);
        appendTodoGroup(builder, "今天", tasks, today, weekEnd, 0);
        appendTodoGroup(builder, "本周", tasks, today, weekEnd, 1);
        appendTodoGroup(builder, "以后", tasks, today, weekEnd, 2);
        appendTodoGroup(builder, "已完成", tasks, today, weekEnd, 3);
        appendTodoGroup(builder, "回收站", tasks, today, weekEnd, 4);
        return builder.toString();
    }

    private void appendTodoGroup(StringBuilder builder, String title, List<ToDoData> tasks, String today, String weekEnd, int group) {
        List<ToDoData> groupTasks = new ArrayList<ToDoData>();
        for (int i = 0; i < tasks.size(); i++) {
            ToDoData task = tasks.get(i);
            if (belongsToTodoGroup(task, today, weekEnd, group)) {
                groupTasks.add(task);
            }
        }
        if (groupTasks.size() == 0) {
            return;
        }
        sortTodoTasks(groupTasks);
        builder.append("\n\n").append(title);
        for (int i = 0; i < groupTasks.size(); i++) {
            builder.append("\n").append(formatTodoTaskLine(groupTasks.get(i), today));
        }
    }

    private boolean belongsToTodoGroup(ToDoData task, String today, String weekEnd, int group) {
        if (task == null) {
            return false;
        }
        if (group == 4) {
            return task.isDeleted();
        }
        if (task.isDeleted()) {
            return false;
        }
        if (group == 3) {
            return task.isFinished();
        }
        if (task.isFinished()) {
            return false;
        }
        String endDate = task.getEndTime().trim();
        boolean isToday = task.isCurrentDay() || today.equals(endDate);
        if (group == 5) {
            return !isToday && !endDate.equals("") && endDate.compareTo(today) < 0;
        }
        if (group == 0) {
            return isToday;
        }
        if (group == 1) {
            return !isToday && !endDate.equals("") && endDate.compareTo(today) > 0 && endDate.compareTo(weekEnd) <= 0;
        }
        return !isToday && (endDate.equals("") || endDate.compareTo(weekEnd) > 0);
    }

    private void sortTodoTasks(List<ToDoData> tasks) {
        Collections.sort(tasks, new Comparator<ToDoData>() {
            @Override
            public int compare(ToDoData left, ToDoData right) {
                int result = urgencyRank(right) - urgencyRank(left);
                if (result != 0) {
                    return result;
                }
                result = importanceRank(right) - importanceRank(left);
                if (result != 0) {
                    return result;
                }
                result = markerRank(right) - markerRank(left);
                if (result != 0) {
                    return result;
                }
                result = priorityRank(right) - priorityRank(left);
                if (result != 0) {
                    return result;
                }
                result = compareTodoDate(left.getEndTime(), right.getEndTime());
                if (result != 0) {
                    return result;
                }
                return left.getDisplayTitle().compareTo(right.getDisplayTitle());
            }
        });
    }

    private String formatTodoTaskLine(ToDoData task, String today) {
        StringBuilder builder = new StringBuilder();
        builder.append(statusMark(task)).append(" ");
        builder.append("●").append(markerText(task)).append(" ");
        builder.append(task.getDisplayTitle());
        builder.append("  ").append(priorityShortText(task));
        if (ToDoData.LEVEL_HIGH.equals(task.getImportance())) {
            builder.append("  重要");
        }
        if (ToDoData.LEVEL_HIGH.equals(task.getUrgency())) {
            builder.append("  紧急");
        }
        if (task.isDailyTask()) {
            builder.append("  日常");
        }
        String dateTag = dateTag(task.getEndTime(), today);
        if (!dateTag.equals("")) {
            builder.append("  ").append(dateTag);
        }
        if (task.getProgress() > 0 || task.isFinished()) {
            builder.append("  ").append(task.getProgress()).append("%");
        }
        return builder.toString();
    }

    private String statusMark(ToDoData task) {
        if (task.isDeleted()) {
            return "[×]";
        }
        if (task.isFinished()) {
            return "[x]";
        }
        if (ToDoData.STATUS_DOING.equals(task.getStatus())) {
            return "[…]";
        }
        return "[ ]";
    }

    private String markerText(ToDoData task) {
        int rank = markerRank(task);
        if (rank >= 4) {
            return "红";
        }
        if (rank == 3) {
            return "橙";
        }
        if (rank == 2) {
            return "蓝";
        }
        return "灰";
    }

    private int markerRank(ToDoData task) {
        if (task.isDeleted() || task.isFinished()) {
            return 0;
        }
        if (ToDoData.LEVEL_HIGH.equals(task.getUrgency())) {
            return 4;
        }
        if (ToDoData.LEVEL_HIGH.equals(task.getImportance()) || ToDoData.PRIORITY_HIGH.equals(task.getPriority())) {
            return 3;
        }
        if (ToDoData.PRIORITY_LOW.equals(task.getPriority())) {
            return 1;
        }
        return 2;
    }

    private int urgencyRank(ToDoData task) {
        return ToDoData.LEVEL_HIGH.equals(task.getUrgency()) ? 1 : 0;
    }

    private int importanceRank(ToDoData task) {
        return ToDoData.LEVEL_HIGH.equals(task.getImportance()) ? 1 : 0;
    }

    private int priorityRank(ToDoData task) {
        if (ToDoData.PRIORITY_HIGH.equals(task.getPriority())) {
            return 3;
        }
        if (ToDoData.PRIORITY_LOW.equals(task.getPriority())) {
            return 1;
        }
        return 2;
    }

    private String priorityShortText(ToDoData task) {
        if (ToDoData.PRIORITY_HIGH.equals(task.getPriority())) {
            return "高";
        }
        if (ToDoData.PRIORITY_LOW.equals(task.getPriority())) {
            return "低";
        }
        return "中";
    }

    private int compareTodoDate(String leftDate, String rightDate) {
        String left = leftDate == null ? "" : leftDate.trim();
        String right = rightDate == null ? "" : rightDate.trim();
        if (left.equals("") && right.equals("")) {
            return 0;
        }
        if (left.equals("")) {
            return 1;
        }
        if (right.equals("")) {
            return -1;
        }
        return left.compareTo(right);
    }

    private String dateTag(String date, String today) {
        String value = date == null ? "" : date.trim();
        if (value.equals("")) {
            return "";
        }
        if (value.equals(today)) {
            return "今天";
        }
        if (value.length() >= 10) {
            return value.substring(5);
        }
        return value;
    }

    private String dateOffset(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return sDateFormat.format(calendar.getTime());
    }

    private static BigDecimal moneyOf(double money) {
        return BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatMoney(BigDecimal money) {
        return money.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
