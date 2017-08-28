package com.wang.bookloading.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.wang.bookloading.R;
import com.wang.bookloading.ViewUtil;

/**
 * Created on 2017/8/24.
 * Author: wang
 */

public class TextMaskView extends View {

    private static final int DEF_DRAW_SIZE = 100;

    Paint mPaint;

    private int mLineLength;
    private int mLinePadding;
    private int mStrokeWidth;
    private float mPercent;
    private float mLines;

    private int mOneLineLength;
    private int mStartX;
    private int mStartY;


    public TextMaskView(Context context) {
        super(context);
        init(context, null);
    }

    public TextMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TextMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextMaskView);
        mLines = typedArray.getFloat(R.styleable.TextMaskView_tmv_lines, 0f);
        mLinePadding = typedArray.getDimensionPixelSize(R.styleable.TextMaskView_tmv_linePadding, 0);
        mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.TextMaskView_tmv_strokeWidth, 0);
        mPercent = typedArray.getFloat(R.styleable.TextMaskView_tmv_percent, 0f);
        int color = typedArray.getColor(R.styleable.TextMaskView_tmv_color, Color.RED);
        typedArray.recycle();


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(color);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
        if (mLines == 0f) {
            return;
        }
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mStartX = getPaddingLeft();
        mStartY = getPaddingTop();
        mOneLineLength = width - mStartX - getPaddingRight();
        mLineLength = (int) (mOneLineLength * mLines);

        int maxHeight = height - mStartY - getPaddingBottom();

        if (mStrokeWidth <= 0 && mLinePadding <= 0) {
            int count = (int) Math.ceil(mLines);
            mStrokeWidth = mLinePadding = maxHeight / (count * 2 - 1);
        } else if (mStrokeWidth <= 0) {
            int count = (int) Math.ceil(mLines);
            if (count > 1) {
                if (mLinePadding * (2 * count - 1) > maxHeight) {
                    mStrokeWidth = (maxHeight - mLinePadding * (count - 1)) / count;
                } else {
                    mStrokeWidth = mLinePadding;
                }
            } else {
                mStrokeWidth = mLineLength;
            }
        } else if (mLinePadding <= 0) {
            int count = (int) Math.ceil(mLines);
            if (count > 1) {
                if (mStrokeWidth * (2 * count - 1) > maxHeight) {
                    mLinePadding = (maxHeight - mStrokeWidth * count) / (count - 1);
                } else {
                    mLinePadding = mStrokeWidth;
                }
            }
        } else {
            int count = (int) Math.ceil(mLines);
            if (mStrokeWidth * count + mLinePadding * (count - 1) > maxHeight) {
                mLinePadding = (maxHeight - mStrokeWidth * count) / (count - 1);
            }
        }
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    private int measureSize(int measureSpec) {
        int defSize = ViewUtil.dp2px(getContext(), DEF_DRAW_SIZE);
        int specSize = MeasureSpec.getSize(measureSpec);
        int specMode = MeasureSpec.getMode(measureSpec);

        int result = 0;
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = Math.min(defSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLines == 0f) {
            return;
        }
        int startY = mStartY + mStrokeWidth / 2;
        int currentLength = (int) (mPercent * mLineLength);
        int count = currentLength / mOneLineLength;
        int last = currentLength % mOneLineLength;
        for (int i = 0; i < count; i++) {
            canvas.drawLine(mStartX, startY, mStartX + mOneLineLength, startY, mPaint);
            startY += (mStrokeWidth + mLinePadding);
        }
        if (last > 0) {
            canvas.drawLine(mStartX, startY, mStartX + last, startY, mPaint);
        }
    }

    public void setPercent(float percent) {
        mPercent = percent;
        invalidate();
    }

    public void setLinePadding(int linePadding) {
        mLinePadding = linePadding;
    }

    public void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    public void setLines(float lines) {
        mLines = lines;
    }

    public void setColor(@ColorInt int color){
        mPaint.setColor(color);
    }
}
