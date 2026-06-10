package com.example.yanghang.clipboard.OthersView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarTrendView extends View {
    public static final int MODE_BAR = 0;
    public static final int MODE_LINE = 1;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final DecimalFormat valueFormat = new DecimalFormat("0.#");
    private final List<String> labels = new ArrayList<>();
    private final List<Float> values = new ArrayList<>();
    private String title = "";
    private float maxValue = 1f;
    private int chartMode = MODE_BAR;
    private boolean alwaysShowValueLabels = false;
    private int selectedIndex = -1;

    public CalendarTrendView(Context context) {
        super(context);
        init();
    }

    public CalendarTrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarTrendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setColor(Color.rgb(95, 103, 118));
        textPaint.setTextSize(dp(11));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setData(Map<String, Float> valueMap, List<String> xLabels, String chartTitle) {
        labels.clear();
        values.clear();
        title = chartTitle == null ? "" : chartTitle;
        maxValue = 1f;
        if (xLabels != null) {
            for (String label : xLabels) {
                labels.add(label);
                Float value = valueMap == null ? null : valueMap.get(label);
                float safeValue = value == null ? 0f : Math.max(0f, value);
                values.add(safeValue);
                if (safeValue > maxValue) {
                    maxValue = safeValue;
                }
            }
        }
        if (maxValue <= 0f) {
            maxValue = 1f;
        }
        if (selectedIndex >= values.size()) {
            selectedIndex = -1;
        }
        invalidate();
    }

    public void setChartMode(int chartMode) {
        this.chartMode = chartMode == MODE_LINE ? MODE_LINE : MODE_BAR;
        invalidate();
    }

    public void setAlwaysShowValueLabels(boolean alwaysShowValueLabels) {
        this.alwaysShowValueLabels = alwaysShowValueLabels;
        if (alwaysShowValueLabels) {
            selectedIndex = -1;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (values.isEmpty()) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            selectedIndex = findNearestIndex(event.getX(), event.getY());
            invalidate();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            selectedIndex = -1;
            invalidate();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        drawBackground(canvas, width, height);
        if (values.isEmpty()) {
            drawEmpty(canvas, width, height);
            return;
        }
        int left = dp(36);
        int right = dp(18);
        int top = dp(38);
        int bottom = dp(42);
        int chartWidth = Math.max(1, width - left - right);
        int chartHeight = Math.max(1, height - top - bottom);
        drawGrid(canvas, left, top, chartWidth, chartHeight);
        if (chartMode == MODE_LINE) {
            drawLine(canvas, left, top, chartWidth, chartHeight);
        } else {
            drawBars(canvas, left, top, chartWidth, chartHeight);
        }
        drawLabels(canvas, left, top, chartWidth, chartHeight);
        if (alwaysShowValueLabels) {
            drawAllValueLabels(canvas, left, top, chartWidth, chartHeight);
        } else {
            drawSelectedValueLabel(canvas, left, top, chartWidth, chartHeight);
        }
    }

    private void drawBackground(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(0, 0, width, height), dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.rgb(230, 235, 244));
        canvas.drawRoundRect(new RectF(dp(0.5f), dp(0.5f), width - dp(0.5f), height - dp(0.5f)), dp(14), dp(14), paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(34, 42, 56));
        textPaint.setColor(Color.rgb(34, 42, 56));
        textPaint.setTextSize(dp(15));
        textPaint.setFakeBoldText(true);
        canvas.drawText(title.length() == 0 ? "趋势" : title, dp(18), dp(25), textPaint);
        textPaint.setFakeBoldText(false);
    }

    private void drawEmpty(Canvas canvas, int width, int height) {
        textPaint.setColor(Color.rgb(145, 153, 166));
        textPaint.setTextSize(dp(13));
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("还没有可以统计的数据", width / 2f, height / 2f, textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGrid(Canvas canvas, int left, int top, int chartWidth, int chartHeight) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.rgb(236, 240, 247));
        for (int i = 0; i <= 4; i++) {
            float y = top + chartHeight * i / 4f;
            canvas.drawLine(left, y, left + chartWidth, y, paint);
        }
    }

    private void drawBars(Canvas canvas, int left, int top, int chartWidth, int chartHeight) {
        int count = values.size();
        float step = count > 1 ? chartWidth * 1f / (count - 1) : chartWidth;
        float barSlot = count > 0 ? chartWidth * 1f / count : chartWidth;
        float barWidth = Math.max(dp(5), Math.min(dp(18), barSlot * 0.38f));

        for (int i = 0; i < count; i++) {
            float x = count > 1 ? left + step * i : left + chartWidth / 2f;
            float value = values.get(i);
            float y = top + chartHeight - chartHeight * value / maxValue;
            float barLeft = x - barWidth / 2f;
            float barRight = x + barWidth / 2f;
            if (value <= 0f) {
                continue;
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(47, 128, 237));
            canvas.drawRoundRect(new RectF(barLeft, y, barRight, top + chartHeight), dp(8), dp(8), paint);
        }
    }

    private void drawLine(Canvas canvas, int left, int top, int chartWidth, int chartHeight) {
        int count = values.size();
        float step = count > 1 ? chartWidth * 1f / (count - 1) : chartWidth;
        Path linePath = new Path();
        boolean hasLinePoint = false;

        for (int i = 0; i < count; i++) {
            if (values.get(i) <= 0f) {
                continue;
            }
            float x = count > 1 ? left + step * i : left + chartWidth / 2f;
            float y = top + chartHeight - chartHeight * values.get(i) / maxValue;
            if (!hasLinePoint) {
                linePath.moveTo(x, y);
                hasLinePoint = true;
            } else {
                linePath.lineTo(x, y);
            }
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2.2f));
        paint.setColor(Color.rgb(9, 183, 99));
        canvas.drawPath(linePath, paint);

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < count; i++) {
            if (values.get(i) <= 0f) {
                continue;
            }
            float x = count > 1 ? left + step * i : left + chartWidth / 2f;
            float y = top + chartHeight - chartHeight * values.get(i) / maxValue;
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dp(4.5f), paint);
            paint.setColor(Color.rgb(9, 183, 99));
            canvas.drawCircle(x, y, dp(3), paint);
        }
    }

    private void drawLabels(Canvas canvas, int left, int top, int chartWidth, int chartHeight) {
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Color.rgb(117, 126, 140));
        textPaint.setTextAlign(Paint.Align.CENTER);
        int count = labels.size();
        if (count == 0) {
            textPaint.setTextAlign(Paint.Align.LEFT);
            return;
        }
        int labelStep = Math.max(1, count / 6);
        float step = count > 1 ? chartWidth * 1f / (count - 1) : chartWidth;
        for (int i = 0; i < count; i += labelStep) {
            float x = count > 1 ? left + step * i : left + chartWidth / 2f;
            canvas.drawText(labels.get(i), x, top + chartHeight + dp(24), textPaint);
        }
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            float y = top + chartHeight * i / 4f;
            float axisValue = maxValue - maxValue * i / 4f;
            canvas.drawText(formatCompactValue(axisValue), left - dp(8), y + dp(4), textPaint);
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawAllValueLabels(Canvas canvas, int left, int top, int chartWidth, int chartHeight) {
        for (int i = 0; i < values.size(); i++) {
            if (shouldSkipValueLabel(i)) {
                continue;
            }
            float pointX = getPointX(i, left, chartWidth);
            float pointY = getPointY(i, top, chartHeight);
            drawValueBubble(canvas, formatCompactValue(values.get(i)), pointX, pointY, false);
        }
    }

    private void drawSelectedValueLabel(Canvas canvas, int left, int top, int chartWidth, int chartHeight) {
        if (selectedIndex < 0 || selectedIndex >= values.size()) {
            return;
        }
        if (shouldSkipValueLabel(selectedIndex)) {
            return;
        }
        float pointX = getPointX(selectedIndex, left, chartWidth);
        float pointY = getPointY(selectedIndex, top, chartHeight);
        String text = labels.get(selectedIndex) + "  " + formatCompactValue(values.get(selectedIndex));
        drawValueBubble(canvas, text, pointX, pointY, true);
    }

    private boolean shouldSkipValueLabel(int index) {
        return chartMode == MODE_LINE && values.get(index) <= 0f;
    }

    private String formatCompactValue(float value) {
        if (Math.abs(value) >= 10000f) {
            return valueFormat.format(value / 10000f) + "w";
        }
        return valueFormat.format(value);
    }

    private void drawValueBubble(Canvas canvas, String text, float anchorX, float anchorY, boolean selected) {
        textPaint.setFakeBoldText(selected);
        textPaint.setTextSize(dp(selected ? 11 : 9));
        textPaint.setTextAlign(Paint.Align.CENTER);
        float paddingX = dp(selected ? 9 : 6);
        float bubbleHeight = dp(selected ? 24 : 18);
        float bubbleWidth = textPaint.measureText(text) + paddingX * 2;
        float bubbleLeft = anchorX - bubbleWidth / 2f;
        float bubbleTop = anchorY - bubbleHeight - dp(8);
        if (bubbleTop < dp(30)) {
            bubbleTop = anchorY + dp(8);
        }
        if (bubbleLeft < dp(4)) {
            bubbleLeft = dp(4);
        } else if (bubbleLeft + bubbleWidth > getWidth() - dp(4)) {
            bubbleLeft = getWidth() - dp(4) - bubbleWidth;
        }

        RectF rectF = new RectF(bubbleLeft, bubbleTop, bubbleLeft + bubbleWidth, bubbleTop + bubbleHeight);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selected ? Color.rgb(47, 128, 237) : Color.rgb(232, 243, 255));
        canvas.drawRoundRect(rectF, dp(9), dp(9), paint);
        if (!selected) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(184, 215, 255));
            canvas.drawRoundRect(rectF, dp(9), dp(9), paint);
        }

        textPaint.setColor(selected ? Color.WHITE : Color.rgb(32, 93, 166));
        canvas.drawText(text, rectF.centerX(), rectF.centerY() + dp(selected ? 4 : 3), textPaint);
        textPaint.setFakeBoldText(false);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private int findNearestIndex(float touchX, float touchY) {
        int count = values.size();
        if (count == 0) {
            return -1;
        }
        int left = dp(36);
        int top = dp(38);
        int chartWidth = Math.max(1, getWidth() - left - dp(18));
        int chartHeight = Math.max(1, getHeight() - top - dp(42));
        if (touchY < top - dp(28) || touchY > top + chartHeight + dp(32)) {
            return -1;
        }
        float step = count > 1 ? chartWidth * 1f / (count - 1) : chartWidth;
        float threshold = Math.max(dp(24), step / 2f);
        int nearestIndex = -1;
        float nearestDistance = Float.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            if (chartMode == MODE_LINE && values.get(i) <= 0f) {
                continue;
            }
            float distance = Math.abs(touchX - getPointX(i, left, chartWidth));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestDistance <= threshold ? nearestIndex : -1;
    }

    private float getPointX(int index, int left, int chartWidth) {
        int count = values.size();
        if (count <= 1) {
            return left + chartWidth / 2f;
        }
        return left + chartWidth * index / (count - 1f);
    }

    private float getPointY(int index, int top, int chartHeight) {
        return top + chartHeight - chartHeight * values.get(index) / maxValue;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
