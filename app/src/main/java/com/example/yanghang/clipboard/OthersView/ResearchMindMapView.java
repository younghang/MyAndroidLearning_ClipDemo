package com.example.yanghang.clipboard.OthersView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.yanghang.clipboard.Fragment.JsonData.ResearchTopicData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResearchMindMapView extends View {
    private static final float MIN_SCALE = 0.18f;
    private static final float MAX_SCALE = 4.0f;
    private static final float DOUBLE_TAP_ZOOM_FACTOR = 1.55f;

    public interface OnNodeClickListener {
        void onNodeClick(ResearchTopicData.ResearchNode node, int index);

        void onNodeLongClick(ResearchTopicData.ResearchNode node, int index);

        void onNodePositionChanged(ResearchTopicData.ResearchNode node, int index);
    }

    private ResearchTopicData topicData;
    private OnNodeClickListener onNodeClickListener;
    private Paint fillPaint;
    private Paint strokePaint;
    private Paint textPaint;
    private Paint smallTextPaint;
    private Paint linePaint;
    private RectF tempRect = new RectF();
    private Path tempPath = new Path();
    private List<NodeBox> nodeBoxes = new ArrayList<NodeBox>();
    private Map<String, NodeBox> nodeBoxMap = new HashMap<String, NodeBox>();
    private NodeBox rootBox;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float scale = 1.0f;
    private float fitScale = 1.0f;
    private float offsetX = 0;
    private float offsetY = 0;
    private boolean transformReady = false;
    private boolean layoutDirty = true;
    private boolean longPressHandled = false;
    private boolean pendingViewportRestore = false;
    private boolean nodeDragEnabled = false;
    private boolean nodeDragging = false;
    private NodeBox pressedNodeBox;
    private float downX;
    private float downY;
    private float dragOffsetX;
    private float dragOffsetY;
    private float pendingViewportScale;
    private float pendingViewportOffsetX;
    private float pendingViewportOffsetY;
    private float mapWidth = 0;
    private float mapHeight = 0;

    public ResearchMindMapView(Context context) {
        super(context);
        init();
    }

    private void init() {
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dp(1.2f));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.rgb(46, 46, 46));
        textPaint.setTextSize(dp(14));

        smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallTextPaint.setColor(Color.rgb(120, 120, 120));
        smallTextPaint.setTextSize(dp(11));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.rgb(170, 170, 170));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(1.4f));

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                longPressHandled = false;
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (nodeDragEnabled && pressedNodeBox != null) {
                    return true;
                }
                if (!scaleGestureDetector.isInProgress()) {
                    offsetX -= distanceX;
                    offsetY -= distanceY;
                    invalidate();
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (longPressHandled) {
                    return true;
                }
                NodeBox nodeBox = findNodeAt(e.getX(), e.getY());
                if (nodeBox != null && onNodeClickListener != null) {
                    onNodeClickListener.onNodeClick(nodeBox.node, nodeBox.index);
                    return true;
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (scaleGestureDetector.isInProgress() || nodeDragging) {
                    return;
                }
                NodeBox nodeBox = findNodeAt(e.getX(), e.getY());
                if (nodeBox != null && onNodeClickListener != null) {
                    longPressHandled = true;
                    onNodeClickListener.onNodeLongClick(nodeBox.node, nodeBox.index);
                }
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (findNodeAt(e.getX(), e.getY()) != null) {
                    return true;
                }
                zoomBlankAreaAt(e.getX(), e.getY());
                return true;
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float oldScale = scale;
                float newScale = scale * detector.getScaleFactor();
                setScaleAroundPoint(newScale, detector.getFocusX(), detector.getFocusY(), oldScale);
                invalidate();
                return true;
            }
        });
    }

    public void setTopicData(ResearchTopicData topicData) {
        this.topicData = ResearchTopicData.normalize(topicData);
        layoutDirty = true;
        transformReady = false;
        invalidate();
    }

    public void setOnNodeClickListener(OnNodeClickListener onNodeClickListener) {
        this.onNodeClickListener = onNodeClickListener;
    }

    public void setNodeDragEnabled(boolean nodeDragEnabled) {
        this.nodeDragEnabled = nodeDragEnabled;
    }

    public boolean hasViewport() {
        return transformReady;
    }

    public float getViewportScale() {
        return scale;
    }

    public float getViewportOffsetX() {
        return offsetX;
    }

    public float getViewportOffsetY() {
        return offsetY;
    }

    public void restoreViewport(float scale, float offsetX, float offsetY) {
        if (scale < MIN_SCALE) {
            scale = MIN_SCALE;
        } else if (scale > MAX_SCALE) {
            scale = MAX_SCALE;
        }
        pendingViewportScale = scale;
        pendingViewportOffsetX = offsetX;
        pendingViewportOffsetY = offsetY;
        pendingViewportRestore = true;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutDirty = true;
        transformReady = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (topicData == null) {
            return;
        }
        if (layoutDirty) {
            buildLayout();
        }
        if (pendingViewportRestore) {
            applyPendingViewport();
        }
        if (!transformReady) {
            resetTransform();
        }

        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        drawEdges(canvas);
        drawRoot(canvas);
        drawNodes(canvas);
        canvas.restore();

        drawHint(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(event.getAction() != MotionEvent.ACTION_UP
                    && event.getAction() != MotionEvent.ACTION_CANCEL);
        }
        scaleGestureDetector.onTouchEvent(event);
        if (handleNodeDrag(event)) {
            return true;
        }
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private boolean handleNodeDrag(MotionEvent event) {
        if (!nodeDragEnabled || scaleGestureDetector.isInProgress()) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                clearNodeDragState();
            }
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressedNodeBox = findNodeAt(event.getX(), event.getY());
                nodeDragging = false;
                downX = event.getX();
                downY = event.getY();
                if (pressedNodeBox != null) {
                    float worldX = (event.getX() - offsetX) / scale;
                    float worldY = (event.getY() - offsetY) / scale;
                    dragOffsetX = worldX - pressedNodeBox.rect.left;
                    dragOffsetY = worldY - pressedNodeBox.rect.top;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (pressedNodeBox == null) {
                    return false;
                }
                if (!nodeDragging) {
                    float dx = Math.abs(event.getX() - downX);
                    float dy = Math.abs(event.getY() - downY);
                    if (dx < dp(5) && dy < dp(5)) {
                        return false;
                    }
                    nodeDragging = true;
                    longPressHandled = true;
                }
                movePressedNode(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (nodeDragging && pressedNodeBox != null && onNodeClickListener != null) {
                    onNodeClickListener.onNodePositionChanged(pressedNodeBox.node, pressedNodeBox.index);
                }
                boolean handled = nodeDragging;
                clearNodeDragState();
                return handled;
            default:
                return false;
        }
    }

    private void movePressedNode(float screenX, float screenY) {
        if (pressedNodeBox == null) {
            return;
        }
        float worldX = (screenX - offsetX) / scale;
        float worldY = (screenY - offsetY) / scale;
        float left = worldX - dragOffsetX;
        float top = worldY - dragOffsetY;
        pressedNodeBox.rect.offsetTo(left, top);
        pressedNodeBox.node.setMapPositioned(true);
        pressedNodeBox.node.setMapX(left);
        pressedNodeBox.node.setMapY(top);
        updateMapBounds();
        invalidate();
    }

    private void clearNodeDragState() {
        pressedNodeBox = null;
        nodeDragging = false;
    }

    private void applyPendingViewport() {
        scale = pendingViewportScale;
        offsetX = pendingViewportOffsetX;
        offsetY = pendingViewportOffsetY;
        pendingViewportRestore = false;
        transformReady = true;
    }

    private void zoomBlankAreaAt(float focusX, float focusY) {
        float targetScale;
        if (scale >= MAX_SCALE * 0.96f) {
            targetScale = fitScale;
        } else {
            targetScale = Math.min(MAX_SCALE, scale * DOUBLE_TAP_ZOOM_FACTOR);
        }
        setScaleAroundPoint(targetScale, focusX, focusY, scale);
        invalidate();
    }

    private void setScaleAroundPoint(float targetScale, float focusX, float focusY, float oldScale) {
        if (targetScale < MIN_SCALE) {
            targetScale = MIN_SCALE;
        } else if (targetScale > MAX_SCALE) {
            targetScale = MAX_SCALE;
        }
        if (oldScale <= 0) {
            oldScale = scale;
        }
        float worldX = (focusX - offsetX) / oldScale;
        float worldY = (focusY - offsetY) / oldScale;
        scale = targetScale;
        offsetX = focusX - worldX * scale;
        offsetY = focusY - worldY * scale;
    }

    private void buildLayout() {
        nodeBoxes.clear();
        nodeBoxMap.clear();
        rootBox = new NodeBox();
        rootBox.index = -1;
        rootBox.color = Color.rgb(63, 81, 181);
        rootBox.rect.set(dp(24), dp(24), dp(224), dp(104));

        List<ResearchTopicData.ResearchNode> nodes = topicData.getNodes();
        Map<String, ResearchTopicData.ResearchNode> nodeMap = new HashMap<String, ResearchTopicData.ResearchNode>();
        for (int i = 0; i < nodes.size(); i++) {
            ResearchTopicData.ResearchNode node = nodes.get(i);
            if (node != null) {
                nodeMap.put(node.getId(), node);
            }
        }

        Map<String, Integer> levelMap = new HashMap<String, Integer>();
        int maxLevel = 1;
        for (int i = 0; i < nodes.size(); i++) {
            ResearchTopicData.ResearchNode node = nodes.get(i);
            if (node == null) {
                continue;
            }
            int level = getNodeLevel(node, nodeMap, levelMap, new HashSet<String>());
            if (level > maxLevel) {
                maxLevel = level;
            }
        }

        List<List<NodeBox>> columns = new ArrayList<List<NodeBox>>();
        for (int i = 0; i <= maxLevel; i++) {
            columns.add(new ArrayList<NodeBox>());
        }
        for (int i = 0; i < nodes.size(); i++) {
            ResearchTopicData.ResearchNode node = nodes.get(i);
            if (node == null) {
                continue;
            }
            NodeBox box = new NodeBox();
            box.node = node;
            box.index = i;
            box.color = typeColor(node.getType());
            Integer level = levelMap.get(node.getId());
            box.level = level == null ? 1 : level;
            columns.get(box.level).add(box);
            nodeBoxes.add(box);
            nodeBoxMap.put(node.getId(), box);
        }

        float nodeWidth = dp(190);
        float nodeHeight = dp(76);
        float columnGap = dp(88);
        float rowGap = dp(24);
        float rootRight = rootBox.rect.right;
        int maxRows = 1;
        for (int i = 1; i < columns.size(); i++) {
            if (columns.get(i).size() > maxRows) {
                maxRows = columns.get(i).size();
            }
        }
        mapHeight = Math.max(dp(220), maxRows * nodeHeight + Math.max(0, maxRows - 1) * rowGap + dp(48));
        rootBox.rect.offsetTo(dp(24), (mapHeight - rootBox.rect.height()) / 2);

        List<NodeBox> placedBoxes = new ArrayList<NodeBox>();
        placedBoxes.add(rootBox);
        for (int level = 1; level < columns.size(); level++) {
            List<NodeBox> column = columns.get(level);
            float columnHeight = column.size() * nodeHeight + Math.max(0, column.size() - 1) * rowGap;
            float y = (mapHeight - columnHeight) / 2;
            float x = rootRight + columnGap + (level - 1) * (nodeWidth + columnGap);
            for (int i = 0; i < column.size(); i++) {
                NodeBox box = column.get(i);
                box.rect.set(x, y, x + nodeWidth, y + nodeHeight);
                if (box.node.isMapPositioned()) {
                    box.rect.offsetTo(box.node.getMapX(), box.node.getMapY());
                } else {
                    moveBoxToOpenSlot(box, placedBoxes, rowGap);
                }
                placedBoxes.add(box);
                y += nodeHeight + rowGap;
            }
        }
        mapWidth = rootRight + columnGap + maxLevel * nodeWidth + Math.max(0, maxLevel - 1) * columnGap + dp(40);
        updateMapBounds();
        layoutDirty = false;
    }

    private void moveBoxToOpenSlot(NodeBox box, List<NodeBox> placedBoxes, float rowGap) {
        if (box == null || placedBoxes == null) {
            return;
        }
        int guard = 0;
        while (hasBoxOverlap(box, placedBoxes) && guard < 80) {
            box.rect.offset(0, box.rect.height() + rowGap);
            guard++;
        }
    }

    private boolean hasBoxOverlap(NodeBox box, List<NodeBox> placedBoxes) {
        RectF expanded = new RectF(box.rect);
        float margin = dp(12);
        expanded.inset(-margin, -margin);
        for (int i = 0; i < placedBoxes.size(); i++) {
            NodeBox placed = placedBoxes.get(i);
            if (placed != null && RectF.intersects(expanded, placed.rect)) {
                return true;
            }
        }
        return false;
    }

    private void updateMapBounds() {
        float right = rootBox == null ? dp(260) : rootBox.rect.right;
        float bottom = rootBox == null ? dp(160) : rootBox.rect.bottom;
        for (int i = 0; i < nodeBoxes.size(); i++) {
            NodeBox box = nodeBoxes.get(i);
            if (box.rect.right > right) {
                right = box.rect.right;
            }
            if (box.rect.bottom > bottom) {
                bottom = box.rect.bottom;
            }
        }
        mapWidth = Math.max(mapWidth, right + dp(40));
        mapHeight = Math.max(mapHeight, bottom + dp(40));
    }

    private int getNodeLevel(ResearchTopicData.ResearchNode node,
                             Map<String, ResearchTopicData.ResearchNode> nodeMap,
                             Map<String, Integer> levelMap,
                             Set<String> visiting) {
        Integer cached = levelMap.get(node.getId());
        if (cached != null) {
            return cached;
        }
        if (visiting.contains(node.getId())) {
            return 1;
        }
        visiting.add(node.getId());
        int level = 1;
        for (int i = 0; i < node.getRelatedNodeIds().size(); i++) {
            ResearchTopicData.ResearchNode related = nodeMap.get(node.getRelatedNodeIds().get(i));
            if (related == null) {
                continue;
            }
            int relatedLevel = getNodeLevel(related, nodeMap, levelMap, visiting);
            if (relatedLevel + 1 > level) {
                level = relatedLevel + 1;
            }
        }
        visiting.remove(node.getId());
        if (level > 6) {
            level = 6;
        }
        levelMap.put(node.getId(), level);
        return level;
    }

    private void resetTransform() {
        float availableWidth = Math.max(1, getWidth() - dp(24));
        fitScale = Math.min(1.0f, availableWidth / Math.max(1, mapWidth));
        if (fitScale < 0.28f) {
            fitScale = 0.28f;
        }
        scale = fitScale;
        offsetX = dp(12);
        offsetY = Math.max(dp(12), (getHeight() - mapHeight * scale) / 2);
        transformReady = true;
    }

    private void drawEdges(Canvas canvas) {
        for (int i = 0; i < nodeBoxes.size(); i++) {
            NodeBox to = nodeBoxes.get(i);
            boolean hasVisibleRelated = false;
            for (int j = 0; j < to.node.getRelatedNodeIds().size(); j++) {
                NodeBox from = nodeBoxMap.get(to.node.getRelatedNodeIds().get(j));
                if (from == null) {
                    continue;
                }
                hasVisibleRelated = true;
                drawCurve(canvas, from.rect.right, from.rect.centerY(), to.rect.left, to.rect.centerY(), from.color);
            }
            if (!hasVisibleRelated) {
                drawCurve(canvas, rootBox.rect.right, rootBox.rect.centerY(), to.rect.left, to.rect.centerY(), to.color);
            }
        }
    }

    private void drawCurve(Canvas canvas, float startX, float startY, float endX, float endY, int color) {
        tempPath.reset();
        float midX = (startX + endX) / 2;
        tempPath.moveTo(startX, startY);
        tempPath.cubicTo(midX, startY, midX, endY, endX, endY);
        linePaint.setColor(adjustAlpha(color, 130));
        canvas.drawPath(tempPath, linePaint);
    }

    private void drawRoot(Canvas canvas) {
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(63, 81, 181));
        canvas.drawRoundRect(rootBox.rect, dp(10), dp(10), fillPaint);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        drawTextLines(canvas, "课题", rootBox.rect.left + dp(14), rootBox.rect.top + dp(24), rootBox.rect.width() - dp(28), textPaint, 1);
        textPaint.setFakeBoldText(false);
        smallTextPaint.setColor(Color.WHITE);
        drawTextLines(canvas, emptyTitle(topicData.getTitle()), rootBox.rect.left + dp(14), rootBox.rect.top + dp(49), rootBox.rect.width() - dp(28), smallTextPaint, 2);
    }

    private void drawNodes(Canvas canvas) {
        for (int i = 0; i < nodeBoxes.size(); i++) {
            NodeBox box = nodeBoxes.get(i);
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.WHITE);
            canvas.drawRoundRect(box.rect, dp(8), dp(8), fillPaint);

            strokePaint.setColor(box.color);
            canvas.drawRoundRect(box.rect, dp(8), dp(8), strokePaint);

            tempRect.set(box.rect.left + dp(10), box.rect.top + dp(8), box.rect.left + dp(58), box.rect.top + dp(26));
            fillPaint.setColor(adjustAlpha(box.color, 34));
            canvas.drawRoundRect(tempRect, dp(6), dp(6), fillPaint);

            smallTextPaint.setColor(box.color);
            smallTextPaint.setFakeBoldText(true);
            drawTextLines(canvas, ResearchTopicData.getTypeText(box.node.getType()), tempRect.left + dp(5), tempRect.top + dp(13), tempRect.width() - dp(10), smallTextPaint, 1);
            smallTextPaint.setFakeBoldText(false);

            textPaint.setColor(Color.rgb(46, 46, 46));
            textPaint.setFakeBoldText(true);
            drawTextLines(canvas, emptyTitle(box.node.getTitle()), box.rect.left + dp(10), box.rect.top + dp(45), box.rect.width() - dp(20), textPaint, 2);
            textPaint.setFakeBoldText(false);
        }
    }

    private void drawHint(Canvas canvas) {
        smallTextPaint.setColor(Color.rgb(140, 140, 140));
        smallTextPaint.setFakeBoldText(false);
        canvas.drawText("拖动查看 · 双指缩放 · 点节点编辑", dp(12), getHeight() - dp(12), smallTextPaint);
    }

    private void drawTextLines(Canvas canvas, String text, float x, float baselineY, float width, Paint paint, int maxLines) {
        if (text == null) {
            text = "";
        }
        String value = text.trim();
        if (value.equals("")) {
            value = "未命名";
        }
        float y = baselineY;
        int line = 0;
        while (value.length() > 0 && line < maxLines) {
            int count = paint.breakText(value, true, width, null);
            if (count <= 0) {
                break;
            }
            String part = value.substring(0, count);
            if (line == maxLines - 1 && count < value.length()) {
                while (paint.measureText(part + "...") > width && part.length() > 1) {
                    part = part.substring(0, part.length() - 1);
                }
                part = part + "...";
            }
            canvas.drawText(part, x, y, paint);
            value = value.substring(count).trim();
            y += paint.getTextSize() + dp(4);
            line++;
        }
    }

    private NodeBox findNodeAt(float screenX, float screenY) {
        float worldX = (screenX - offsetX) / scale;
        float worldY = (screenY - offsetY) / scale;
        for (int i = nodeBoxes.size() - 1; i >= 0; i--) {
            NodeBox box = nodeBoxes.get(i);
            if (box.rect.contains(worldX, worldY)) {
                return box;
            }
        }
        return null;
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

    private int adjustAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private String emptyTitle(String value) {
        if (value == null || value.trim().equals("")) {
            return "未命名";
        }
        return value.trim();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static class NodeBox {
        ResearchTopicData.ResearchNode node;
        int index;
        int level;
        int color;
        RectF rect = new RectF();
    }
}
