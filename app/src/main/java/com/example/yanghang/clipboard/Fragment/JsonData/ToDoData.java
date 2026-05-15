package com.example.yanghang.clipboard.Fragment.JsonData;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by young on 2017/6/18.
 */
public class ToDoData {
    public static final String TYPE_BOARD = "board";
    public static final String TYPE_TASK = "task";
    public static final String STATUS_TODO = "todo";
    public static final String STATUS_DOING = "doing";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_TRASH = "trash";
    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";
    public static final String LEVEL_NORMAL = "normal";
    public static final String LEVEL_HIGH = "high";

    private String dataType;
    private String content;
    // Format: yyyy-MM-dd.
    private String endTime;
    private boolean isFinished;
    private boolean isCurrentDay;
    private boolean isDailyTask;
    private String taskId;
    private String parentId;
    private String title;
    private String status;
    private String priority;
    private String importance;
    private String urgency;
    private int progress;
    private String deletedAt;
    private List<ToDoData> tasks;

    public ToDoData() {
    }

    public ToDoData(String content, String endTime, boolean isFinished, boolean isCurrentDay) {
        this.content = content;
        this.endTime = endTime;
        this.isFinished = isFinished;
        this.isCurrentDay = isCurrentDay;
        this.status = isFinished ? STATUS_DONE : STATUS_TODO;
    }

    public ToDoData(boolean isFinished, String content, String endTime) {
        this.isFinished = isFinished;
        this.content = content;
        this.endTime = endTime;
        this.status = isFinished ? STATUS_DONE : STATUS_TODO;
    }

    public ToDoData(String content, String endTime, boolean isFinished, boolean isCurrentDay, boolean isDailyTask) {
        this.content = content;
        this.endTime = endTime;
        this.isFinished = isFinished;
        this.isCurrentDay = isCurrentDay;
        this.isDailyTask = isDailyTask;
        this.status = isFinished ? STATUS_DONE : STATUS_TODO;
    }

    public static ToDoData createBoard() {
        ToDoData board = new ToDoData();
        board.setDataType(TYPE_BOARD);
        board.setStatus(STATUS_TODO);
        board.setPriority(PRIORITY_MEDIUM);
        board.setImportance(LEVEL_NORMAL);
        board.setUrgency(LEVEL_NORMAL);
        board.setTasks(new ArrayList<ToDoData>());
        board.getTaskId();
        return board;
    }

    public static ToDoData createTask() {
        ToDoData task = new ToDoData();
        task.setDataType(TYPE_TASK);
        task.setStatus(STATUS_TODO);
        task.setPriority(PRIORITY_MEDIUM);
        task.setImportance(LEVEL_NORMAL);
        task.setUrgency(LEVEL_NORMAL);
        task.getTaskId();
        return task;
    }

    public static ToDoData normalizeBoard(ToDoData data) {
        if (data == null) {
            return createBoard();
        }
        if (TYPE_TASK.equals(data.getDataType())) {
            ToDoData board = createBoard();
            board.setStatus(data.getStatus());
            board.setDeletedAt(data.getDeletedAt());
            board.setDailyTask(data.isDailyTask());
            board.getTasks().add(data);
            return board;
        }
        if (TYPE_BOARD.equals(data.getDataType()) || data.tasks != null) {
            data.setDataType(TYPE_BOARD);
            data.getTaskId();
            data.getTasks();
            return data;
        }
        ToDoData board = createBoard();
        board.setStatus(data.getStatus());
        board.setDeletedAt(data.getDeletedAt());
        board.setDailyTask(data.isDailyTask());
        addLegacyLineTasks(board, data);
        if (board.getTasks().size() == 0 && !data.getTitle().trim().equals("")) {
            ToDoData task = createTaskFromLegacy(data);
            if (task.hasTaskContent()) {
                board.getTasks().add(task);
            }
        }
        return board;
    }

    public static ToDoData createBoardFromPlainText(String content, String endTime, boolean isCurrentDay, boolean isDailyTask) {
        ToDoData board = createBoard();
        addLineTasks(board, content, endTime, STATUS_TODO, PRIORITY_MEDIUM, LEVEL_NORMAL, LEVEL_NORMAL, isCurrentDay, isDailyTask);
        return board;
    }

    public static ToDoData createTaskFromLegacy(ToDoData oldData) {
        ToDoData task = createTask();
        if (oldData == null) {
            return task;
        }
        if (oldData.taskId == null || oldData.taskId.trim().equals("")) {
            task.setTaskId(stableLegacyTaskId(oldData.getTitle() + "|" + oldData.getContent() + "|" + oldData.getEndTime()));
        } else {
            task.setTaskId(oldData.taskId);
        }
        task.setTitle(oldData.getTitle());
        task.setContent(oldData.getContent());
        task.setEndTime(oldData.getEndTime());
        task.setCurrentDay(oldData.isCurrentDay());
        task.setDailyTask(oldData.isDailyTask());
        task.setParentId(oldData.getParentId());
        task.setStatus(oldData.getStatus());
        task.setFinished(oldData.isFinished());
        task.setPriority(oldData.getPriority());
        task.setImportance(oldData.getImportance());
        task.setUrgency(oldData.getUrgency());
        task.setProgress(oldData.getProgress());
        task.setDeletedAt(oldData.getDeletedAt());
        return task;
    }

    private static void addLegacyLineTasks(ToDoData board, ToDoData oldData) {
        if (oldData == null) {
            return;
        }
        addLineTasks(board, oldData.getContent(), oldData.getEndTime(), oldData.getStatus(), oldData.getPriority(),
                oldData.getImportance(), oldData.getUrgency(), oldData.isCurrentDay(), oldData.isDailyTask());
    }

    private static void addLineTasks(ToDoData board, String content, String endTime, String status, String priority,
                                     String importance, String urgency, boolean isCurrentDay, boolean isDailyTask) {
        if (board == null || content == null) {
            return;
        }
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String title = lines[i].trim();
            if (title.equals("") || title.startsWith("#")) {
                continue;
            }
            ToDoData task = createTask();
            task.setTaskId(stableLegacyTaskId(title + "|" + endTime + "|" + i));
            task.setTitle(title);
            task.setEndTime(endTime);
            task.setStatus(status);
            task.setFinished(STATUS_DONE.equals(status));
            task.setPriority(priority);
            task.setImportance(importance);
            task.setUrgency(urgency);
            task.setCurrentDay(isCurrentDay);
            task.setDailyTask(isDailyTask);
            board.getTasks().add(task);
        }
    }

    private static String stableLegacyTaskId(String value) {
        if (value == null) {
            value = "";
        }
        return "legacy-" + Integer.toHexString(value.hashCode());
    }

    public String getDataType() {
        if (dataType == null) {
            return "";
        }
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getContent() {
        if (content == null) {
            return "";
        }
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEndTime() {
        if (endTime == null) {
            return "";
        }
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
        if (STATUS_DONE.equals(status)) {
            return true;
        }
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
        if (finished) {
            status = STATUS_DONE;
        } else if (status == null || STATUS_DONE.equals(status)) {
            status = STATUS_TODO;
        }
    }

    public boolean isDailyTask() {
        return isDailyTask;
    }

    public void setDailyTask(boolean dailyTask) {
        isDailyTask = dailyTask;
    }

    public String getTaskId() {
        if (taskId == null || taskId.trim().equals("")) {
            taskId = UUID.randomUUID().toString();
        }
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getParentId() {
        if (parentId == null) {
            return "";
        }
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTitle() {
        if (title == null) {
            return "";
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        if (status == null || status.trim().equals("")) {
            return isFinished ? STATUS_DONE : STATUS_TODO;
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        isFinished = STATUS_DONE.equals(status);
    }

    public String getPriority() {
        if (priority == null || priority.trim().equals("")) {
            return PRIORITY_MEDIUM;
        }
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getImportance() {
        if (importance == null || importance.trim().equals("")) {
            return LEVEL_NORMAL;
        }
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getUrgency() {
        if (urgency == null || urgency.trim().equals("")) {
            return LEVEL_NORMAL;
        }
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public int getProgress() {
        if (progress < 0) {
            return 0;
        }
        if (progress > 100) {
            return 100;
        }
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getDeletedAt() {
        if (deletedAt == null) {
            return "";
        }
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<ToDoData> getTasks() {
        if (!TYPE_BOARD.equals(getDataType())) {
            return null;
        }
        if (tasks == null) {
            tasks = new ArrayList<ToDoData>();
        }
        return tasks;
    }

    public void setTasks(List<ToDoData> tasks) {
        this.tasks = tasks;
    }

    @JSONField(serialize = false)
    public boolean isDeleted() {
        return STATUS_TRASH.equals(getStatus());
    }

    @JSONField(serialize = false)
    public boolean hasTaskContent() {
        return !getTitle().trim().equals("")
                || !getContent().trim().equals("")
                || !getEndTime().trim().equals("")
                || isCurrentDay()
                || isDailyTask()
                || isFinished()
                || isDeleted();
    }

    @JSONField(serialize = false)
    public String getDisplayTitle() {
        String titleValue = getTitle().trim();
        if (!titleValue.equals("")) {
            return titleValue;
        }
        String contentValue = getContent().trim();
        if (contentValue.equals("")) {
            return "未命名任务";
        }
        int index = contentValue.indexOf('\n');
        if (index >= 0) {
            return contentValue.substring(0, index).trim();
        }
        return contentValue;
    }

    public static String getStatusText(String status) {
        if (STATUS_DOING.equals(status)) {
            return "进行中";
        }
        if (STATUS_DONE.equals(status)) {
            return "完成";
        }
        if (STATUS_TRASH.equals(status)) {
            return "回收站";
        }
        return "待办";
    }

    public static String getPriorityText(String priority) {
        if (PRIORITY_HIGH.equals(priority)) {
            return "高优先级";
        }
        if (PRIORITY_LOW.equals(priority)) {
            return "低优先级";
        }
        return "中优先级";
    }

    public static String getLevelText(String level, String normalText, String highText) {
        if (LEVEL_HIGH.equals(level)) {
            return highText;
        }
        return normalText;
    }
}
