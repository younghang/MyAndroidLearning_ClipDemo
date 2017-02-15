package com.example.yanghang.myapplication.OthersView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.yanghang.myapplication.R;


/**
 * Created by yanghang on 2016/12/2.
 */
public class MessageText extends TextView {


    int paddingLeftForTriangle;
    int paddingRightForTriangle;
    float roundRectRadius;
    int paddingInner;
    int paddingTop;
    int width;
    int height;
    int bgcolor;
    float rectLeft;
    float rectRight;
    boolean isYourSide = false;
    private Paint mPaint;


    public MessageText(Context context) {
        this(context, null);
    }

    public MessageText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MessageText, defStyleAttr, 0);
        if (null != a) {
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.MessageText_selfMessage:
                        isYourSide = a.getBoolean(attr, false);
                        break;
                }
            }
            a.recycle();
        }
        init();
    }

    private void init() {


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        Drawable background = getBackground();
        ColorDrawable colorDrawable = (ColorDrawable) background;
        bgcolor = colorDrawable.getColor();
        roundRectRadius = getResources().getDimension(R.dimen.round_rect_corner);

        paddingInner = (int) (roundRectRadius * 0.4);
        paddingTop = (int) (paddingInner + getTextSize() + getPaddingTop());

        if (isYourSide) {
            rectRight = roundRectRadius;
            rectLeft = 0;
            paddingLeftForTriangle = 0;
            paddingRightForTriangle = (int) (roundRectRadius * 1.6);
        } else {
            rectLeft = roundRectRadius;
            rectRight = 0;

            paddingLeftForTriangle = (int) (roundRectRadius * 1.6);
            paddingRightForTriangle = 0;
        }

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
//            Log.v(MainFormActivity.MTTAG, "EXACTLY Height:" + Height);
        } else {

            //作为List 的Item控件的测量模式并非 EXACTLY 和AT_MOST 而是 UNSPECIFIED
                TextPaint tpaint = new TextPaint();
                tpaint.setTextSize(getTextSize());
//                Log.v(MainFormActivity.MTTAG, "Measure text:" + getText().toString());
                float textWidth = tpaint.measureText(getText().toString());
                int Lines = (int) (textWidth / (Width - paddingLeftForTriangle - paddingRightForTriangle - paddingInner - getPaddingLeft() - getPaddingRight()));
                if (Lines > getMaxLines()) {
                    Lines = getMaxLines();
                }
                Height = (int) ((Lines + 1) * (getTextSize() + paddingInner / 2) + getPaddingBottom() + paddingTop + getTextSize() / 2);
//                Log.v(MainFormActivity.MTTAG, "Measure Height:" + Height);

        }
        if (Height < roundRectRadius * 2) {
            Height = (int) roundRectRadius * 3;
        }
//        Log.v(MainFormActivity.MTTAG, "set Height:" + Height);
        setMeasuredDimension(Width, Height);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int beforeColor;
        int afterColor;
        if (isYourSide) {
            beforeColor = getResources().getColor(R.color.message_bg_color);
            afterColor = getResources().getColor(R.color.message_bg_color_dark);
        } else {
            beforeColor = getResources().getColor(R.color.white);
            afterColor = getResources().getColor(R.color.deep_gray);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.v(MainFormActivity.MTTAG, "按下");
                bgcolor = afterColor;
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                bgcolor = beforeColor;
                postInvalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                bgcolor = beforeColor;
                postInvalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void drawBackground(Canvas canvas) {
        width = getWidth();
        height = getHeight();
//        Log.v(MainFormActivity.MTTAG, "draw Height:" + getHeight());
//        Log.v(MainFormActivity.MTTAG, "bgcolor:  " + bgcolor);
        mPaint.setColor(bgcolor);
        mPaint.setStyle(Paint.Style.FILL);

        setBackground(getResources().getDrawable(R.drawable.press_up));

        RectF rectF = new RectF(rectLeft, 0, width - rectRight, height);
        canvas.drawRoundRect(rectF, roundRectRadius, roundRectRadius, mPaint);
        Path tranglePath = new Path();
        mPaint.setPathEffect(new CornerPathEffect(5));
        tranglePath.reset();
        if (isYourSide) {
            tranglePath.moveTo(width - 2.2f * roundRectRadius, 0.6f * roundRectRadius);
            tranglePath.lineTo(width, roundRectRadius);
            tranglePath.lineTo(width - roundRectRadius, 1.4f * roundRectRadius);
        } else {
            tranglePath.moveTo(2.2f * roundRectRadius, 0.6f * roundRectRadius);
            tranglePath.lineTo(0, roundRectRadius);
            tranglePath.lineTo(roundRectRadius, 1.4f * roundRectRadius);
        }

        tranglePath.close();
        canvas.drawPath(tranglePath, mPaint);

        TextPaint tpaint = new TextPaint();
        tpaint.setColor(getTextColors().getDefaultColor());
        tpaint.setTextSize(getTextSize());
        float textWidth = tpaint.measureText(getText().toString());
        int Lines = (int) (textWidth / (width - paddingLeftForTriangle - paddingRightForTriangle - paddingInner - getPaddingLeft() - getPaddingRight()));

        float singleWidth = textWidth / getText().length();
        int singleLineCount = (int) ((width - paddingInner - paddingLeftForTriangle - paddingRightForTriangle - getPaddingLeft() - getPaddingRight()) / singleWidth);
        int end = getText().length();
        String endings = "";
        if (Lines > getMaxLines()) {
            Lines = getMaxLines();
            end = Lines * singleLineCount;
            endings = "....";
        }

//        Log.v(MainFormActivity.MTTAG, " Lines: " + Lines);
        for (int i = 0; i < Lines; i++) {
            canvas.drawText(getText().toString(), i * singleLineCount, (1 + i) * singleLineCount, getPaddingLeft() + paddingLeftForTriangle, paddingTop + getTextSize() / 2 + getTextSize() * i + paddingInner / 2 * i, tpaint);
        }
//        int top = (int) (paddingTop + getTextSize() / 2 + getTextSize() * 1 + paddingInner / 2 * 1);
//        Log.v(MainFormActivity.MTTAG, " marginTop: " + top);
        canvas.drawText(getText().toString(), Lines * singleLineCount, end, paddingLeftForTriangle + getPaddingLeft(), paddingTop + getTextSize() / 2 + getTextSize() * Lines + paddingInner / 2 * Lines, tpaint);
        canvas.drawText(endings, 0, endings.length(), paddingLeftForTriangle + getPaddingLeft() + singleLineCount * singleWidth, paddingTop + getTextSize() / 2 + getTextSize() * (Lines - 1) + paddingInner / 2 * (Lines), tpaint);
    }


}
