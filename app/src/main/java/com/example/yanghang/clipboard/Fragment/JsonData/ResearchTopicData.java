package com.example.yanghang.clipboard.Fragment.JsonData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResearchTopicData {
    public static final String CATALOGUE_NAME = "课题";
    public static final String DATA_TYPE = "researchTopic";

    public static final String TYPE_PROBLEM = "problem";
    public static final String TYPE_KNOWLEDGE = "knowledge";
    public static final String TYPE_REFERENCE = "reference";
    public static final String TYPE_TASK = "task";
    public static final String TYPE_CONCLUSION = "conclusion";
    public static final String TYPE_TOOL = "tool";
    public static final String TYPE_NOTE = "note";
    public static final String VIEW_MODE_CARD = "card";
    public static final String VIEW_MODE_MAP = "map";

    private String dataType;
    private String title;
    private String problem;
    private String goal;
    private String conclusion;
    private String viewMode;
    private List<ResearchNode> nodes;

    public ResearchTopicData() {
    }

    public static ResearchTopicData parse(String content) {
        if (content == null || content.trim().equals("")) {
            return createSample();
        }
        ResearchTopicData data = null;
        try {
            data = JSON.parseObject(content, ResearchTopicData.class);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data == null) {
            data = createFromPlainText(content);
        }
        return normalize(data);
    }

    public static ResearchTopicData createSample() {
        // 旧论文示例保留在 createPaperSample()，现在默认新建空白课题。
        // return createPaperSample();
        ResearchTopicData data = new ResearchTopicData();
        data.setDataType(DATA_TYPE);
        data.setTitle("");
        data.setViewMode(VIEW_MODE_MAP);
        return data;
    }

    private static ResearchTopicData createPaperSample() {
        ResearchTopicData data = new ResearchTopicData();
        data.setDataType(DATA_TYPE);
        data.setTitle("如何写一篇论文");
        data.setProblem("把一个研究想法推进成可以投稿的论文：先讲清楚 motivation 和 gap，再用方法、实验、图和文字说服别人。");
        data.setGoal("形成一条可执行路线：确定问题 -> 设计方法和实验 -> 做结果图 -> 汇报迭代 -> 写作和润色 -> 投稿前检查。");
        data.setConclusion("论文不是从 Abstract 开始硬写，而是先把“为什么值得研究”和“结果是否支撑贡献”打磨清楚。");

        ResearchNode motivation = createNode(TYPE_PROBLEM, "确定 Motivation",
                "回答为什么要研究这个问题：现阶段存在什么痛点、缺口或矛盾？这个理由要能说服导师、审稿人和自己。",
                "");
        ResearchNode gap = createNode(TYPE_KNOWLEDGE, "现有工作与研究缺口",
                "梳理已有方法解决了什么、没解决什么。这里决定 Introduction 里怎么引出问题，也决定引用文献怎么找。",
                "");
        ResearchNode methodDesign = createNode(TYPE_TASK, "实验方法与方案设计",
                "设计方法框架、实验变量、对比方法、评价指标和消融实验。先确认实验能回答 motivation 提出的问题。",
                "");
        ResearchNode feedback = createNode(TYPE_TASK, "阶段汇报与导师评估",
                "把 motivation、方法草图、初步结果做成汇报，听老师判断方向是否值得继续推进，再调整实验和叙事。",
                "");
        ResearchNode conceptFigure = createNode(TYPE_TASK, "画概念图 / Framework 图",
                "先把核心想法画成草图：可以自己列思路，找 GPT 帮忙整理布局，找 Gemini 做资料调研和草图启发，最后自己在 PPT 里正式重画并导出 PDF。",
                "");
        ResearchNode resultFigures = createNode(TYPE_TASK, "整理实验结果图",
                "把仿真、真实实验、对比曲线、表格和可视化结果整理成能支撑结论的图。图先服务论点，不只是把结果堆上去。",
                "");
        ResearchNode latexProject = createNode(TYPE_TOOL, "建立 TeX 项目",
                "新建论文 TeX 项目，放入模板、bib、figure、table。图尽量用 PDF / 高分辨率图片，命名保持清楚。",
                "");
        ResearchNode structure = createNode(TYPE_KNOWLEDGE, "论文结构",
                "常见顺序：Abstract、Introduction、Related Work、Methodology、Experiments、Results/Discussion、Conclusion。实际写作时可以先写 Method 和 Experiments。",
                "");
        ResearchNode introduction = createNode(TYPE_TASK, "打磨 Introduction 和引用",
                "Introduction 要讲清楚背景、问题、gap、贡献。引用文献要支撑每一步叙事，避免只堆论文名。",
                "");
        ResearchNode abstractNode = createNode(TYPE_TASK, "反复打磨 Abstract",
                "Abstract 最后一定要回头重写：一句背景，一句问题，一句方法，一句结果，一句贡献。要短、准、有说服力。",
                "");
        ResearchNode polish = createNode(TYPE_TASK, "整体润色与一致性检查",
                "检查术语、图文引用、实验描述、贡献表述、符号一致性、语法和逻辑链。每一张图都要在正文里被解释。",
                "");
        ResearchNode checklist = createNode(TYPE_CONCLUSION, "投稿前检查",
                "确认 motivation 清楚、实验支撑结论、图表可读、引用完整、Abstract/Introduction 打磨过、模板格式无明显问题。",
                "");

        gap.getRelatedNodeIds().add(motivation.getId());
        methodDesign.getRelatedNodeIds().add(motivation.getId());
        feedback.getRelatedNodeIds().add(methodDesign.getId());
        conceptFigure.getRelatedNodeIds().add(methodDesign.getId());
        resultFigures.getRelatedNodeIds().add(methodDesign.getId());
        latexProject.getRelatedNodeIds().add(conceptFigure.getId());
        latexProject.getRelatedNodeIds().add(resultFigures.getId());
        structure.getRelatedNodeIds().add(latexProject.getId());
        introduction.getRelatedNodeIds().add(gap.getId());
        abstractNode.getRelatedNodeIds().add(introduction.getId());
        abstractNode.getRelatedNodeIds().add(resultFigures.getId());
        polish.getRelatedNodeIds().add(structure.getId());
        polish.getRelatedNodeIds().add(abstractNode.getId());
        checklist.getRelatedNodeIds().add(polish.getId());

        data.getNodes().add(motivation);
        data.getNodes().add(gap);
        data.getNodes().add(methodDesign);
        data.getNodes().add(feedback);
        data.getNodes().add(conceptFigure);
        data.getNodes().add(resultFigures);
        data.getNodes().add(latexProject);
        data.getNodes().add(structure);
        data.getNodes().add(introduction);
        data.getNodes().add(abstractNode);
        data.getNodes().add(polish);
        data.getNodes().add(checklist);
        return data;
    }

    private static ResearchTopicData createFromPlainText(String content) {
        ResearchTopicData data = new ResearchTopicData();
        data.setDataType(DATA_TYPE);
        data.setTitle(firstLine(content, "未命名课题"));
        data.setProblem("从旧文本转换而来，可以继续拆成节点。");
        data.getNodes().add(createNode(TYPE_NOTE, "原始记录", content, ""));
        return data;
    }

    public static ResearchTopicData normalize(ResearchTopicData data) {
        if (data == null) {
            return createSample();
        }
        data.setDataType(DATA_TYPE);
        data.setTitle(data.getTitle());
        data.setProblem(data.getProblem());
        data.setGoal(data.getGoal());
        data.setConclusion(data.getConclusion());
        data.setViewMode(data.getViewMode());
        List<ResearchNode> normalizedNodes = data.getNodes();
        for (int i = normalizedNodes.size() - 1; i >= 0; i--) {
            ResearchNode node = normalizedNodes.get(i);
            if (node == null) {
                normalizedNodes.remove(i);
            } else {
                node.normalize();
            }
        }
        return data;
    }

    public static ResearchNode createNode(String type, String title, String content, String link) {
        ResearchNode node = new ResearchNode();
        node.setType(type);
        node.setTitle(title);
        node.setContent(content);
        if (link != null && !link.trim().equals("")) {
            node.getLinks().add(link.trim());
        }
        node.normalize();
        return node;
    }

    public String buildSimpleContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("课题：").append(getTitle().equals("") ? "未命名课题" : getTitle());
        builder.append("\n节点 ").append(getNodes().size());
        builder.append(" · 资料 ").append(countType(TYPE_REFERENCE));
        builder.append(" · 问题 ").append(countType(TYPE_PROBLEM));
        builder.append(" · 任务 ").append(countType(TYPE_TASK));
        builder.append(" · 结论 ").append(countType(TYPE_CONCLUSION));
        if (!getProblem().trim().equals("")) {
            builder.append("\n问题定义：").append(firstLine(getProblem(), ""));
        }
        if (!getGoal().trim().equals("")) {
            builder.append("\n目标：").append(firstLine(getGoal(), ""));
        }
        int limit = Math.min(4, getNodes().size());
        for (int i = 0; i < limit; i++) {
            ResearchNode node = getNodes().get(i);
            if (node == null) {
                continue;
            }
            builder.append("\n• [").append(getTypeText(node.getType())).append("] ").append(node.getTitle());
        }
        if (getNodes().size() > limit) {
            builder.append("\n…");
        }
        return builder.toString();
    }

    public int countType(String type) {
        int count = 0;
        for (int i = 0; i < getNodes().size(); i++) {
            ResearchNode node = getNodes().get(i);
            if (node != null && type.equals(node.getType())) {
                count++;
            }
        }
        return count;
    }

    public ResearchNode findNodeById(String id) {
        if (id == null || id.trim().equals("")) {
            return null;
        }
        for (int i = 0; i < getNodes().size(); i++) {
            ResearchNode node = getNodes().get(i);
            if (node != null && id.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    public String getRelatedTitles(ResearchNode node) {
        if (node == null || node.getRelatedNodeIds().size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < node.getRelatedNodeIds().size(); i++) {
            ResearchNode related = findNodeById(node.getRelatedNodeIds().get(i));
            if (related == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append(related.getTitle());
        }
        return builder.toString();
    }

    public static String getTypeText(String type) {
        if (TYPE_PROBLEM.equals(type)) {
            return "问题";
        }
        if (TYPE_KNOWLEDGE.equals(type)) {
            return "概念";
        }
        if (TYPE_REFERENCE.equals(type)) {
            return "资料";
        }
        if (TYPE_TASK.equals(type)) {
            return "任务";
        }
        if (TYPE_CONCLUSION.equals(type)) {
            return "结论";
        }
        if (TYPE_TOOL.equals(type)) {
            return "工具";
        }
        if (type != null && !type.trim().equals("") && !TYPE_NOTE.equals(type)) {
            return type.trim();
        }
        return "灵感";
    }

    private static String firstLine(String value, String fallback) {
        String text = value == null ? "" : value.trim();
        if (text.equals("")) {
            return fallback;
        }
        int index = text.indexOf('\n');
        if (index >= 0) {
            return text.substring(0, index).trim();
        }
        return text;
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

    public String getTitle() {
        if (title == null) {
            return "";
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getProblem() {
        if (problem == null) {
            return "";
        }
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem == null ? "" : problem;
    }

    public String getGoal() {
        if (goal == null) {
            return "";
        }
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal == null ? "" : goal;
    }

    public String getConclusion() {
        if (conclusion == null) {
            return "";
        }
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion == null ? "" : conclusion;
    }

    public String getViewMode() {
        if (VIEW_MODE_CARD.equals(viewMode)) {
            return VIEW_MODE_CARD;
        }
        return VIEW_MODE_MAP;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = VIEW_MODE_CARD.equals(viewMode) ? VIEW_MODE_CARD : VIEW_MODE_MAP;
    }

    public List<ResearchNode> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<ResearchNode>();
        }
        return nodes;
    }

    public void setNodes(List<ResearchNode> nodes) {
        this.nodes = nodes;
    }

    public static class ResearchNode {
        private String id;
        private String type;
        private String title;
        private String content;
        private boolean mapPositioned;
        private float mapX;
        private float mapY;
        private List<String> links;
        private List<String> relatedNodeIds;

        public ResearchNode() {
        }

        public void normalize() {
            getId();
            setType(getType());
            setTitle(getTitle());
            setContent(getContent());
            getLinks();
            getRelatedNodeIds();
        }

        public String getId() {
            if (id == null || id.trim().equals("")) {
                id = UUID.randomUUID().toString();
            }
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            if (type == null || type.trim().equals("")) {
                return TYPE_NOTE;
            }
            return type;
        }

        public void setType(String type) {
            this.type = type == null || type.trim().equals("") ? TYPE_NOTE : type;
        }

        public String getTitle() {
            if (title == null) {
                return "";
            }
            return title;
        }

        public void setTitle(String title) {
            this.title = title == null ? "" : title;
        }

        public String getContent() {
            if (content == null) {
                return "";
            }
            return content;
        }

        public void setContent(String content) {
            this.content = content == null ? "" : content;
        }

        public boolean isMapPositioned() {
            return mapPositioned;
        }

        public void setMapPositioned(boolean mapPositioned) {
            this.mapPositioned = mapPositioned;
        }

        public float getMapX() {
            return mapX;
        }

        public void setMapX(float mapX) {
            this.mapX = mapX;
        }

        public float getMapY() {
            return mapY;
        }

        public void setMapY(float mapY) {
            this.mapY = mapY;
        }

        public List<String> getLinks() {
            if (links == null) {
                links = new ArrayList<String>();
            }
            return links;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        public List<String> getRelatedNodeIds() {
            if (relatedNodeIds == null) {
                relatedNodeIds = new ArrayList<String>();
            }
            return relatedNodeIds;
        }

        public void setRelatedNodeIds(List<String> relatedNodeIds) {
            this.relatedNodeIds = relatedNodeIds;
        }
    }
}
