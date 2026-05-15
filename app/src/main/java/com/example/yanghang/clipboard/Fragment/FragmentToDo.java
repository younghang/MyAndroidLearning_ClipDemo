package com.example.yanghang.clipboard.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.OthersView.DateChooseWheelViewDialog;
import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FragmentToDo extends FragmentEditAbstract {
    private static final int TASK_FILTER_ACTIVE = 0;
    private static final int TASK_FILTER_DONE = 1;
    private static final int TASK_FILTER_TRASH = 2;

    private static final String[] STATUS_LABELS = {"待办", "进行中", "完成", "回收站"};
    private static final String[] STATUS_VALUES = {ToDoData.STATUS_TODO, ToDoData.STATUS_DOING, ToDoData.STATUS_DONE, ToDoData.STATUS_TRASH};
    private static final String[] PRIORITY_LABELS = {"低", "中", "高"};
    private static final String[] PRIORITY_VALUES = {ToDoData.PRIORITY_LOW, ToDoData.PRIORITY_MEDIUM, ToDoData.PRIORITY_HIGH};
    private static final String[] LEVEL_LABELS = {"普通", "重要"};
    private static final String[] LEVEL_VALUES = {ToDoData.LEVEL_NORMAL, ToDoData.LEVEL_HIGH};
    private static final String[] URGENCY_LABELS = {"普通", "紧急"};

    private View mView;
    private ToDoData boardData;
    private TextView summaryTextView;
    private Button addTaskButton;
    private EditText boardNoteEdit;
    private LinearLayout taskContainer;

    public FragmentToDo() {
        // Required empty public constructor
    }

    public static FragmentToDo newInstance(String information, boolean isEdit) {
        FragmentToDo fragment = new FragmentToDo();
        newInstance(fragment, information, isEdit);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onICreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_to_do, null);
        initialView();
        return mView;
    }

    private void initialView() {
        boardData = parseBoard(infoEdit);
        if (isEdit && MainFormActivity.isDailyTask) {
            boardData.setDailyTask(true);
            MainFormActivity.isDailyTask = false;
        }

        summaryTextView = (TextView) mView.findViewById(R.id.fragment_todo_summary_tv);
        addTaskButton = (Button) mView.findViewById(R.id.fragment_todo_add_task_btn);
        boardNoteEdit = (EditText) mView.findViewById(R.id.fragment_todo_board_note_edit);
        taskContainer = (LinearLayout) mView.findViewById(R.id.fragment_todo_task_container);

        boardNoteEdit.setText(boardData.getContent());
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTaskEditDialog(createNewTask(), -1);
            }
        });
        setControlsEnabled(isEdit);
        renderTasks();
    }

    private ToDoData parseBoard(String information) {
        if (information == null || information.trim().equals("")) {
            return ToDoData.createBoard();
        }
        ToDoData data = null;
        try {
            data = JSON.parseObject(information, ToDoData.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (data == null) {
            return ToDoData.createBoardFromPlainText(information, todayString(), false, false);
        }
        return ToDoData.normalizeBoard(data);
    }

    private ToDoData createNewTask() {
        ToDoData task = ToDoData.createTask();
        task.setEndTime(todayString());
        task.setDailyTask(boardData.isDailyTask());
        return task;
    }

    private String todayString() {
        return DateFormat.format("yyyy-MM-dd", Calendar.getInstance().getTime()).toString();
    }

    private void renderTasks() {
        taskContainer.removeAllViews();
        updateSummary();

        List<ToDoData> tasks = boardData.getTasks();
        if (tasks.size() == 0) {
            TextView emptyView = makeText("这个阶段还没有任务。点右上角“+ 任务”开始添加。", 15, R.color.text_11);
            emptyView.setGravity(Gravity.CENTER);
            taskContainer.addView(emptyView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(120)));
            return;
        }

        addSection("未完成");
        boolean hasActive = addTaskGroup(tasks, TASK_FILTER_ACTIVE);
        if (!hasActive) {
            addHint("没有未完成任务");
        }

        addSection("已完成");
        boolean hasDone = addTaskGroup(tasks, TASK_FILTER_DONE);
        if (!hasDone) {
            addHint("没有已完成任务");
        }

        addSection("回收站");
        boolean hasTrash = addTaskGroup(tasks, TASK_FILTER_TRASH);
        if (!hasTrash) {
            addHint("回收站为空");
        }
    }

    private void updateSummary() {
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
        String dailyText = boardData.isDailyTask() ? "  日常阶段" : "";
        summaryTextView.setText("任务 " + total + "  完成 " + done + "  进行中 " + doing
                + "  进度 " + progress + "%" + dailyText + "\n高优先级 " + high + "  紧急 " + urgent + "  回收站 " + trash);
    }

    private boolean addTaskGroup(List<ToDoData> tasks, int filter) {
        boolean[] rendered = new boolean[tasks.size()];
        List<Integer> sortedIndexes = getSortedTaskIndexes(tasks, filter);
        boolean hasTask = false;
        hasTask = addTaskTree(tasks, sortedIndexes, "", 0, rendered) || hasTask;
        for (int j = 0; j < sortedIndexes.size(); j++) {
            int i = sortedIndexes.get(j);
            if (!rendered[i] && matchesFilter(tasks.get(i), filter)) {
                addTaskRow(tasks.get(i), i, 0);
                rendered[i] = true;
                hasTask = true;
            }
        }
        return hasTask;
    }

    private boolean addTaskTree(List<ToDoData> tasks, List<Integer> sortedIndexes, String parentId, int depth, boolean[] rendered) {
        boolean hasTask = false;
        for (int j = 0; j < sortedIndexes.size(); j++) {
            int i = sortedIndexes.get(j);
            ToDoData task = tasks.get(i);
            if (task == null || rendered[i]) {
                continue;
            }
            if (!sameParent(task.getParentId(), parentId)) {
                continue;
            }
            addTaskRow(task, i, depth);
            rendered[i] = true;
            hasTask = true;
            hasTask = addTaskTree(tasks, sortedIndexes, task.getTaskId(), depth + 1, rendered) || hasTask;
        }
        return hasTask;
    }

    private List<Integer> getSortedTaskIndexes(final List<ToDoData> tasks, int filter) {
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < tasks.size(); i++) {
            if (matchesFilter(tasks.get(i), filter)) {
                indexes.add(i);
            }
        }
        Collections.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer leftIndex, Integer rightIndex) {
                return compareTask(tasks.get(leftIndex), tasks.get(rightIndex));
            }
        });
        return indexes;
    }

    private int compareTask(ToDoData left, ToDoData right) {
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
        result = statusRank(right) - statusRank(left);
        if (result != 0) {
            return result;
        }
        result = compareDate(left.getEndTime(), right.getEndTime());
        if (result != 0) {
            return result;
        }
        return left.getDisplayTitle().compareTo(right.getDisplayTitle());
    }

    private int compareDate(String leftDate, String rightDate) {
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

    private int urgencyRank(ToDoData task) {
        return ToDoData.LEVEL_HIGH.equals(task.getUrgency()) ? 1 : 0;
    }

    private int importanceRank(ToDoData task) {
        return ToDoData.LEVEL_HIGH.equals(task.getImportance()) ? 1 : 0;
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

    private int priorityRank(ToDoData task) {
        if (ToDoData.PRIORITY_HIGH.equals(task.getPriority())) {
            return 3;
        }
        if (ToDoData.PRIORITY_LOW.equals(task.getPriority())) {
            return 1;
        }
        return 2;
    }

    private int statusRank(ToDoData task) {
        if (ToDoData.STATUS_DOING.equals(task.getStatus())) {
            return 2;
        }
        if (ToDoData.STATUS_TODO.equals(task.getStatus())) {
            return 1;
        }
        return 0;
    }

    private boolean sameParent(String parentId, String expectedParentId) {
        String parent = parentId == null ? "" : parentId.trim();
        String expected = expectedParentId == null ? "" : expectedParentId.trim();
        return parent.equals(expected);
    }

    private boolean matchesFilter(ToDoData task, int filter) {
        if (task == null) {
            return false;
        }
        if (filter == TASK_FILTER_TRASH) {
            return task.isDeleted();
        }
        if (filter == TASK_FILTER_DONE) {
            return !task.isDeleted() && task.isFinished();
        }
        return !task.isDeleted() && !task.isFinished();
    }

    private void addTaskRow(final ToDoData task, final int index, int depth) {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        row.setBackgroundResource(R.drawable.corner_background);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(dp(4 + depth * 18), dp(4), dp(4), dp(4));
        taskContainer.addView(row, rowParams);

        LinearLayout titleRow = new LinearLayout(getActivity());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(titleRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView statusView = makeText(statusMark(task), 18, task.isDeleted() ? R.color.text_11 : R.color.colorPrimary);
        titleRow.addView(statusView, new LinearLayout.LayoutParams(dp(28), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView colorView = makeText("●", 16, R.color.text_11);
        colorView.setTextColor(markerColor(task));
        titleRow.addView(colorView, new LinearLayout.LayoutParams(dp(22), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView titleView = makeText(task.getDisplayTitle(), 16, R.color.message_text);
        titleView.setTypeface(null, Typeface.BOLD);
        if (task.isFinished()) {
            titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        titleRow.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView progressView = makeText(task.getProgress() + "%", 14, R.color.text_11);
        progressView.setGravity(Gravity.RIGHT);
        titleRow.addView(progressView, new LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tagView = makeText(buildTaskTags(task), 13, R.color.text_11);
        LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagParams.setMargins(dp(50), dp(3), 0, 0);
        row.addView(tagView, tagParams);

        if (!task.getContent().trim().equals("")) {
            TextView noteView = makeText(firstLine(task.getContent()), 14, R.color.message_text);
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            noteParams.setMargins(dp(50), dp(4), 0, 0);
            row.addView(noteView, noteParams);
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    showTaskEditDialog(task, index);
                } else {
                    showTaskDetailDialog(task);
                }
            }
        });
        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isEdit) {
                    return true;
                }
                if (task.isDeleted()) {
                    task.setStatus(ToDoData.STATUS_TODO);
                    task.setDeletedAt("");
                } else {
                    task.setStatus(ToDoData.STATUS_TRASH);
                    task.setDeletedAt(ListData.GetDate());
                }
                renderTasks();
                return true;
            }
        });
    }

    private String statusMark(ToDoData task) {
        if (task.isDeleted()) {
            return "×";
        }
        if (task.isFinished()) {
            return "✓";
        }
        if (ToDoData.STATUS_DOING.equals(task.getStatus())) {
            return "…";
        }
        return "○";
    }

    private int markerColor(ToDoData task) {
        int rank = markerRank(task);
        if (rank >= 4) {
            return Color.rgb(213, 64, 60);
        }
        if (rank == 3) {
            return Color.rgb(217, 150, 16);
        }
        if (rank == 2) {
            return Color.rgb(63, 81, 181);
        }
        return Color.rgb(153, 153, 153);
    }

    private String markerText(ToDoData task) {
        int rank = markerRank(task);
        if (rank >= 4) {
            return "红色";
        }
        if (rank == 3) {
            return "橙色";
        }
        if (rank == 2) {
            return "蓝色";
        }
        return "灰色";
    }

    private String buildTaskTags(ToDoData task) {
        String tags = ToDoData.getStatusText(task.getStatus())
                + " · " + ToDoData.getPriorityText(task.getPriority())
                + " · " + ToDoData.getLevelText(task.getImportance(), "普通", "重要")
                + "/" + ToDoData.getLevelText(task.getUrgency(), "普通", "紧急")
                + " · " + markerText(task);
        if (!task.getEndTime().trim().equals("")) {
            tags += " · 截止 " + task.getEndTime();
        }
        if (task.isCurrentDay()) {
            tags += " · 今天";
        }
        if (task.isDailyTask()) {
            tags += " · 日常";
        }
        return tags;
    }

    private void showTaskEditDialog(final ToDoData editingTask, final int editingIndex) {
        final ToDoData task = editingTask == null ? createNewTask() : editingTask;
        final LinearLayout contentLayout = new LinearLayout(getActivity());
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(18), dp(8), dp(18), dp(4));

        final EditText titleEdit = makeEditText("任务标题", false);
        titleEdit.setText(task.getTitle());
        contentLayout.addView(makeLabel("标题"));
        contentLayout.addView(titleEdit);

        final Spinner statusSpinner = makeSpinner(STATUS_LABELS);
        statusSpinner.setSelection(indexOf(STATUS_VALUES, task.getStatus()));
        contentLayout.addView(makeLabel("状态"));
        contentLayout.addView(statusSpinner);

        final Spinner prioritySpinner = makeSpinner(PRIORITY_LABELS);
        prioritySpinner.setSelection(indexOf(PRIORITY_VALUES, task.getPriority()));
        contentLayout.addView(makeLabel("优先级"));
        contentLayout.addView(prioritySpinner);

        final Spinner importanceSpinner = makeSpinner(LEVEL_LABELS);
        importanceSpinner.setSelection(indexOf(LEVEL_VALUES, task.getImportance()));
        contentLayout.addView(makeLabel("重要性"));
        contentLayout.addView(importanceSpinner);

        final Spinner urgencySpinner = makeSpinner(URGENCY_LABELS);
        urgencySpinner.setSelection(indexOf(LEVEL_VALUES, task.getUrgency()));
        contentLayout.addView(makeLabel("紧急性"));
        contentLayout.addView(urgencySpinner);

        final String[] parentValues = buildParentValues(editingIndex);
        final Spinner parentSpinner = makeSpinner(buildParentLabels(editingIndex));
        parentSpinner.setSelection(indexOf(parentValues, task.getParentId()));
        contentLayout.addView(makeLabel("上级任务"));
        contentLayout.addView(parentSpinner);

        contentLayout.addView(makeLabel("截止日期"));
        final TextView endDateTextView = makeText(task.getEndTime().trim().equals("") ? todayString() : task.getEndTime(), 16, R.color.colorPrimary);
        endDateTextView.setGravity(Gravity.CENTER_VERTICAL);
        endDateTextView.setPadding(0, dp(10), 0, dp(10));
        contentLayout.addView(endDateTextView);
        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateChooseWheelViewDialog endDateChooseDialog = new DateChooseWheelViewDialog(getActivity(), endDateTextView.getText().toString(),
                        new DateChooseWheelViewDialog.DateChooseInterface() {
                            @Override
                            public void getDateTime(String time, boolean longTimeChecked) {
                                endDateTextView.setText(time);
                            }
                        });
                endDateChooseDialog.setTimePickerGone(true);
                endDateChooseDialog.setDateDialogTitle("结束时间");
                endDateChooseDialog.showDateChooseDialog();
            }
        });

        final Switch currentDaySwitch = new Switch(getActivity());
        currentDaySwitch.setText("当天显示");
        currentDaySwitch.setChecked(task.isCurrentDay());
        contentLayout.addView(currentDaySwitch);

        final Switch dailyTaskSwitch = new Switch(getActivity());
        dailyTaskSwitch.setText("日常任务");
        dailyTaskSwitch.setChecked(task.isDailyTask() || boardData.isDailyTask());
        contentLayout.addView(dailyTaskSwitch);

        contentLayout.addView(makeLabel("进度"));
        LinearLayout progressLayout = new LinearLayout(getActivity());
        progressLayout.setOrientation(LinearLayout.HORIZONTAL);
        progressLayout.setGravity(Gravity.CENTER_VERTICAL);
        final SeekBar progressSeekBar = new SeekBar(getActivity());
        progressSeekBar.setMax(100);
        progressSeekBar.setProgress(task.getProgress());
        final TextView progressTextView = makeText(task.getProgress() + "%", 14, R.color.text_11);
        progressTextView.setGravity(Gravity.RIGHT);
        progressLayout.addView(progressSeekBar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        progressLayout.addView(progressTextView, new LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.WRAP_CONTENT));
        contentLayout.addView(progressLayout);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressTextView.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final EditText noteEdit = makeEditText("任务备注", true);
        noteEdit.setText(task.getContent());
        contentLayout.addView(makeLabel("备注"));
        contentLayout.addView(noteEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(96)));

        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(contentLayout);

        String neutralText = editingIndex < 0 ? null : (task.isDeleted() ? "恢复" : "移入回收站");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(editingIndex < 0 ? "添加任务" : "编辑任务")
                .setView(scrollView)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null);
        if (neutralText != null) {
            builder.setNeutralButton(neutralText, null);
        }
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveTaskFromDialog(task, editingIndex, titleEdit, statusSpinner, prioritySpinner,
                                importanceSpinner, urgencySpinner, parentValues, parentSpinner, endDateTextView,
                                currentDaySwitch, dailyTaskSwitch, progressSeekBar, noteEdit);
                        dialog.dismiss();
                    }
                });
                if (editingIndex >= 0) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (task.isDeleted()) {
                                task.setStatus(ToDoData.STATUS_TODO);
                                task.setDeletedAt("");
                            } else {
                                task.setStatus(ToDoData.STATUS_TRASH);
                                task.setDeletedAt(ListData.GetDate());
                            }
                            renderTasks();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    private void saveTaskFromDialog(ToDoData task, int editingIndex, EditText titleEdit, Spinner statusSpinner,
                                    Spinner prioritySpinner, Spinner importanceSpinner, Spinner urgencySpinner,
                                    String[] parentValues, Spinner parentSpinner, TextView endDateTextView,
                                    Switch currentDaySwitch, Switch dailyTaskSwitch, SeekBar progressSeekBar,
                                    EditText noteEdit) {
        String status = STATUS_VALUES[statusSpinner.getSelectedItemPosition()];
        task.setDataType(ToDoData.TYPE_TASK);
        task.setTitle(titleEdit.getText().toString());
        task.setStatus(status);
        task.setFinished(ToDoData.STATUS_DONE.equals(status));
        task.setPriority(PRIORITY_VALUES[prioritySpinner.getSelectedItemPosition()]);
        task.setImportance(LEVEL_VALUES[importanceSpinner.getSelectedItemPosition()]);
        task.setUrgency(LEVEL_VALUES[urgencySpinner.getSelectedItemPosition()]);
        task.setParentId(parentValues[parentSpinner.getSelectedItemPosition()]);
        task.setEndTime(endDateTextView.getText().toString());
        task.setCurrentDay(currentDaySwitch.isChecked());
        task.setDailyTask(dailyTaskSwitch.isChecked());
        task.setProgress(progressSeekBar.getProgress());
        task.setContent(noteEdit.getText().toString());
        if (ToDoData.STATUS_TRASH.equals(status) && task.getDeletedAt().trim().equals("")) {
            task.setDeletedAt(ListData.GetDate());
        } else if (!ToDoData.STATUS_TRASH.equals(status)) {
            task.setDeletedAt("");
        }
        task.getTaskId();

        if (editingIndex < 0) {
            boardData.getTasks().add(task);
        } else {
            boardData.getTasks().set(editingIndex, task);
        }
        renderTasks();
    }

    private void showTaskDetailDialog(ToDoData task) {
        new AlertDialog.Builder(getActivity())
                .setTitle(task.getDisplayTitle())
                .setMessage(buildTaskTags(task) + "\n\n" + task.getContent())
                .setPositiveButton("确定", null)
                .show();
    }

    private String[] buildParentLabels(int editingIndex) {
        List<ToDoData> tasks = boardData.getTasks();
        String[] labels = new String[getAvailableParentCount(editingIndex) + 1];
        labels[0] = "无上级";
        int index = 1;
        for (int i = 0; i < tasks.size(); i++) {
            ToDoData task = tasks.get(i);
            if (!canUseAsParent(task, i, editingIndex)) {
                continue;
            }
            labels[index] = task.getDisplayTitle();
            index++;
        }
        return labels;
    }

    private String[] buildParentValues(int editingIndex) {
        List<ToDoData> tasks = boardData.getTasks();
        String[] values = new String[getAvailableParentCount(editingIndex) + 1];
        values[0] = "";
        int index = 1;
        for (int i = 0; i < tasks.size(); i++) {
            ToDoData task = tasks.get(i);
            if (!canUseAsParent(task, i, editingIndex)) {
                continue;
            }
            values[index] = task.getTaskId();
            index++;
        }
        return values;
    }

    private int getAvailableParentCount(int editingIndex) {
        int count = 0;
        List<ToDoData> tasks = boardData.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            if (canUseAsParent(tasks.get(i), i, editingIndex)) {
                count++;
            }
        }
        return count;
    }

    private boolean canUseAsParent(ToDoData task, int taskIndex, int editingIndex) {
        if (task == null || task.isDeleted() || taskIndex == editingIndex) {
            return false;
        }
        if (editingIndex < 0) {
            return true;
        }
        ToDoData editingTask = boardData.getTasks().get(editingIndex);
        return !isDescendantOf(task, editingTask.getTaskId());
    }

    private boolean isDescendantOf(ToDoData task, String possibleParentId) {
        String parentId = task.getParentId();
        int guard = 0;
        while (parentId != null && !parentId.trim().equals("") && guard < boardData.getTasks().size()) {
            if (parentId.equals(possibleParentId)) {
                return true;
            }
            ToDoData parent = findTaskById(parentId);
            if (parent == null) {
                return false;
            }
            parentId = parent.getParentId();
            guard++;
        }
        return false;
    }

    private ToDoData findTaskById(String taskId) {
        if (taskId == null || taskId.trim().equals("")) {
            return null;
        }
        List<ToDoData> tasks = boardData.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            ToDoData task = tasks.get(i);
            if (task != null && taskId.equals(task.getTaskId())) {
                return task;
            }
        }
        return null;
    }

    private Spinner makeSpinner(String[] labels) {
        Spinner spinner = new Spinner(getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private EditText makeEditText(String hint, boolean multiLine) {
        EditText editText = new EditText(getActivity());
        editText.setHint(hint);
        editText.setTextColor(getResources().getColor(R.color.message_text));
        editText.setTextSize(15);
        editText.setSingleLine(!multiLine);
        if (multiLine) {
            editText.setGravity(Gravity.TOP);
        }
        return editText;
    }

    private TextView makeLabel(String text) {
        TextView label = makeText(text, 13, R.color.text_11);
        label.setPadding(0, dp(10), 0, 0);
        return label;
    }

    private TextView makeText(String text, int sp, int colorId) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(sp);
        textView.setTextColor(getResources().getColor(colorId));
        return textView;
    }

    private void addSection(String title) {
        TextView section = makeText(title, 14, R.color.text_11);
        section.setTypeface(null, Typeface.BOLD);
        section.setPadding(dp(4), dp(12), 0, dp(3));
        taskContainer.addView(section, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void addHint(String hint) {
        TextView textView = makeText(hint, 13, R.color.text_11);
        textView.setPadding(dp(18), dp(4), 0, dp(6));
        taskContainer.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private String firstLine(String value) {
        String content = value == null ? "" : value.trim();
        int index = content.indexOf('\n');
        if (index >= 0) {
            return content.substring(0, index).trim();
        }
        return content;
    }

    private void setControlsEnabled(boolean enabled) {
        boardNoteEdit.setEnabled(enabled);
        addTaskButton.setEnabled(enabled);
        addTaskButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void redo() {
    }

    @Override
    public void undo() {
    }

    @Override
    public String getString() {
        boardData.setDataType(ToDoData.TYPE_BOARD);
        boardData.setContent(boardNoteEdit.getText().toString());
        boardData.getTaskId();
        boardData.getTasks();
        return JSON.toJSONString(boardData);
    }

    @Override
    public void enableEdit() {
        isEdit = true;
        setControlsEnabled(true);
        renderTasks();
    }
}
