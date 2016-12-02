package com.example.yanghang.myapplication.OthersView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.yanghang.myapplication.MainFormActivity;
import com.example.yanghang.myapplication.R;


/**
 * Created by yanghang on 2016/12/2.
 */
public class MessageText extends TextView {

    float roundRectRadius;
    int paddingLeft;
    int paddingTop;
    int padding;
    int width;
    int height;
    int bgcolor;
    private Paint mPaint;

    public MessageText(Context context) {
        this(context, null);
    }

    public MessageText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        Drawable background = getBackground();
        ColorDrawable colorDrawable = (ColorDrawable) background;
        bgcolor = colorDrawable.getColor();
        roundRectRadius = getResources().getDimension(R.dimen.round_rect_corner);
        paddingLeft = (int) (roundRectRadius * 1.6);
        padding = (int) (roundRectRadius * 0.4);
        paddingTop = (int) (padding + getTextSize() + getPaddingTop());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBackground(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int Width;
        int Height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        Width = width;
        if (heightMode == MeasureSpec.EXACTLY) {
            Height = height;
        } else {
            if (heightMode == MeasureSpec.AT_MOST) {
                TextPaint tpaint = new TextPaint();
                tpaint.setTextSize(getTextSize());
                float textWidth = tpaint.measureText(getText().toString());
                int Lines = (int) (textWidth / (Width - paddingLeft - padding));
                if (Lines > getMaxLines()) {
                    Lines = getMaxLines();
                }
                Height = (int) ((Lines) * (getTextSize() + padding / 2) + getPaddingBottom() + paddingTop + getTextSize() / 2);

            } else
                Height = height;
        }
        if (Height < roundRectRadius * 2) {
            Height = (int) roundRectRadius * 3;
        }
        setMeasuredDimension(Width, Height);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v(MainFormActivity.MTTAG, "按下");
                bgcolor = getResources().getColor(R.color.message_bg_color_dark);
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                bgcolor = getResources().getColor(R.color.message_bg_color);
                postInvalidate();
                Log.v(MainFormActivity.MTTAG, "抬起");

        }
        return true;
    }

    private void drawBackground(Canvas canvas) {
        width = getWidth();
        height = getHeight();

//        Log.v(MainFormActivity.MTTAG, "bgcolor:  " + bgcolor);
        mPaint.setColor(bgcolor);
        mPaint.setStyle(Paint.Style.FILL);

        setBackground(getResources().getDrawable(R.drawable.press_up));

        RectF rectF = new RectF(roundRectRadius, 0, width, height);
        canvas.drawRoundRect(rectF, roundRectRadius, roundRectRadius, mPaint);
        Path tranglePath = new Path();
        mPaint.setPathEffect(new CornerPathEffect(5));
        tranglePath.reset();
        tranglePath.moveTo(2.2f * roundRectRadius, 0.6f * roundRectRadius);
        tranglePath.lineTo(0, roundRectRadius);
        tranglePath.lineTo(roundRectRadius, 1.4f * roundRectRadius);
        tranglePath.close();
        canvas.drawPath(tranglePath, mPaint);

        TextPaint tpaint = new TextPaint();
        tpaint.setColor(getTextColors().getDefaultColor());
        tpaint.setTextSize(getTextSize());
        float textWidth = tpaint.measureText(getText().toString());
        int Lines = (int) (textWidth / (width - paddingLeft - padding - getPaddingLeft() - getPaddingRight()));

        float singleWidth = textWidth / getText().length();
        int singleLineCount = (int) ((width - padding - paddingLeft - getPaddingLeft() - getPaddingRight()) / singleWidth);
        int end = getText().length();
        String endings = "";
        if (Lines > getMaxLines()) {
            Lines = getMaxLines();
            end = Lines * singleLineCount;
            endings = "....";
        }
//        Log.v(MainFormActivity.MTTAG, " Lines: " + Lines);
        for (int i = 0; i < Lines; i++) {
            canvas.drawText(getText().toString(), i * singleLineCount, (1 + i) * singleLineCount, paddingLeft, paddingTop + getTextSize() / 2 + getTextSize() * i + padding / 2 * i, tpaint);
        }
        int top = (int) (paddingTop + getTextSize() / 2 + getTextSize() * 1 + padding / 2 * 1);
//        Log.v(MainFormActivity.MTTAG, " marginTop: " + top);
        canvas.drawText(getText().toString(), Lines * singleLineCount, end, paddingLeft, paddingTop + getTextSize() / 2 + getTextSize() * Lines + padding / 2 * Lines, tpaint);
        canvas.drawText(endings, 0, endings.length(), paddingLeft + singleLineCount * singleWidth, paddingTop + getTextSize() / 2 + getTextSize() * (Lines - 1) + padding / 2 * (Lines), tpaint);
    }


}
