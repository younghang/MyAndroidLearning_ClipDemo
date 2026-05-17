package com.example.yanghang.clipboard.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.ActivityEditInfo;
import com.example.yanghang.clipboard.Fragment.JsonData.ProjectData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.OthersView.DateChooseWheelViewDialog;
import com.example.yanghang.clipboard.OthersView.ProjectGanttView;
import com.example.yanghang.clipboard.R;

import java.util.Calendar;
import java.util.List;

public class FragmentProjectManagement extends FragmentEditAbstract {
    private static final String[] STATUS_LABELS = {"规划中", "进行中", "暂停", "完成", "归档"};
    private static final String[] STATUS_VALUES = {
            ProjectData.STATUS_PLANNING,
            ProjectData.STATUS_DOING,
            ProjectData.STATUS_PAUSED,
            ProjectData.STATUS_DONE,
            ProjectData.STATUS_ARCHIVED
    };
    private static final String[] PRIORITY_LABELS = {"低", "中", "高"};
    private static final String[] PRIORITY_VALUES = {
            ProjectData.PRIORITY_LOW,
            ProjectData.PRIORITY_MEDIUM,
            ProjectData.PRIORITY_HIGH
    };

    private ScrollView scrollView;
    private LinearLayout rootLayout;
    private ProjectData projectData;
    private EditText titleEdit;
    private EditText goalEdit;
    private EditText nextActionEdit;
    private Spinner statusSpinner;
    private Spinner prioritySpinner;
    private TextView startDateText;
    private TextView endDateText;
    private TextView progressText;
    private ProjectGanttView ganttView;

    public FragmentProjectManagement() {
    }

    public static FragmentProjectManagement newInstance(String information, boolean isEdit) {
        FragmentProjectManagement fragment = new FragmentProjectManagement();
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
        projectData = ProjectData.parse(infoEdit);
        scrollView = new ScrollView(getActivity());
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(getResources().getColor(R.color.gray_bg));
        rootLayout = new LinearLayout(getActivity());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dp(10), dp(10), dp(10), dp(18));
        scrollView.addView(rootLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        render();
        return scrollView;
    }

    private void render() {
        rootLayout.removeAllViews();
        renderHeader();
        renderGantt();
        renderMilestones();
        renderStages();
        renderDeliverables();
    }

    private void renderHeader() {
        LinearLayout card = makeCard();

        titleEdit = makeEditText("项目标题", false);
        titleEdit.setText(projectData.getTitle());
        goalEdit = makeEditText("项目目标", true);
        goalEdit.setText(projectData.getGoal());
        nextActionEdit = makeEditText("下一步", true);
        nextActionEdit.setText(projectData.getNextAction());

        statusSpinner = makeSpinner(STATUS_LABELS);
        statusSpinner.setSelection(indexOf(STATUS_VALUES, projectData.getStatus()));
        prioritySpinner = makeSpinner(PRIORITY_LABELS);
        prioritySpinner.setSelection(indexOf(PRIORITY_VALUES, projectData.getPriority()));

        card.addView(makeLabel("项目标题"));
        card.addView(titleEdit);
        card.addView(makeLabel("项目目标"));
        card.addView(goalEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(86)));

        LinearLayout stateRow = new LinearLayout(getActivity());
        stateRow.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(stateRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        stateRow.addView(labeledBox("状态", statusSpinner), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        stateRow.addView(labeledBox("优先级", prioritySpinner), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout dateRow = new LinearLayout(getActivity());
        dateRow.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(dateRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        startDateText = makeDateButton(projectData.getStartDate().equals("") ? todayString() : projectData.getStartDate(), "开始时间");
        endDateText = makeDateButton(projectData.getEndDate().equals("") ? todayString() : projectData.getEndDate(), "结束时间");
        dateRow.addView(labeledBox("开始", startDateText), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        dateRow.addView(labeledBox("结束", endDateText), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        card.addView(makeLabel("下一步"));
        card.addView(nextActionEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(72)));

        progressText = makeText("总进度 " + projectData.calculateProgressFromStages() + "%", 14, R.color.message_text);
        progressText.setTypeface(null, Typeface.BOLD);
        progressText.setPadding(0, dp(10), 0, dp(2));
        card.addView(progressText);
        addProgressLine(card, projectData.calculateProgressFromStages(), getResources().getColor(R.color.colorPrimary), 0);

        setHeaderEnabled(isEdit);
        rootLayout.addView(card);
    }

    private LinearLayout labeledBox(String label, View content) {
        LinearLayout box = new LinearLayout(getActivity());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(4), dp(8), dp(4));
        box.addView(makeLabel(label));
        box.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return box;
    }

    private TextView makeDateButton(String text, final String title) {
        final TextView textView = makeText(text, 16, R.color.colorPrimary);
        textView.setPadding(0, dp(10), 0, dp(10));
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setBackground(makeRoundBackground(Color.WHITE, Color.rgb(232, 232, 232), 7));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEdit) {
                    return;
                }
                DateChooseWheelViewDialog dialog = new DateChooseWheelViewDialog(getActivity(), textView.getText().toString(),
                        new DateChooseWheelViewDialog.DateChooseInterface() {
                            @Override
                            public void getDateTime(String time, boolean longTimeChecked) {
                                textView.setText(time);
                            }
                        });
                dialog.setTimePickerGone(true);
                dialog.setDateDialogTitle(title);
                dialog.showDateChooseDialog();
            }
        });
        return textView;
    }

    private void renderGantt() {
        LinearLayout card = makeCard();
        LinearLayout titleRow = new LinearLayout(getActivity());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = makeText("甘特图", 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button fullscreenButton = makeIconButton("\u25a1");
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFullscreenGantt();
            }
        });
        LinearLayout.LayoutParams fullscreenParams = new LinearLayout.LayoutParams(dp(38), dp(34));
        fullscreenParams.setMargins(0, 0, dp(6), 0);
        titleRow.addView(fullscreenButton, fullscreenParams);
        Button addButton = makeSmallButton("+ 阶段");
        addButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStageEditDialog(createNewStage(), -1);
            }
        });
        titleRow.addView(addButton, new LinearLayout.LayoutParams(dp(82), dp(34)));
        card.addView(titleRow);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setFillViewport(false);
        horizontalScrollView.setHorizontalScrollBarEnabled(true);
        ganttView = new ProjectGanttView(getActivity());
        ganttView.setProjectData(projectData);
        horizontalScrollView.addView(ganttView, new HorizontalScrollView.LayoutParams(dp(980), ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(horizontalScrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootLayout.addView(card);
    }

    private void openFullscreenGantt() {
        if (getActivity() == null) {
            return;
        }
        collectFromViews();
        final int oldOrientation = getActivity().getRequestedOrientation();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        FrameLayout contentLayout = new FrameLayout(getActivity());
        contentLayout.setBackgroundColor(Color.WHITE);

        ScrollView verticalScrollView = new ScrollView(getActivity());
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getActivity());
        horizontalScrollView.setFillViewport(false);
        horizontalScrollView.setHorizontalScrollBarEnabled(true);
        ProjectGanttView fullscreenGanttView = new ProjectGanttView(getActivity());
        fullscreenGanttView.setProjectData(projectData);
        horizontalScrollView.addView(fullscreenGanttView, new HorizontalScrollView.LayoutParams(dp(1280), ViewGroup.LayoutParams.WRAP_CONTENT));
        verticalScrollView.addView(horizontalScrollView, new ScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contentLayout.addView(verticalScrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topRow = new LinearLayout(getActivity());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        topRow.setPadding(dp(8), dp(8), dp(8), 0);
        topRow.setBackgroundColor(Color.TRANSPARENT);
        final Button closeButton = makeTransparentButton("X", 18);
        topRow.addView(closeButton, new LinearLayout.LayoutParams(dp(44), dp(40)));
        contentLayout.addView(topRow, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(contentLayout);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (getActivity() != null) {
                    getActivity().setRequestedOrientation(oldOrientation);
                }
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void renderStages() {
        LinearLayout card = makeCard();
        TextView title = makeText("阶段任务", 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        List<ProjectData.ProjectStage> stages = projectData.getStages();
        if (stages.size() == 0) {
            TextView empty = makeText("暂无阶段，点击“+ 阶段”添加计划。", 14, R.color.text_11);
            empty.setPadding(0, dp(10), 0, dp(10));
            card.addView(empty);
        }
        for (int i = 0; i < stages.size(); i++) {
            addStageCard(card, stages.get(i), i);
        }
        rootLayout.addView(card);
    }

    private void addStageCard(LinearLayout parent, final ProjectData.ProjectStage stage, final int index) {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(makeRoundBackground(Color.WHITE, statusColor(stage.getStatus()), 8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, 0);
        parent.addView(row, params);

        LinearLayout titleRow = new LinearLayout(getActivity());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(titleRow);

        TextView title = makeText(stage.getTitle(), 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView progress = makeText(stage.getProgress() + "%", 14, R.color.text_11);
        progress.setGravity(Gravity.RIGHT);
        titleRow.addView(progress, new LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView meta = makeText(ProjectData.getStatusText(stage.getStatus()) + " · " + stage.getStartDate() + " -> " + stage.getEndDate(), 13, R.color.text_11);
        meta.setPadding(0, dp(4), 0, 0);
        row.addView(meta);
        String dependencies = getStageDependencyTitles(stage);
        if (!dependencies.equals("")) {
            TextView dependencyView = makeText("前置：" + dependencies, 13, R.color.text_11);
            dependencyView.setPadding(0, dp(4), 0, 0);
            row.addView(dependencyView);
        }
        if (!stage.getNote().trim().equals("")) {
            TextView note = makeText(firstLine(stage.getNote()), 14, R.color.message_text);
            note.setPadding(0, dp(5), 0, 0);
            row.addView(note);
        }
        addProgressLine(row, stage.getProgress(), statusColor(stage.getStatus()), 0);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    showStageEditDialog(stage, index);
                } else {
                    showStageDetail(stage);
                }
            }
        });
    }

    private void renderMilestones() {
        LinearLayout card = makeCard();
        LinearLayout titleRow = new LinearLayout(getActivity());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = makeText("里程碑", 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button addButton = makeSmallButton("+ 里程碑");
        addButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMilestoneEditDialog(createNewMilestone(), -1);
            }
        });
        titleRow.addView(addButton, new LinearLayout.LayoutParams(dp(96), dp(34)));
        card.addView(titleRow);

        if (projectData.getMilestones().size() == 0) {
            TextView empty = makeText("暂无里程碑。里程碑会在甘特图上显示为时间节点。", 14, R.color.text_11);
            empty.setPadding(0, dp(8), 0, 0);
            card.addView(empty);
        }
        for (int i = 0; i < projectData.getMilestones().size(); i++) {
            addMilestoneRow(card, projectData.getMilestones().get(i), i);
        }
        rootLayout.addView(card);
    }

    private void addMilestoneRow(LinearLayout parent, final ProjectData.ProjectMilestone milestone, final int index) {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, 0);
        TextView date = makeText(milestone.getDate(), 13, R.color.colorPrimary);
        date.setTypeface(null, Typeface.BOLD);
        row.addView(date, new LinearLayout.LayoutParams(dp(96), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView title = makeText(milestone.getTitle(), 14, R.color.message_text);
        row.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    showMilestoneEditDialog(milestone, index);
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(milestone.getTitle())
                            .setMessage(milestone.getDate())
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
        parent.addView(row);
    }

    private void renderDeliverables() {
        LinearLayout card = makeCard();
        TextView title = makeText("交付物索引", 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        if (projectData.getDeliverables().size() == 0) {
            TextView empty = makeText("第一版先保存交付物索引；真实文件后续由电脑端统一文件夹管理。", 14, R.color.text_11);
            empty.setPadding(0, dp(8), 0, 0);
            card.addView(empty);
        }
        for (int i = 0; i < projectData.getDeliverables().size(); i++) {
            ProjectData.ProjectDeliverable deliverable = projectData.getDeliverables().get(i);
            TextView item = makeText("· " + deliverable.getName() + "\n  " + deliverable.getRelativePath(), 13, R.color.message_text);
            item.setPadding(0, dp(8), 0, 0);
            card.addView(item);
        }
        rootLayout.addView(card);
    }

    private ProjectData.ProjectStage createNewStage() {
        ProjectData.ProjectStage stage = ProjectData.createStage("新阶段", todayString(), oneMonthLaterString(), 0, ProjectData.STATUS_PLANNING, "");
        return stage;
    }

    private ProjectData.ProjectMilestone createNewMilestone() {
        ProjectData.ProjectMilestone milestone = new ProjectData.ProjectMilestone();
        milestone.setTitle("新里程碑");
        milestone.setDate(todayString());
        return milestone;
    }

    private void showMilestoneEditDialog(final ProjectData.ProjectMilestone editingMilestone, final int editingIndex) {
        final ProjectData.ProjectMilestone milestone = editingMilestone == null ? createNewMilestone() : editingMilestone;
        LinearLayout contentLayout = new LinearLayout(getActivity());
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(18), dp(8), dp(18), dp(4));

        final EditText titleEdit = makeEditText("里程碑标题", false);
        titleEdit.setText(milestone.getTitle());
        contentLayout.addView(makeLabel("标题"));
        contentLayout.addView(titleEdit);

        contentLayout.addView(makeLabel("日期"));
        final TextView dateText = makeDateButton(milestone.getDate().equals("") ? todayString() : milestone.getDate(), "里程碑日期");
        contentLayout.addView(dateText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(editingIndex < 0 ? "添加里程碑" : "编辑里程碑")
                .setView(contentLayout)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null);
        if (editingIndex >= 0) {
            builder.setNeutralButton("删除", null);
        }
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        milestone.setTitle(titleEdit.getText().toString());
                        milestone.setDate(dateText.getText().toString());
                        milestone.normalize();
                        if (editingIndex < 0) {
                            projectData.getMilestones().add(milestone);
                        } else {
                            projectData.getMilestones().set(editingIndex, milestone);
                        }
                        autoSaveParent();
                        render();
                        dialog.dismiss();
                    }
                });
                if (editingIndex >= 0) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            projectData.getMilestones().remove(editingIndex);
                            autoSaveParent();
                            render();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    private String[] buildStageDependencyIds(ProjectData.ProjectStage editingStage) {
        List<ProjectData.ProjectStage> stages = projectData.getStages();
        int count = 0;
        String editingId = editingStage == null ? "" : editingStage.getId();
        for (int i = 0; i < stages.size(); i++) {
            ProjectData.ProjectStage stage = stages.get(i);
            if (stage != null && !stage.getId().equals(editingId)) {
                count++;
            }
        }
        String[] ids = new String[count];
        int index = 0;
        for (int i = 0; i < stages.size(); i++) {
            ProjectData.ProjectStage stage = stages.get(i);
            if (stage != null && !stage.getId().equals(editingId)) {
                ids[index] = stage.getId();
                index++;
            }
        }
        return ids;
    }

    private String[] buildStageDependencyLabels(String[] dependencyIds) {
        String[] labels = new String[dependencyIds.length];
        for (int i = 0; i < dependencyIds.length; i++) {
            ProjectData.ProjectStage stage = findStageById(dependencyIds[i]);
            labels[i] = stage == null ? "未知阶段" : stage.getTitle();
        }
        return labels;
    }

    private boolean[] buildStageDependencyChecked(ProjectData.ProjectStage stage, String[] dependencyIds) {
        boolean[] checked = new boolean[dependencyIds.length];
        if (stage == null) {
            return checked;
        }
        for (int i = 0; i < dependencyIds.length; i++) {
            checked[i] = stage.getDependsOn().contains(dependencyIds[i]);
        }
        return checked;
    }

    private void updateDependencyText(TextView view, String[] dependencyIds, boolean[] checked) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dependencyIds.length; i++) {
            if (!checked[i]) {
                continue;
            }
            ProjectData.ProjectStage stage = findStageById(dependencyIds[i]);
            if (stage == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("，");
            }
            builder.append(stage.getTitle());
        }
        view.setText(builder.length() == 0 ? "无前置阶段" : builder.toString());
    }

    private String getStageDependencyTitles(ProjectData.ProjectStage stage) {
        if (stage == null || stage.getDependsOn().size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stage.getDependsOn().size(); i++) {
            ProjectData.ProjectStage parent = findStageById(stage.getDependsOn().get(i));
            if (parent == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("，");
            }
            builder.append(parent.getTitle());
        }
        return builder.toString();
    }

    private ProjectData.ProjectStage findStageById(String id) {
        if (id == null || id.trim().equals("")) {
            return null;
        }
        List<ProjectData.ProjectStage> stages = projectData.getStages();
        for (int i = 0; i < stages.size(); i++) {
            ProjectData.ProjectStage stage = stages.get(i);
            if (stage != null && id.equals(stage.getId())) {
                return stage;
            }
        }
        return null;
    }

    private void removeStageReferences(String stageId) {
        if (stageId == null || stageId.trim().equals("")) {
            return;
        }
        List<ProjectData.ProjectStage> stages = projectData.getStages();
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).getDependsOn().remove(stageId);
        }
        for (int i = 0; i < projectData.getDeliverables().size(); i++) {
            ProjectData.ProjectDeliverable deliverable = projectData.getDeliverables().get(i);
            if (stageId.equals(deliverable.getStageId())) {
                deliverable.setStageId("");
            }
        }
    }

    private void showStageEditDialog(final ProjectData.ProjectStage editingStage, final int editingIndex) {
        final ProjectData.ProjectStage stage = editingStage == null ? createNewStage() : editingStage;
        LinearLayout contentLayout = new LinearLayout(getActivity());
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(18), dp(8), dp(18), dp(4));

        final EditText titleEdit = makeEditText("阶段标题", false);
        titleEdit.setText(stage.getTitle());
        contentLayout.addView(makeLabel("标题"));
        contentLayout.addView(titleEdit);

        final Spinner statusSpinner = makeSpinner(STATUS_LABELS);
        statusSpinner.setSelection(indexOf(STATUS_VALUES, stage.getStatus()));
        contentLayout.addView(makeLabel("状态"));
        contentLayout.addView(statusSpinner);

        final String[] dependencyIds = buildStageDependencyIds(stage);
        final String[] dependencyLabels = buildStageDependencyLabels(dependencyIds);
        final boolean[] dependencyChecked = buildStageDependencyChecked(stage, dependencyIds);
        final TextView dependencyText = makeText("", 13, R.color.text_11);
        updateDependencyText(dependencyText, dependencyIds, dependencyChecked);
        Button dependencyButton = makeSmallButton("选择前置阶段");
        dependencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dependencyIds.length == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("前置阶段")
                            .setMessage("还没有其它阶段可以选择。")
                            .setPositiveButton("确定", null)
                            .show();
                    return;
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("选择前置阶段")
                        .setMultiChoiceItems(dependencyLabels, dependencyChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                dependencyChecked[which] = isChecked;
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateDependencyText(dependencyText, dependencyIds, dependencyChecked);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        contentLayout.addView(makeLabel("前置阶段"));
        contentLayout.addView(dependencyButton, new LinearLayout.LayoutParams(dp(128), dp(36)));
        contentLayout.addView(dependencyText);

        contentLayout.addView(makeLabel("开始日期"));
        final TextView startText = makeDateButton(stage.getStartDate().equals("") ? todayString() : stage.getStartDate(), "开始日期");
        contentLayout.addView(startText);

        contentLayout.addView(makeLabel("结束日期"));
        final TextView endText = makeDateButton(stage.getEndDate().equals("") ? oneMonthLaterString() : stage.getEndDate(), "结束日期");
        contentLayout.addView(endText);

        contentLayout.addView(makeLabel("进度"));
        LinearLayout progressLayout = new LinearLayout(getActivity());
        progressLayout.setOrientation(LinearLayout.HORIZONTAL);
        progressLayout.setGravity(Gravity.CENTER_VERTICAL);
        final SeekBar progressSeekBar = new SeekBar(getActivity());
        progressSeekBar.setMax(100);
        progressSeekBar.setProgress(stage.getProgress());
        final TextView progressView = makeText(stage.getProgress() + "%", 14, R.color.text_11);
        progressView.setGravity(Gravity.RIGHT);
        progressLayout.addView(progressSeekBar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        progressLayout.addView(progressView, new LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.WRAP_CONTENT));
        contentLayout.addView(progressLayout);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressView.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final EditText noteEdit = makeEditText("阶段备注", true);
        noteEdit.setText(stage.getNote());
        contentLayout.addView(makeLabel("备注"));
        contentLayout.addView(noteEdit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(96)));

        ScrollView dialogScroll = new ScrollView(getActivity());
        dialogScroll.addView(contentLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(editingIndex < 0 ? "添加阶段" : "编辑阶段")
                .setView(dialogScroll)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null);
        if (editingIndex >= 0) {
            builder.setNeutralButton("删除", null);
        }
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stage.setTitle(titleEdit.getText().toString());
                        stage.setStatus(STATUS_VALUES[statusSpinner.getSelectedItemPosition()]);
                        stage.setStartDate(startText.getText().toString());
                        stage.setEndDate(endText.getText().toString());
                        stage.setProgress(progressSeekBar.getProgress());
                        stage.setNote(noteEdit.getText().toString());
                        stage.getDependsOn().clear();
                        for (int i = 0; i < dependencyIds.length; i++) {
                            if (dependencyChecked[i]) {
                                stage.getDependsOn().add(dependencyIds[i]);
                            }
                        }
                        stage.normalize();
                        if (editingIndex < 0) {
                            projectData.getStages().add(stage);
                        } else {
                            projectData.getStages().set(editingIndex, stage);
                        }
                        projectData.setProgress(projectData.calculateProgressFromStages());
                        autoSaveParent();
                        render();
                        dialog.dismiss();
                    }
                });
                if (editingIndex >= 0) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeStageReferences(projectData.getStages().get(editingIndex).getId());
                            projectData.getStages().remove(editingIndex);
                            projectData.setProgress(projectData.calculateProgressFromStages());
                            autoSaveParent();
                            render();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    private void showStageDetail(ProjectData.ProjectStage stage) {
        String dependencies = getStageDependencyTitles(stage);
        String dependencyLine = dependencies.equals("") ? "" : "\n前置：" + dependencies;
        new AlertDialog.Builder(getActivity())
                .setTitle(stage.getTitle())
                .setMessage(ProjectData.getStatusText(stage.getStatus()) + " · " + stage.getProgress() + "%\n"
                        + stage.getStartDate() + " -> " + stage.getEndDate() + dependencyLine + "\n\n" + stage.getNote())
                .setPositiveButton("确定", null)
                .show();
    }

    private void collectFromViews() {
        if (titleEdit == null) {
            return;
        }
        projectData.setTitle(titleEdit.getText().toString());
        projectData.setGoal(goalEdit.getText().toString());
        projectData.setNextAction(nextActionEdit.getText().toString());
        projectData.setStatus(STATUS_VALUES[statusSpinner.getSelectedItemPosition()]);
        projectData.setPriority(PRIORITY_VALUES[prioritySpinner.getSelectedItemPosition()]);
        projectData.setStartDate(startDateText.getText().toString());
        projectData.setEndDate(endDateText.getText().toString());
        projectData.setProgress(projectData.calculateProgressFromStages());
    }

    private void autoSaveParent() {
        if (getActivity() instanceof ActivityEditInfo) {
            ((ActivityEditInfo) getActivity()).autoSaveCurrentContent();
        }
    }

    private void setHeaderEnabled(boolean enabled) {
        titleEdit.setEnabled(enabled);
        goalEdit.setEnabled(enabled);
        nextActionEdit.setEnabled(enabled);
        statusSpinner.setEnabled(enabled);
        prioritySpinner.setEnabled(enabled);
        startDateText.setEnabled(enabled);
        endDateText.setEnabled(enabled);
    }

    private void addProgressLine(LinearLayout row, int progress, int color, int marginLeft) {
        LinearLayout track = new LinearLayout(getActivity());
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setBackground(makeRoundBackground(Color.rgb(238, 241, 247), Color.rgb(238, 241, 247), 3));
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(5));
        trackParams.setMargins(dp(marginLeft), dp(8), 0, 0);
        row.addView(track, trackParams);

        View progressView = new View(getActivity());
        progressView.setBackground(makeRoundBackground(color, color, 3));
        int progressWidth = Math.max(1, progress);
        track.addView(progressView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, progressWidth));
        if (progressWidth < 100) {
            View spacer = new View(getActivity());
            track.addView(spacer, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 100 - progressWidth));
        }
    }

    private int statusColor(String status) {
        if (ProjectData.STATUS_DOING.equals(status)) {
            return Color.rgb(217, 150, 16);
        }
        if (ProjectData.STATUS_DONE.equals(status)) {
            return Color.rgb(9, 183, 99);
        }
        if (ProjectData.STATUS_PAUSED.equals(status)) {
            return Color.rgb(153, 153, 153);
        }
        if (ProjectData.STATUS_ARCHIVED.equals(status)) {
            return Color.rgb(100, 106, 120);
        }
        return Color.rgb(63, 81, 181);
    }

    private LinearLayout makeCard() {
        LinearLayout card = new LinearLayout(getActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(makeRoundBackground(Color.WHITE, Color.rgb(232, 232, 232), 8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);
        return card;
    }

    private Button makeSmallButton(String text) {
        Button button = new Button(getActivity());
        button.setText(text);
        button.setTextSize(13);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(6), 0, dp(6), 0);
        button.setBackground(makeRoundBackground(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimary), 8));
        return button;
    }

    private Button makeIconButton(String text) {
        Button button = makeSmallButton(text);
        button.setTextSize(16);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private Button makeTransparentButton(String text, int sp) {
        Button button = new Button(getActivity());
        button.setText(text);
        button.setTextSize(sp);
        button.setTextColor(getResources().getColor(R.color.message_text));
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(0, 0, 0, 0);
        button.setBackground(makeRoundBackground(Color.argb(180, 255, 255, 255), Color.argb(80, 120, 126, 140), 20));
        return button;
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
        label.setPadding(0, dp(8), 0, 0);
        return label;
    }

    private TextView makeText(String text, int sp, int colorId) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(sp);
        textView.setTextColor(getResources().getColor(colorId));
        return textView;
    }

    private int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private String todayString() {
        return DateFormat.format("yyyy-MM-dd", Calendar.getInstance().getTime()).toString();
    }

    private String oneMonthLaterString() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        return DateFormat.format("yyyy-MM-dd", calendar.getTime()).toString();
    }

    private String firstLine(String value) {
        String content = value == null ? "" : value.trim();
        int index = content.indexOf('\n');
        if (index >= 0) {
            return content.substring(0, index).trim();
        }
        return content;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    private GradientDrawable makeRoundBackground(int fillColor, int strokeColor, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void redo() {
    }

    @Override
    public void undo() {
    }

    @Override
    public String getString() {
        collectFromViews();
        projectData.setDataType(ProjectData.DATA_TYPE);
        projectData.setUpdatedAt(ListData.GetDate().replace("\n", " "));
        projectData.setProgress(projectData.calculateProgressFromStages());
        projectData.getId();
        return JSON.toJSONString(projectData);
    }

    @Override
    public void enableEdit() {
        isEdit = true;
        render();
    }

    public void disableEdit() {
        isEdit = false;
        render();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }
}
