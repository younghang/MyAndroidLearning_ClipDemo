package com.example.yanghang.clipboard.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.ActivityEditInfo;
import com.example.yanghang.clipboard.Fragment.JsonData.ResearchTopicData;
import com.example.yanghang.clipboard.OthersView.ResearchMindMapView;
import com.example.yanghang.clipboard.R;

import java.util.List;

public class FragmentResearchTopic extends FragmentEditAbstract {
    public static final int MENU_RESEARCH_VIEW_MODE = 0x7101;
    public static final int MENU_RESEARCH_ADD_NODE = 0x7102;
    public static final int MENU_RESEARCH_FULLSCREEN = 0x7103;
    public static final int MENU_RESEARCH_RESET_LAYOUT = 0x7104;
    private static final int VIEW_MODE_CARD = 0;
    private static final int VIEW_MODE_MAP = 1;
    private static final String[] TYPE_LABELS = {"问题", "概念", "资料", "任务", "结论", "工具", "灵感"};
    private static final String[] TYPE_VALUES = {
            ResearchTopicData.TYPE_PROBLEM,
            ResearchTopicData.TYPE_KNOWLEDGE,
            ResearchTopicData.TYPE_REFERENCE,
            ResearchTopicData.TYPE_TASK,
            ResearchTopicData.TYPE_CONCLUSION,
            ResearchTopicData.TYPE_TOOL,
            ResearchTopicData.TYPE_NOTE
    };

    private ScrollView scrollView;
    private LinearLayout rootLayout;
    private ResearchTopicData topicData;
    private EditText titleEdit;
    private EditText problemEdit;
    private EditText goalEdit;
    private EditText conclusionEdit;
    private ResearchMindMapView currentMindMapView;
    private boolean hasSavedMindMapViewport = false;
    private float savedMindMapScale;
    private float savedMindMapOffsetX;
    private float savedMindMapOffsetY;
    private int currentViewMode = VIEW_MODE_CARD;

    public FragmentResearchTopic() {
    }

    public static FragmentResearchTopic newInstance(String information, boolean isEdit) {
        FragmentResearchTopic fragment = new FragmentResearchTopic();
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
        topicData = ResearchTopicData.parse(infoEdit);
        currentViewMode = ResearchTopicData.VIEW_MODE_CARD.equals(topicData.getViewMode()) ? VIEW_MODE_CARD : VIEW_MODE_MAP;
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
        rememberMindMapViewport();
        rootLayout.removeAllViews();
        currentMindMapView = null;
        if (currentViewMode == VIEW_MODE_MAP) {
            renderMindMap();
        } else {
            renderHeader();
            renderRelationMap();
            renderNodes();
        }
    }

    public void toggleViewModeFromToolbar() {
        collectFromViews();
        currentViewMode = currentViewMode == VIEW_MODE_CARD ? VIEW_MODE_MAP : VIEW_MODE_CARD;
        topicData.setViewMode(currentViewMode == VIEW_MODE_CARD ? ResearchTopicData.VIEW_MODE_CARD : ResearchTopicData.VIEW_MODE_MAP);
        autoSaveParent();
        render();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public String getViewModeMenuTitle() {
        return currentViewMode == VIEW_MODE_CARD ? "图谱" : "卡片";
    }

    public void addNodeFromToolbar() {
        if (!isEdit) {
            return;
        }
        collectFromViews();
        showNodeEditDialog(ResearchTopicData.createNode(ResearchTopicData.TYPE_NOTE, "", "", ""), -1);
    }

    public void resetMindMapLayout() {
        if (getActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("重排图谱")
                .setMessage("会恢复节点的自动位置，但不会删除节点、内容和关联关系。确定要重排吗？")
                .setPositiveButton("重排", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetMindMapLayoutNow();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void resetMindMapLayoutNow() {
        collectFromViews();
        clearMindMapPositions();
        clearMindMapViewport();
        autoSaveParent();
        render();
    }

    private void clearMindMapPositions() {
        List<ResearchTopicData.ResearchNode> nodes = topicData.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            ResearchTopicData.ResearchNode node = nodes.get(i);
            if (node == null) {
                continue;
            }
            node.setMapPositioned(false);
            node.setMapX(0);
            node.setMapY(0);
        }
    }

    public void openFullscreenMindMap() {
        if (getActivity() == null) {
            return;
        }
        collectFromViews();
        final int oldOrientation = getActivity().getRequestedOrientation();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        FrameLayout contentLayout = new FrameLayout(getActivity());
        contentLayout.setBackgroundColor(Color.WHITE);

        final ResearchMindMapView mindMapView = createMindMapView(false);
        contentLayout.addView(mindMapView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topRow = new LinearLayout(getActivity());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        topRow.setPadding(dp(8), dp(8), dp(8), 0);
        topRow.setBackgroundColor(Color.TRANSPARENT);

        final Button closeButton = makeTransparentButton("X", 18);
        topRow.addView(closeButton, new LinearLayout.LayoutParams(dp(44), dp(40)));

        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        contentLayout.addView(topRow, topParams);

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
                render();
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

    private void renderMindMap() {
        LinearLayout card = makeCard();
        TextView title = makeText(topicData.getTitle(), 18, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView hint = makeText("拖动查看关系，双指缩放，点节点查看或编辑。", 12, R.color.text_11);
        hint.setPadding(0, dp(4), 0, dp(8));
        card.addView(hint);

        ResearchMindMapView mindMapView = createMindMapView(true);
        card.addView(mindMapView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(560)));
        rootLayout.addView(card, cardParams());
    }

    private ResearchMindMapView createMindMapView(boolean trackViewport) {
        ResearchMindMapView mindMapView = new ResearchMindMapView(getActivity());
        mindMapView.setTopicData(topicData);
        mindMapView.setNodeDragEnabled(isEdit);
        if (hasSavedMindMapViewport) {
            mindMapView.restoreViewport(savedMindMapScale, savedMindMapOffsetX, savedMindMapOffsetY);
        }
        if (trackViewport) {
            currentMindMapView = mindMapView;
        }
        mindMapView.setOnNodeClickListener(new ResearchMindMapView.OnNodeClickListener() {
            @Override
            public void onNodeClick(ResearchTopicData.ResearchNode node, int index) {
                collectFromViews();
                if (isEdit) {
                    showNodeEditDialog(node, index);
                } else {
                    showNodeDetailDialog(node);
                }
            }

            @Override
            public void onNodeLongClick(ResearchTopicData.ResearchNode node, int index) {
                if (!isEdit) {
                    return;
                }
                collectFromViews();
                showLinkedNodeEditDialog(node);
            }

            @Override
            public void onNodePositionChanged(ResearchTopicData.ResearchNode node, int index) {
                topicData = ResearchTopicData.normalize(topicData);
                autoSaveParent();
            }
        });
        return mindMapView;
    }

    private void rememberMindMapViewport() {
        if (currentMindMapView == null || !currentMindMapView.hasViewport()) {
            return;
        }
        savedMindMapScale = currentMindMapView.getViewportScale();
        savedMindMapOffsetX = currentMindMapView.getViewportOffsetX();
        savedMindMapOffsetY = currentMindMapView.getViewportOffsetY();
        hasSavedMindMapViewport = true;
    }

    private void clearMindMapViewport() {
        hasSavedMindMapViewport = false;
        currentMindMapView = null;
    }

    private void renderHeader() {
        LinearLayout card = makeCard();

        TextView label = makeText("课题路线图", 13, R.color.colorPrimary);
        label.setTypeface(null, Typeface.BOLD);
        card.addView(label);

        if (isEdit) {
            titleEdit = makeEditText("课题标题", false);
            titleEdit.setText(topicData.getTitle());
            card.addView(titleEdit, matchWrapParams(0, dp(6), 0, 0));

            problemEdit = makeEditText("问题定义：这个课题真正要解决什么？", true);
            problemEdit.setText(topicData.getProblem());
            problemEdit.setMinHeight(dp(74));
            card.addView(makeSmallLabel("问题定义"));
            card.addView(problemEdit, matchWrapParams(0, dp(2), 0, 0));

            goalEdit = makeEditText("阶段目标 / 预期产出", true);
            goalEdit.setText(topicData.getGoal());
            goalEdit.setMinHeight(dp(70));
            card.addView(makeSmallLabel("阶段目标"));
            card.addView(goalEdit, matchWrapParams(0, dp(2), 0, 0));

            conclusionEdit = makeEditText("阶段结论 / 当前判断", true);
            conclusionEdit.setText(topicData.getConclusion());
            conclusionEdit.setMinHeight(dp(70));
            card.addView(makeSmallLabel("阶段结论"));
            card.addView(conclusionEdit, matchWrapParams(0, dp(2), 0, 0));
        } else {
            TextView titleView = makeText(topicData.getTitle(), 22, R.color.message_text);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setPadding(0, dp(4), 0, dp(8));
            card.addView(titleView);

            addInfoBlock(card, "问题定义", topicData.getProblem(), Color.rgb(213, 64, 60));
            addInfoBlock(card, "阶段目标", topicData.getGoal(), Color.rgb(63, 81, 181));
            addInfoBlock(card, "阶段结论", topicData.getConclusion(), Color.rgb(9, 183, 99));
        }

        LinearLayout statsRow = new LinearLayout(getActivity());
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setPadding(0, dp(8), 0, 0);
        addStat(statsRow, "节点", topicData.getNodes().size(), Color.rgb(63, 81, 181));
        addStat(statsRow, "资料", topicData.countType(ResearchTopicData.TYPE_REFERENCE), Color.rgb(9, 183, 99));
        addStat(statsRow, "问题", topicData.countType(ResearchTopicData.TYPE_PROBLEM), Color.rgb(213, 64, 60));
        addStat(statsRow, "任务", topicData.countType(ResearchTopicData.TYPE_TASK), Color.rgb(217, 150, 16));
        card.addView(statsRow);

        rootLayout.addView(card, cardParams());
    }

    private void showNodeDetailDialog(ResearchTopicData.ResearchNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("类型：").append(ResearchTopicData.getTypeText(node.getType()));
        if (!node.getContent().trim().equals("")) {
            builder.append("\n\n").append(node.getContent());
        }
        String relatedTitles = topicData.getRelatedTitles(node);
        if (!relatedTitles.equals("")) {
            builder.append("\n\n关联：").append(relatedTitles);
        }
        if (node.getLinks().size() > 0) {
            builder.append("\n\n资料：\n").append(joinLines(node.getLinks()));
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(node.getTitle().trim().equals("") ? "未命名节点" : node.getTitle())
                .setMessage(builder.toString())
                .setPositiveButton("确定", null)
                .show();
    }

    private void renderRelationMap() {
        LinearLayout card = makeCard();
        TextView title = makeText("关系线索", 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        int count = 0;
        List<ResearchTopicData.ResearchNode> nodes = topicData.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            ResearchTopicData.ResearchNode node = nodes.get(i);
            for (int j = 0; j < node.getRelatedNodeIds().size(); j++) {
                ResearchTopicData.ResearchNode related = topicData.findNodeById(node.getRelatedNodeIds().get(j));
                if (related == null) {
                    continue;
                }
                TextView relationView = makeText(related.getTitle() + "  ->  " + node.getTitle(), 13, R.color.text_11);
                relationView.setPadding(0, dp(5), 0, 0);
                card.addView(relationView);
                count++;
                if (count >= 8) {
                    break;
                }
            }
            if (count >= 8) {
                break;
            }
        }
        if (count == 0) {
            TextView empty = makeText("还没有节点关系。后面可以把一个节点设成另一个节点的前置、参考或依赖。", 13, R.color.text_11);
            empty.setPadding(0, dp(6), 0, 0);
            card.addView(empty);
        }
        rootLayout.addView(card, cardParams());
    }

    private void renderNodes() {
        LinearLayout titleRow = new LinearLayout(getActivity());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.setPadding(dp(4), dp(8), dp(4), dp(2));

        TextView title = makeText("路线节点", 16, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        rootLayout.addView(titleRow);

        List<ResearchTopicData.ResearchNode> nodes = topicData.getNodes();
        if (nodes.size() == 0) {
            TextView empty = makeText("还没有节点。可以先加一个问题、资料或任务。", 14, R.color.text_11);
            empty.setGravity(Gravity.CENTER);
            rootLayout.addView(empty, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(90)));
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            addNodeViewCard(nodes.get(i), i);
        }
    }

    private void addNodeViewCard(final ResearchTopicData.ResearchNode node, final int index) {
        LinearLayout card = makeCard();

        LinearLayout topRow = new LinearLayout(getActivity());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView chip = makeChip(ResearchTopicData.getTypeText(node.getType()), typeColor(node.getType()));
        topRow.addView(chip);

        TextView title = makeText(node.getTitle(), 17, R.color.message_text);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        titleParams.setMargins(dp(8), 0, 0, 0);
        topRow.addView(title, titleParams);

        TextView number = makeText(String.valueOf(index + 1), 13, R.color.text_11);
        number.setGravity(Gravity.RIGHT);
        topRow.addView(number, new LinearLayout.LayoutParams(dp(34), ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(topRow);

        if (!node.getContent().trim().equals("")) {
            TextView content = makeText(node.getContent(), 14, R.color.message_text);
            content.setLineSpacing(dp(2), 1.0f);
            content.setPadding(0, dp(8), 0, 0);
            card.addView(content);
        }

        String relatedTitles = topicData.getRelatedTitles(node);
        if (!relatedTitles.equals("")) {
            addMetaLine(card, "关联", relatedTitles);
        }
        if (node.getLinks().size() > 0) {
            addMetaLine(card, "资料", joinLines(node.getLinks()));
        }

        if (isEdit) {
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collectFromViews();
                    showNodeEditDialog(node, index);
                }
            });
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    collectFromViews();
                    showLinkedNodeEditDialog(node);
                    return true;
                }
            });
        }

        rootLayout.addView(card, cardParams());
    }

    private void showLinkedNodeEditDialog(ResearchTopicData.ResearchNode parentNode) {
        if (parentNode == null) {
            return;
        }
        parentNode.normalize();
        ResearchTopicData.ResearchNode childNode = ResearchTopicData.createNode(ResearchTopicData.TYPE_NOTE, "", "", "");
        childNode.getRelatedNodeIds().add(parentNode.getId());
        showNodeEditDialog(childNode, -1);
    }

    private void showNodeEditDialog(final ResearchTopicData.ResearchNode node, final int index) {
        if (getActivity() == null) {
            return;
        }
        final NodeEditHolder holder = new NodeEditHolder();
        holder.node = node;
        holder.typeValue = node.getType();

        LinearLayout contentLayout = new LinearLayout(getActivity());
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(8), dp(16), dp(4));

        Button typeButton = makeTypeButton(holder.typeValue);
        holder.typeButton = typeButton;
        typeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNodeTypeDialog(holder);
            }
        });
        contentLayout.addView(makeSmallLabel("节点类型"));
        contentLayout.addView(typeButton, new LinearLayout.LayoutParams(dp(120), dp(38)));

        final EditText titleEdit = makeEditText("节点标题", false);
        titleEdit.setText(node.getTitle());
        contentLayout.addView(makeSmallLabel("标题"));
        contentLayout.addView(titleEdit, matchWrapParams(0, dp(2), 0, 0));

        final EditText contentEdit = makeEditText("节点内容", true);
        contentEdit.setText(node.getContent());
        contentEdit.setMinHeight(dp(110));
        contentLayout.addView(makeSmallLabel("内容"));
        contentLayout.addView(contentEdit, matchWrapParams(0, dp(2), 0, 0));

        final EditText linksEdit = makeEditText("资料链接，一行一个", true);
        linksEdit.setText(joinLines(node.getLinks()));
        linksEdit.setMinHeight(dp(72));
        contentLayout.addView(makeSmallLabel("资料链接"));
        contentLayout.addView(linksEdit, matchWrapParams(0, dp(2), 0, 0));

        final TextView relatedView = makeText("", 13, R.color.text_11);
        holder.relatedView = relatedView;
        updateRelatedText(holder);
        Button relatedButton = makeButton("选择关联节点", Color.rgb(63, 81, 181));
        relatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRelatedNodeDialog(holder);
            }
        });
        contentLayout.addView(makeSmallLabel("关联节点"));
        contentLayout.addView(relatedButton, new LinearLayout.LayoutParams(dp(128), dp(36)));
        contentLayout.addView(relatedView, matchWrapParams(0, dp(4), 0, 0));

        holder.titleEdit = titleEdit;
        holder.contentEdit = contentEdit;
        holder.linksEdit = linksEdit;

        ScrollView dialogScrollView = new ScrollView(getActivity());
        dialogScrollView.addView(contentLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(index < 0 ? "新增节点" : "编辑节点")
                .setView(dialogScrollView)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null);
        if (index >= 0) {
            builder.setNeutralButton("删除", null);
        }

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveNodeFromDialog(holder);
                        if (index < 0) {
                            topicData.getNodes().add(node);
                        }
                        render();
                        autoSaveParent();
                        dialog.dismiss();
                    }
                });
                if (index >= 0) {
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String id = node.getId();
                            topicData.getNodes().remove(index);
                            removeRelationTo(id);
                            render();
                            autoSaveParent();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        dialog.show();
    }

    private void collectFromViews() {
        if (!isEdit || titleEdit == null) {
            return;
        }
        topicData.setTitle(titleEdit.getText().toString());
        topicData.setProblem(problemEdit.getText().toString());
        topicData.setGoal(goalEdit.getText().toString());
        topicData.setConclusion(conclusionEdit.getText().toString());
    }

    private void autoSaveParent() {
        if (getActivity() instanceof ActivityEditInfo) {
            ((ActivityEditInfo) getActivity()).autoSaveCurrentContent();
        }
    }

    private void saveNodeFromDialog(NodeEditHolder holder) {
        holder.node.setType(holder.typeValue);
        holder.node.setTitle(holder.titleEdit.getText().toString());
        holder.node.setContent(holder.contentEdit.getText().toString());
        holder.node.getLinks().clear();
        String linksText = holder.linksEdit.getText().toString();
        String[] links = linksText.split("\\r?\\n");
        for (int j = 0; j < links.length; j++) {
            String link = links[j].trim();
            if (!link.equals("")) {
                holder.node.getLinks().add(link);
            }
        }
        holder.node.normalize();
    }

    private void showRelatedNodeDialog(final NodeEditHolder holder) {
        List<ResearchTopicData.ResearchNode> nodes = topicData.getNodes();
        List<ResearchTopicData.ResearchNode> candidates = new java.util.ArrayList<ResearchTopicData.ResearchNode>();
        for (int i = 0; i < nodes.size(); i++) {
            ResearchTopicData.ResearchNode item = nodes.get(i);
            if (item == null || item.getId().equals(holder.node.getId())) {
                continue;
            }
            candidates.add(item);
        }
        if (candidates.size() == 0) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("关联节点")
                    .setMessage("还没有其它节点可以关联。")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }
        final String[] labels = new String[candidates.size()];
        final String[] ids = new String[candidates.size()];
        final boolean[] checked = new boolean[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            ResearchTopicData.ResearchNode item = candidates.get(i);
            labels[i] = ResearchTopicData.getTypeText(item.getType()) + " · " + item.getTitle();
            ids[i] = item.getId();
            checked[i] = holder.node.getRelatedNodeIds().contains(ids[i]);
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("选择关联节点")
                .setMultiChoiceItems(labels, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        holder.node.getRelatedNodeIds().clear();
                        for (int i = 0; i < ids.length; i++) {
                            if (checked[i]) {
                                holder.node.getRelatedNodeIds().add(ids[i]);
                            }
                        }
                        updateRelatedText(holder);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateRelatedText(NodeEditHolder holder) {
        if (holder.relatedView == null) {
            return;
        }
        String relatedTitles = topicData.getRelatedTitles(holder.node);
        if (relatedTitles.equals("")) {
            holder.relatedView.setText("未关联其它节点");
        } else {
            holder.relatedView.setText("已关联：" + relatedTitles);
        }
    }

    private void showNodeTypeDialog(final NodeEditHolder holder) {
        if (getActivity() == null) {
            return;
        }
        final String[] labels = new String[TYPE_LABELS.length + 1];
        for (int i = 0; i < TYPE_LABELS.length; i++) {
            labels[i] = TYPE_LABELS[i];
        }
        labels[labels.length - 1] = "自定义...";
        new AlertDialog.Builder(getActivity())
                .setTitle("选择节点类型")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == TYPE_LABELS.length) {
                            showCustomTypeDialog(holder);
                        } else {
                            holder.typeValue = TYPE_VALUES[which];
                            styleTypeButton(holder.typeButton, holder.typeValue);
                        }
                    }
                })
                .show();
    }

    private void showCustomTypeDialog(final NodeEditHolder holder) {
        final EditText editText = makeEditText("例如：图表、文献、实验、风险", false);
        if (!isBuiltInType(holder.typeValue)) {
            editText.setText(holder.typeValue);
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("自定义节点类型")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String type = editText.getText().toString().trim();
                        if (!type.equals("")) {
                            holder.typeValue = type;
                            styleTypeButton(holder.typeButton, holder.typeValue);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private boolean isBuiltInType(String type) {
        for (int i = 0; i < TYPE_VALUES.length; i++) {
            if (TYPE_VALUES[i].equals(type)) {
                return true;
            }
        }
        return false;
    }

    private void removeRelationTo(String id) {
        List<ResearchTopicData.ResearchNode> nodes = topicData.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).getRelatedNodeIds().remove(id);
        }
    }

    private void addInfoBlock(LinearLayout parent, String label, String content, int color) {
        if (content == null || content.trim().equals("")) {
            return;
        }
        TextView labelView = makeText(label, 12, R.color.text_11);
        labelView.setTextColor(color);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setPadding(0, dp(8), 0, dp(2));
        parent.addView(labelView);

        TextView contentView = makeText(content, 14, R.color.message_text);
        contentView.setLineSpacing(dp(2), 1.0f);
        parent.addView(contentView);
    }

    private void addMetaLine(LinearLayout parent, String label, String text) {
        TextView view = makeText(label + "：" + text, 13, R.color.text_11);
        view.setPadding(0, dp(7), 0, 0);
        parent.addView(view);
    }

    private void addStat(LinearLayout row, String label, int value, int color) {
        LinearLayout stat = new LinearLayout(getActivity());
        stat.setOrientation(LinearLayout.VERTICAL);
        stat.setGravity(Gravity.CENTER);
        stat.setBackground(makeRoundBackground(Color.rgb(248, 248, 248), Color.rgb(232, 232, 232), 7));
        stat.setPadding(dp(2), dp(5), dp(2), dp(5));

        TextView valueView = makeText(String.valueOf(value), 17, R.color.message_text);
        valueView.setTextColor(color);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);
        stat.addView(valueView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView labelView = makeText(label, 10, R.color.text_11);
        labelView.setGravity(Gravity.CENTER);
        stat.addView(labelView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(dp(2), 0, dp(2), 0);
        row.addView(stat, params);
    }

    private LinearLayout makeCard() {
        LinearLayout card = new LinearLayout(getActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(11), dp(12), dp(11));
        card.setBackground(makeRoundBackground(Color.WHITE, Color.rgb(232, 232, 232), 8));
        return card;
    }

    private TextView makeChip(String text, int color) {
        TextView chip = makeText(text, 12, R.color.colorPrimary);
        chip.setTextColor(color);
        chip.setGravity(Gravity.CENTER);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setPadding(dp(8), dp(4), dp(8), dp(4));
        chip.setBackground(makeRoundBackground(Color.WHITE, color, 7));
        return chip;
    }

    private TextView makeSmallLabel(String text) {
        TextView label = makeText(text, 12, R.color.text_11);
        label.setPadding(0, dp(8), 0, dp(2));
        return label;
    }

    private Button makeButton(String text, int color) {
        Button button = new Button(getActivity());
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setMinimumHeight(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackground(makeRoundBackground(color, color, 7));
        return button;
    }

    private Button makeTransparentButton(String text, int sp) {
        Button button = new Button(getActivity());
        button.setText(text);
        button.setTextSize(sp);
        button.setTextColor(getResources().getColor(R.color.message_text));
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setMinimumHeight(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackgroundColor(Color.TRANSPARENT);
        return button;
    }

    private Button makeTypeButton(String type) {
        Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setMinimumHeight(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(4), 0, dp(4), 0);
        styleTypeButton(button, type);
        return button;
    }

    private void styleTypeButton(Button button, String type) {
        int color = typeColor(type);
        button.setText(ResearchTopicData.getTypeText(type));
        button.setTextColor(color);
        button.setBackground(makeRoundBackground(Color.WHITE, color, 7));
    }

    private EditText makeEditText(String hint, boolean multiLine) {
        EditText editText = new EditText(getActivity());
        editText.setHint(hint);
        editText.setTextSize(15);
        editText.setTextColor(getResources().getColor(R.color.message_text));
        editText.setSingleLine(!multiLine);
        editText.setPadding(dp(8), dp(6), dp(8), dp(6));
        editText.setBackground(makeRoundBackground(Color.WHITE, Color.rgb(232, 232, 232), 7));
        if (multiLine) {
            editText.setMinLines(2);
            editText.setMaxLines(Integer.MAX_VALUE);
            editText.setHorizontallyScrolling(false);
            editText.setVerticalScrollBarEnabled(false);
            editText.setOverScrollMode(View.OVER_SCROLL_NEVER);
            editText.setGravity(Gravity.TOP);
        }
        return editText;
    }

    private TextView makeText(String text, int sp, int colorId) {
        TextView textView = new TextView(getActivity());
        textView.setText(text == null ? "" : text);
        textView.setTextSize(sp);
        textView.setTextColor(getResources().getColor(colorId));
        return textView;
    }

    private int typeColor(String type) {
        if (ResearchTopicData.TYPE_PROBLEM.equals(type)) {
            return Color.rgb(213, 64, 60);
        }
        if (ResearchTopicData.TYPE_REFERENCE.equals(type)) {
            return Color.rgb(9, 183, 99);
        }
        if (ResearchTopicData.TYPE_TASK.equals(type)) {
            return Color.rgb(217, 150, 16);
        }
        if (ResearchTopicData.TYPE_CONCLUSION.equals(type)) {
            return Color.rgb(147, 91, 188);
        }
        if (ResearchTopicData.TYPE_TOOL.equals(type)) {
            return Color.rgb(0, 137, 123);
        }
        if (ResearchTopicData.TYPE_KNOWLEDGE.equals(type)) {
            return Color.rgb(63, 81, 181);
        }
        return Color.rgb(153, 153, 153);
    }

    private String joinLines(List<String> values) {
        if (values == null || values.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == null || values.get(i).trim().equals("")) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(values.get(i).trim());
        }
        return builder.toString();
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(9));
        return params;
    }

    private LinearLayout.LayoutParams matchWrapParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private GradientDrawable makeRoundBackground(int fillColor, int strokeColor, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
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
        topicData.setViewMode(currentViewMode == VIEW_MODE_CARD ? ResearchTopicData.VIEW_MODE_CARD : ResearchTopicData.VIEW_MODE_MAP);
        topicData = ResearchTopicData.normalize(topicData);
        return JSON.toJSONString(topicData);
    }

    @Override
    public void enableEdit() {
        isEdit = true;
        render();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public void disableEdit() {
        collectFromViews();
        isEdit = false;
        render();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private static class NodeEditHolder {
        ResearchTopicData.ResearchNode node;
        Button typeButton;
        String typeValue;
        EditText titleEdit;
        EditText contentEdit;
        EditText linksEdit;
        TextView relatedView;
    }
}
