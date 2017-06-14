package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.yanghang.clipboard.R;

/**
 * Created by young on 2016/3/22 0022.
 */
public class AlwaysMarqueeTextView extends TextView implements View.OnClickListener {

    public boolean isStarting = true;//是否开始滚动
    private float textLength = 0f;//文本长度
    private float viewWidth = 0f;
    private float step = 0f;//文字的横坐标
    private float y = 0f;//文字的纵坐标
    private float temp_view_plus_text_length = 0.0f;//用于计算的临时变量
    private float temp_view_plus_two_text_length = 0.0f;//用于计算的临时变量
    private Paint paint = null;//绘图样式
    private String text = "";//文本内容

    public AlwaysMarqueeTextView(Context context) {
        super(context);
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {


        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        viewWidth = getWidth();
        int width = 0;
//
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            width = widthSize;
        }
//        Log.v(MainFormActivity.TAG,"  onMeasure width: "+width);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    public void init(WindowManager windowManager) {
        postInvalidate();
        paint = getPaint();
        paint.setColor(getResources().getColor(R.color.cardview_shadow_start_color));
        text = getText().toString();
        textLength = paint.measureText(text);

//        Log.v(MainFormActivity.TAG,"init  viewWidth: "+viewWidth);
//        if (viewWidth == 0) {
//            if (windowManager != null) {
//                Point p = new Point();
//                Display display = windowManager.getDefaultDisplay();
//                display.getSize(p);
//                viewWidth = p.x;
//
//            }
//        }
        step = 0;
//        temp_view_plus_text_length = viewWidth + textLength;
//        temp_view_plus_two_text_length = viewWidth + textLength * 2;
        y = getTextSize() + getPaddingTop();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.step = step;
        ss.isStarting = isStarting;

        return ss;

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        step = ss.step;
        isStarting = ss.isStarting;
    }

    public void startScroll() {
//        Log.v(MainFormActivity.TAG,"  startScroll: viewWidth"+viewWidth);
        if (textLength < getWidth())
            return;
        if (isStarting) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isStarting) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        postInvalidate();

                    }

                }
            }).start();
        }

    }

    public void stopScroll() {
        isStarting = false;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
//        Log.v(MainFormActivity.TAG,"  onDraw viewWidth: "+viewWidth);
        if (textLength < getWidth()) {
            canvas.drawText(text, 0, y, paint);
            return;
        } else
            canvas.drawText(text, step, y, paint);
        if (!isStarting) {
            return;
        }
        step -= 5;//文字滚动速度。
        if (-step > textLength)
            step = getWidth();

    }

    @Override
    public void onClick(View v) {
        isStarting = !isStarting;
        if (isStarting)
            stopScroll();
        else
            startScroll();

    }

    public static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
        };
        public boolean isStarting = false;
        public float step = 0.0f;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            boolean[] b = null;
            in.readBooleanArray(b);
            if (b != null && b.length > 0)
                isStarting = b[0];
            step = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBooleanArray(new boolean[]{isStarting});
            out.writeFloat(step);
        }
    }


}