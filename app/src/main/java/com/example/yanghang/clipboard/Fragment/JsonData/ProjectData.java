package com.example.yanghang.clipboard.Fragment.JsonData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProjectData {
    public static final String CATALOGUE_NAME = "项目";
    public static final String DATA_TYPE = "project";

    public static final String STATUS_PLANNING = "planning";
    public static final String STATUS_DOING = "doing";
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_ARCHIVED = "archived";

    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";

    private String id;
    private String dataType;
    private String title;
    private String goal;
    private String status;
    private String priority;
    private String startDate;
    private String endDate;
    private int progress;
    private String nextAction;
    private String updatedAt;
    private List<ProjectStage> stages;
    private List<ProjectMilestone> milestones;
    private List<ProjectDeliverable> deliverables;

    public ProjectData() {
    }

    public static ProjectData parse(String content) {
        if (content == null || content.trim().equals("")) {
            return createBlank();
        }
        ProjectData data = null;
        try {
            data = JSON.parseObject(content, ProjectData.class);
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

    public static ProjectData createBlank() {
        ProjectData data = new ProjectData();
        data.setDataType(DATA_TYPE);
        data.setTitle("");
        data.setGoal("");
        data.setStatus(STATUS_PLANNING);
        data.setPriority(PRIORITY_MEDIUM);
        data.setStartDate(today());
        data.setEndDate(today());
        data.setNextAction("");
        data.setProgress(0);
        return normalize(data);
    }

    public static ProjectData createSample() {
        ProjectData data = new ProjectData();
        data.setDataType(DATA_TYPE);
        data.setTitle("开发一个轻量 CAD 软件");
        data.setGoal("做一个能打开和显示 CAD 模型的轻量原型，先跑通 STEP viewer，再决定是否继续进入编辑和建模。");
        data.setStatus(STATUS_DOING);
        data.setPriority(PRIORITY_HIGH);
        data.setStartDate("2026-05-01");
        data.setEndDate("2026-07-30");
        data.setNextAction("先把 OCC/FreeCAD/显示层的技术路线整理清楚，然后做最小 Demo。");

        ProjectStage research = createStage("调研 OCC / FreeCAD", "2026-05-01", "2026-05-15", 100, STATUS_DONE,
                "整理几何内核、显示层、交互层分别怎么选型。");
        ProjectStage route = createStage("确定技术路线", "2026-05-10", "2026-05-22", 60, STATUS_DOING,
                "先不要一开始就做完整 CAD，优先确认 STEP 加载、显示、基础交互。");
        ProjectStage viewer = createStage("模型加载 Demo", "2026-05-20", "2026-06-08", 15, STATUS_DOING,
                "跑通一个可以打开 STEP 文件并显示模型的原型。");
        ProjectStage interaction = createStage("渲染与交互", "2026-06-05", "2026-06-25", 0, STATUS_PLANNING,
                "旋转、缩放、选择对象、显示层控制。");
        ProjectStage packageStage = createStage("打包测试", "2026-06-24", "2026-07-10", 0, STATUS_PLANNING,
                "整理依赖、打包 Windows 版本、记录使用限制。");

        route.getDependsOn().add(research.getId());
        viewer.getDependsOn().add(route.getId());
        interaction.getDependsOn().add(viewer.getId());
        packageStage.getDependsOn().add(interaction.getId());

        data.getStages().add(research);
        data.getStages().add(route);
        data.getStages().add(viewer);
        data.getStages().add(interaction);
        data.getStages().add(packageStage);

        ProjectMilestone demo = new ProjectMilestone();
        demo.setTitle("第一个 STEP viewer Demo 跑通");
        demo.setDate("2026-06-08");
        data.getMilestones().add(demo);

        ProjectDeliverable deliverable = new ProjectDeliverable();
        deliverable.setStageId(research.getId());
        deliverable.setName("FreeCAD 架构参考.md");
        deliverable.setFileType("md");
        deliverable.setRelativePath("projects/cad-project/deliverables/01-research/freecad-architecture.md");
        deliverable.setDescription("记录 FreeCAD 模块结构、OCC 依赖和可参考的 viewer 流程。");
        data.getDeliverables().add(deliverable);

        data.setProgress(data.calculateProgressFromStages());
        return normalize(data);
    }

    public static ProjectStage createStage(String title, String startDate, String endDate, int progress, String status, String note) {
        ProjectStage stage = new ProjectStage();
        stage.setTitle(title);
        stage.setStartDate(startDate);
        stage.setEndDate(endDate);
        stage.setProgress(progress);
        stage.setStatus(status);
        stage.setNote(note);
        return stage;
    }

    private static ProjectData createFromPlainText(String content) {
        ProjectData data = new ProjectData();
        data.setDataType(DATA_TYPE);
        data.setTitle(firstLine(content, "未命名项目"));
        data.setGoal(content);
        data.setStatus(STATUS_PLANNING);
        data.setPriority(PRIORITY_MEDIUM);
        data.setStartDate(today());
        data.setEndDate(today());
        return data;
    }

    public static ProjectData normalize(ProjectData data) {
        if (data == null) {
            return createBlank();
        }
        data.getId();
        data.setDataType(DATA_TYPE);
        data.setTitle(data.getTitle());
        data.setGoal(data.getGoal());
        data.setStatus(data.getStatus());
        data.setPriority(data.getPriority());
        data.setStartDate(data.getStartDate());
        data.setEndDate(data.getEndDate());
        data.setNextAction(data.getNextAction());
        data.setUpdatedAt(data.getUpdatedAt());
        List<ProjectStage> stages = data.getStages();
        for (int i = stages.size() - 1; i >= 0; i--) {
            ProjectStage stage = stages.get(i);
            if (stage == null) {
                stages.remove(i);
            } else {
                stage.normalize();
            }
        }
        cleanStageDependencies(stages);
        List<ProjectMilestone> milestones = data.getMilestones();
        for (int i = milestones.size() - 1; i >= 0; i--) {
            ProjectMilestone milestone = milestones.get(i);
            if (milestone == null) {
                milestones.remove(i);
            } else {
                milestone.normalize();
            }
        }
        List<ProjectDeliverable> deliverables = data.getDeliverables();
        for (int i = deliverables.size() - 1; i >= 0; i--) {
            ProjectDeliverable deliverable = deliverables.get(i);
            if (deliverable == null) {
                deliverables.remove(i);
            } else {
                deliverable.normalize();
            }
        }
        if (data.progress < 0 || data.progress > 100) {
            data.setProgress(data.calculateProgressFromStages());
        }
        return data;
    }

    private static void cleanStageDependencies(List<ProjectStage> stages) {
        Set<String> stageIds = new HashSet<String>();
        for (int i = 0; i < stages.size(); i++) {
            stageIds.add(stages.get(i).getId());
        }
        for (int i = 0; i < stages.size(); i++) {
            ProjectStage stage = stages.get(i);
            List<String> dependsOn = stage.getDependsOn();
            for (int j = dependsOn.size() - 1; j >= 0; j--) {
                String dependencyId = dependsOn.get(j);
                if (!stageIds.contains(dependencyId) || dependencyId.equals(stage.getId())) {
                    dependsOn.remove(j);
                }
            }
        }
    }

    public String buildSimpleContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("项目：").append(getTitle().equals("") ? "未命名项目" : getTitle());
        builder.append("\n").append(getStatusText(getStatus())).append(" · 进度 ").append(getProgress()).append("%");
        builder.append(" · 阶段 ").append(getStages().size());
        builder.append(" · 里程碑 ").append(getMilestones().size());
        builder.append(" · 交付物 ").append(getDeliverables().size());
        if (!getStartDate().equals("") || !getEndDate().equals("")) {
            builder.append("\n").append(getStartDate()).append(" -> ").append(getEndDate());
        }
        if (!getNextAction().trim().equals("")) {
            builder.append("\n下一步：").append(firstLine(getNextAction(), ""));
        }
        int limit = Math.min(4, getStages().size());
        for (int i = 0; i < limit; i++) {
            ProjectStage stage = getStages().get(i);
            builder.append("\n· ").append(stage.getTitle())
                    .append("  ").append(stage.getProgress()).append("%")
                    .append("  ").append(getStatusText(stage.getStatus()));
        }
        if (getStages().size() > limit) {
            builder.append("\n· ...");
        }
        return builder.toString();
    }

    public int calculateProgressFromStages() {
        List<ProjectStage> list = getStages();
        if (list.size() == 0) {
            return getProgress();
        }
        int total = 0;
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i).getProgress();
        }
        return total / list.size();
    }

    public static String getStatusText(String status) {
        if (STATUS_DOING.equals(status)) {
            return "进行中";
        }
        if (STATUS_PAUSED.equals(status)) {
            return "暂停";
        }
        if (STATUS_DONE.equals(status)) {
            return "完成";
        }
        if (STATUS_ARCHIVED.equals(status)) {
            return "归档";
        }
        return "规划中";
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

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
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

    public String getDataType() {
        return dataType == null ? "" : dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getGoal() {
        return goal == null ? "" : goal;
    }

    public void setGoal(String goal) {
        this.goal = goal == null ? "" : goal;
    }

    public String getStatus() {
        if (status == null || status.trim().equals("")) {
            return STATUS_PLANNING;
        }
        return status;
    }

    public void setStatus(String status) {
        if (STATUS_DOING.equals(status) || STATUS_PAUSED.equals(status) || STATUS_DONE.equals(status) || STATUS_ARCHIVED.equals(status)) {
            this.status = status;
        } else {
            this.status = STATUS_PLANNING;
        }
    }

    public String getPriority() {
        if (priority == null || priority.trim().equals("")) {
            return PRIORITY_MEDIUM;
        }
        return priority;
    }

    public void setPriority(String priority) {
        if (PRIORITY_LOW.equals(priority) || PRIORITY_HIGH.equals(priority)) {
            this.priority = priority;
        } else {
            this.priority = PRIORITY_MEDIUM;
        }
    }

    public String getStartDate() {
        return startDate == null ? "" : startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate == null ? "" : startDate;
    }

    public String getEndDate() {
        return endDate == null ? "" : endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate == null ? "" : endDate;
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

    public String getNextAction() {
        return nextAction == null ? "" : nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction == null ? "" : nextAction;
    }

    public String getUpdatedAt() {
        return updatedAt == null ? "" : updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt == null ? "" : updatedAt;
    }

    public List<ProjectStage> getStages() {
        if (stages == null) {
            stages = new ArrayList<ProjectStage>();
        }
        return stages;
    }

    public void setStages(List<ProjectStage> stages) {
        this.stages = stages;
    }

    public List<ProjectMilestone> getMilestones() {
        if (milestones == null) {
            milestones = new ArrayList<ProjectMilestone>();
        }
        return milestones;
    }

    public void setMilestones(List<ProjectMilestone> milestones) {
        this.milestones = milestones;
    }

    public List<ProjectDeliverable> getDeliverables() {
        if (deliverables == null) {
            deliverables = new ArrayList<ProjectDeliverable>();
        }
        return deliverables;
    }

    public void setDeliverables(List<ProjectDeliverable> deliverables) {
        this.deliverables = deliverables;
    }

    public static class ProjectStage {
        private String id;
        private String title;
        private String status;
        private String startDate;
        private String endDate;
        private int progress;
        private String note;
        private List<String> dependsOn;
        private List<String> linkedTodoIds;
        private List<String> linkedResearchIds;
        private List<String> linkedNoteIds;
        private List<String> deliverableIds;

        public void normalize() {
            getId();
            setTitle(getTitle());
            setStatus(getStatus());
            setStartDate(getStartDate());
            setEndDate(getEndDate());
            setProgress(getProgress());
            setNote(getNote());
            cleanList(getDependsOn());
            cleanList(getLinkedTodoIds());
            cleanList(getLinkedResearchIds());
            cleanList(getLinkedNoteIds());
            cleanList(getDeliverableIds());
        }

        private void cleanList(List<String> list) {
            for (int i = list.size() - 1; i >= 0; i--) {
                String value = list.get(i);
                if (value == null || value.trim().equals("") || value.trim().equals(getId())) {
                    list.remove(i);
                } else {
                    list.set(i, value.trim());
                }
            }
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

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title == null ? "" : title;
        }

        public String getStatus() {
            if (status == null || status.trim().equals("")) {
                return STATUS_PLANNING;
            }
            return status;
        }

        public void setStatus(String status) {
            if (STATUS_DOING.equals(status) || STATUS_PAUSED.equals(status) || STATUS_DONE.equals(status) || STATUS_ARCHIVED.equals(status)) {
                this.status = status;
            } else {
                this.status = STATUS_PLANNING;
            }
        }

        public String getStartDate() {
            return startDate == null ? "" : startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate == null ? "" : startDate;
        }

        public String getEndDate() {
            return endDate == null ? "" : endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate == null ? "" : endDate;
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

        public String getNote() {
            return note == null ? "" : note;
        }

        public void setNote(String note) {
            this.note = note == null ? "" : note;
        }

        public List<String> getDependsOn() {
            if (dependsOn == null) {
                dependsOn = new ArrayList<String>();
            }
            return dependsOn;
        }

        public void setDependsOn(List<String> dependsOn) {
            this.dependsOn = dependsOn;
        }

        public List<String> getLinkedTodoIds() {
            if (linkedTodoIds == null) {
                linkedTodoIds = new ArrayList<String>();
            }
            return linkedTodoIds;
        }

        public void setLinkedTodoIds(List<String> linkedTodoIds) {
            this.linkedTodoIds = linkedTodoIds;
        }

        public List<String> getLinkedResearchIds() {
            if (linkedResearchIds == null) {
                linkedResearchIds = new ArrayList<String>();
            }
            return linkedResearchIds;
        }

        public void setLinkedResearchIds(List<String> linkedResearchIds) {
            this.linkedResearchIds = linkedResearchIds;
        }

        public List<String> getLinkedNoteIds() {
            if (linkedNoteIds == null) {
                linkedNoteIds = new ArrayList<String>();
            }
            return linkedNoteIds;
        }

        public void setLinkedNoteIds(List<String> linkedNoteIds) {
            this.linkedNoteIds = linkedNoteIds;
        }

        public List<String> getDeliverableIds() {
            if (deliverableIds == null) {
                deliverableIds = new ArrayList<String>();
            }
            return deliverableIds;
        }

        public void setDeliverableIds(List<String> deliverableIds) {
            this.deliverableIds = deliverableIds;
        }
    }

    public static class ProjectMilestone {
        private String id;
        private String title;
        private String date;

        public void normalize() {
            getId();
            setTitle(getTitle());
            setDate(getDate());
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

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title == null ? "" : title;
        }

        public String getDate() {
            return date == null ? "" : date;
        }

        public void setDate(String date) {
            this.date = date == null ? "" : date;
        }
    }

    public static class ProjectDeliverable {
        private String id;
        private String stageId;
        private String name;
        private String relativePath;
        private String fileType;
        private String description;

        public void normalize() {
            getId();
            setStageId(getStageId());
            setName(getName());
            setRelativePath(getRelativePath());
            setFileType(getFileType());
            setDescription(getDescription());
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

        public String getStageId() {
            return stageId == null ? "" : stageId;
        }

        public void setStageId(String stageId) {
            this.stageId = stageId == null ? "" : stageId;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name == null ? "" : name;
        }

        public String getRelativePath() {
            return relativePath == null ? "" : relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath == null ? "" : relativePath;
        }

        public String getFileType() {
            return fileType == null ? "" : fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType == null ? "" : fileType;
        }

        public String getDescription() {
            return description == null ? "" : description;
        }

        public void setDescription(String description) {
            this.description = description == null ? "" : description;
        }
    }
}
