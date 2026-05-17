package com.example.yanghang.clipboard.OthersView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.example.yanghang.clipboard.Fragment.JsonData.ProjectData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectGanttView extends View {
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;

    private ProjectData projectData;
    private Paint textPaint;
    private Paint smallTextPaint;
    private Paint linePaint;
    private Paint fillPaint;
    private RectF tempRect = new RectF();
    private Path tempPath = new Path();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat labelFormat = new SimpleDateFormat("MM-dd");

    public ProjectGanttView(Context context) {
        super(context);
        init();
    }

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.rgb(38, 45, 56));
        textPaint.setTextSize(dp(13));

        smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallTextPaint.setColor(Color.rgb(118, 126, 140));
        smallTextPaint.setTextSize(dp(11));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.rgb(224, 228, 236));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(1));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
    }

    public void setProjectData(ProjectData projectData) {
        this.projectData = ProjectData.normalize(projectData);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rowCount = projectData == null ? 1 : Math.max(1, projectData.getStages().size());
        int width = resolveSize(dp(980), widthMeasureSpec);
        int height = dp(72) + rowCount * dp(56) + dp(28);
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (projectData == null) {
            return;
        }
        List<ProjectData.ProjectStage> stages = projectData.getStages();
        if (stages.size() == 0) {
            drawEmpty(canvas);
            return;
        }

        DateRange range = findRange(projectData);
        int leftWidth = dp(128);
        int top = dp(50);
        int rowHeight = dp(56);
        int rightPad = dp(22);
        int timelineWidth = Math.max(dp(240), getWidth() - leftWidth - rightPad);
        int totalDays = Math.max(1, range.days());

        drawHeader(canvas, range, leftWidth, timelineWidth, totalDays);
        drawMilestones(canvas, projectData.getMilestones(), range.start, leftWidth, timelineWidth, totalDays, top);

        Map<String, BarInfo> barInfoMap = new HashMap<String, BarInfo>();
        for (int i = 0; i < stages.size(); i++) {
            ProjectData.ProjectStage stage = stages.get(i);
            int rowTop = top + i * rowHeight;
            BarInfo barInfo = drawRow(canvas, stage, rowTop, rowHeight, leftWidth, timelineWidth, range.start, totalDays);
            barInfoMap.put(stage.getId(), barInfo);
        }
        drawDependencies(canvas, stages, barInfoMap);
    }

    private void drawEmpty(Canvas canvas) {
        smallTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("暂无阶段，点击右上角“阶段”添加项目计划", getWidth() / 2f, getHeight() / 2f, smallTextPaint);
        smallTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawHeader(Canvas canvas, DateRange range, int leftWidth, int timelineWidth, int totalDays) {
        smallTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("阶段", dp(12), dp(24), smallTextPaint);
        int step = Math.max(1, totalDays / 4);
        for (int day = 0; day <= totalDays; day += step) {
            float x = leftWidth + timelineWidth * day / (float) totalDays;
            canvas.drawLine(x, dp(30), x, getHeight() - dp(8), linePaint);
            canvas.drawText(labelFormat.format(addDays(range.start, day)), x + dp(3), dp(24), smallTextPaint);
        }
        canvas.drawLine(leftWidth, dp(32), getWidth() - dp(12), dp(32), linePaint);
    }

    private void drawMilestones(Canvas canvas, List<ProjectData.ProjectMilestone> milestones,
                                Date rangeStart, int leftWidth, int timelineWidth, int totalDays, int contentTop) {
        if (milestones == null || milestones.size() == 0) {
            return;
        }
        for (int i = 0; i < milestones.size(); i++) {
            ProjectData.ProjectMilestone milestone = milestones.get(i);
            Date date = parseDate(milestone.getDate(), null);
            if (date == null) {
                continue;
            }
            int day = daysBetween(rangeStart, date);
            if (day < 0 || day > totalDays) {
                continue;
            }
            float x = leftWidth + timelineWidth * day / (float) totalDays;
            linePaint.setColor(Color.argb(120, 147, 91, 188));
            linePaint.setStrokeWidth(dp(1));
            canvas.drawLine(x, dp(34), x, getHeight() - dp(12), linePaint);

            fillPaint.setColor(Color.rgb(147, 91, 188));
            tempPath.reset();
            tempPath.moveTo(x, contentTop - dp(12));
            tempPath.lineTo(x + dp(6), contentTop - dp(6));
            tempPath.lineTo(x, contentTop);
            tempPath.lineTo(x - dp(6), contentTop - dp(6));
            tempPath.close();
            canvas.drawPath(tempPath, fillPaint);

            smallTextPaint.setColor(Color.rgb(94, 62, 128));
            drawEllipsized(canvas, milestone.getTitle(), x + dp(8), contentTop - dp(5), dp(120), smallTextPaint);
        }
        linePaint.setColor(Color.rgb(224, 228, 236));
        linePaint.setStrokeWidth(dp(1));
    }

    private BarInfo drawRow(Canvas canvas, ProjectData.ProjectStage stage, int rowTop, int rowHeight,
                            int leftWidth, int timelineWidth, Date rangeStart, int totalDays) {
        float centerY = rowTop + rowHeight / 2f;
        canvas.drawLine(dp(10), rowTop + rowHeight - dp(4), getWidth() - dp(12), rowTop + rowHeight - dp(4), linePaint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.rgb(38, 45, 56));
        drawEllipsized(canvas, stage.getTitle(), dp(12), centerY - dp(4), leftWidth - dp(22), textPaint);
        smallTextPaint.setColor(statusColor(stage.getStatus()));
        canvas.drawText(ProjectData.getStatusText(stage.getStatus()) + " " + stage.getProgress() + "%", dp(12), centerY + dp(15), smallTextPaint);

        Date start = parseDate(stage.getStartDate(), rangeStart);
        Date end = parseDate(stage.getEndDate(), start);
        int startDay = Math.max(0, daysBetween(rangeStart, start));
        int endDay = Math.max(startDay + 1, daysBetween(rangeStart, end) + 1);
        float x = leftWidth + timelineWidth * startDay / (float) totalDays;
        float right = leftWidth + timelineWidth * Math.min(totalDays, endDay) / (float) totalDays;
        if (right - x < dp(26)) {
            right = x + dp(26);
        }

        fillPaint.setColor(lightColor(statusColor(stage.getStatus())));
        tempRect.set(x, centerY - dp(10), right, centerY + dp(10));
        canvas.drawRoundRect(tempRect, dp(8), dp(8), fillPaint);

        float progressRight = x + (right - x) * stage.getProgress() / 100f;
        fillPaint.setColor(statusColor(stage.getStatus()));
        tempRect.set(x, centerY - dp(10), Math.max(x + dp(3), progressRight), centerY + dp(10));
        canvas.drawRoundRect(tempRect, dp(8), dp(8), fillPaint);

        smallTextPaint.setColor(Color.rgb(80, 86, 96));
        canvas.drawText(stage.getStartDate() + " - " + stage.getEndDate(), x, centerY + dp(28), smallTextPaint);

        BarInfo barInfo = new BarInfo();
        barInfo.left = x;
        barInfo.right = right;
        barInfo.centerY = centerY;
        return barInfo;
    }

    private void drawDependencies(Canvas canvas, List<ProjectData.ProjectStage> stages, Map<String, BarInfo> barInfoMap) {
        linePaint.setColor(Color.argb(170, 100, 106, 120));
        linePaint.setStrokeWidth(dp(1));
        for (int i = 0; i < stages.size(); i++) {
            ProjectData.ProjectStage stage = stages.get(i);
            BarInfo to = barInfoMap.get(stage.getId());
            if (to == null) {
                continue;
            }
            for (int j = 0; j < stage.getDependsOn().size(); j++) {
                BarInfo from = barInfoMap.get(stage.getDependsOn().get(j));
                if (from == null) {
                    continue;
                }
                drawDependencyArrow(canvas, from.right, from.centerY, to.left, to.centerY);
            }
        }
        linePaint.setColor(Color.rgb(224, 228, 236));
        linePaint.setStrokeWidth(dp(1));
    }

    private void drawDependencyArrow(Canvas canvas, float startX, float startY, float endX, float endY) {
        float gap = dp(8);
        float x1 = startX + gap;
        float x2 = endX - gap;
        if (x2 < x1 + dp(16)) {
            x2 = (startX + endX) / 2f;
        }
        tempPath.reset();
        tempPath.moveTo(startX, startY);
        tempPath.lineTo(x1, startY);
        tempPath.lineTo(x2, endY);
        tempPath.lineTo(endX, endY);
        canvas.drawPath(tempPath, linePaint);

        fillPaint.setColor(Color.argb(170, 100, 106, 120));
        tempPath.reset();
        tempPath.moveTo(endX, endY);
        tempPath.lineTo(endX - dp(6), endY - dp(4));
        tempPath.lineTo(endX - dp(6), endY + dp(4));
        tempPath.close();
        canvas.drawPath(tempPath, fillPaint);
    }

    private DateRange findRange(ProjectData data) {
        Date start = null;
        Date end = null;
        List<ProjectData.ProjectStage> stages = data.getStages();
        for (int i = 0; i < stages.size(); i++) {
            ProjectData.ProjectStage stage = stages.get(i);
            Date stageStart = parseDate(stage.getStartDate(), null);
            Date stageEnd = parseDate(stage.getEndDate(), stageStart);
            if (stageStart != null && (start == null || stageStart.before(start))) {
                start = stageStart;
            }
            if (stageEnd != null && (end == null || stageEnd.after(end))) {
                end = stageEnd;
            }
        }
        List<ProjectData.ProjectMilestone> milestones = data.getMilestones();
        for (int i = 0; i < milestones.size(); i++) {
            Date milestoneDate = parseDate(milestones.get(i).getDate(), null);
            if (milestoneDate == null) {
                continue;
            }
            if (start == null || milestoneDate.before(start)) {
                start = milestoneDate;
            }
            if (end == null || milestoneDate.after(end)) {
                end = milestoneDate;
            }
        }
        if (start == null) {
            start = new Date();
        }
        if (end == null || end.before(start)) {
            end = addDays(start, 7);
        }
        return new DateRange(start, end);
    }

    private Date parseDate(String value, Date fallback) {
        if (value == null || value.trim().equals("")) {
            return fallback;
        }
        try {
            return dateFormat.parse(value.trim());
        } catch (ParseException e) {
            return fallback;
        }
    }

    private int daysBetween(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        return (int) ((end.getTime() - start.getTime()) / DAY_MILLIS);
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
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

    private int lightColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.rgb((r + 255 * 4) / 5, (g + 255 * 4) / 5, (b + 255 * 4) / 5);
    }

    private void drawEllipsized(Canvas canvas, String text, float x, float y, float maxWidth, Paint paint) {
        if (text == null) {
            text = "";
        }
        String value = text;
        while (value.length() > 1 && paint.measureText(value) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        if (!value.equals(text) && value.length() > 1) {
            value = value.substring(0, value.length() - 1) + "...";
        }
        canvas.drawText(value, x, y, paint);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    private static class DateRange {
        Date start;
        Date end;

        DateRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        int days() {
            long diff = end.getTime() - start.getTime();
            int days = (int) (diff / DAY_MILLIS) + 1;
            return Math.max(1, days);
        }
    }

    private static class BarInfo {
        float left;
        float right;
        float centerY;
    }
}
