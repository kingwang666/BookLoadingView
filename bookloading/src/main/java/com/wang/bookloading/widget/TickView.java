package com.wang.bookloading.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import com.wang.bookloading.R;
import com.wang.bookloading.ViewUtil;

/**
 * Created on 2017/8/23.
 * Author: wang
 * padding 只有left和Top有用
 */

public class TickView extends View {

    private static final int DEF_DRAW_SIZE = 25;

    private Paint mPaint;
    private RectF mArcRect;
    private Point[] mTickPoints;
    private Path mTickPath;

    private int mDiameter;
    private float mLeftLineDistance;
    private float mRightLineDistance;

    private int mStrokeWidth;

    private float mPercent = 0f;


    public TickView(Context context) {
        super(context);
        init(context, null);
    }

    public TickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TickView);
        mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.TickView_tv_strokeWidth, 0);
        int color = typedArray.getColor(R.styleable.TickView_tv_color, Color.RED);
        mPercent = typedArray.getFloat(R.styleable.TickView_tv_percent, 0f);
        typedArray.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(color);

        mTickPath = new Path();
        mArcRect = new RectF();
        mTickPoints = new Point[3];
        mTickPoints[0] = new Point();
        mTickPoints[1] = new Point();
        mTickPoints[2] = new Point();
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec));
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int left = getPaddingLeft();
        int top = getPaddingTop();

        mDiameter = Math.min(height - top * 2, width - left * 2);
        if (mDiameter <= 0) {
            return;
        }
        mStrokeWidth = (mStrokeWidth == 0 ? mDiameter / 10 : mStrokeWidth);
        mStrokeWidth = mStrokeWidth > mDiameter / 5 ? mDiameter / 5 : mStrokeWidth;
        mStrokeWidth = (mStrokeWidth < 3) ? 3 : mStrokeWidth;

        mDiameter = mDiameter - mStrokeWidth;


        mArcRect.left = (float) (width - mDiameter) / 2;
        mArcRect.top = (float) (height - mDiameter) / 2;
        mArcRect.right = (float) (width + mDiameter) / 2;
        mArcRect.bottom = (float) (height + mDiameter) / 2;


        mTickPoints[0].x = Math.round((float) mDiameter / 30 * 7 + mArcRect.left);
        mTickPoints[0].y = Math.round((float) mDiameter / 30 * 14 + mArcRect.top);
        mTickPoints[1].x = Math.round((float) mDiameter / 30 * 13 + mArcRect.left);
        mTickPoints[1].y = Math.round((float) mDiameter / 30 * 20 + mArcRect.top);
        mTickPoints[2].x = Math.round((float) mDiameter / 30 * 22 + mArcRect.left);
        mTickPoints[2].y = Math.round((float) mDiameter / 30 * 10 + mArcRect.top);

        mLeftLineDistance = (float) Math.sqrt(Math.pow(mTickPoints[1].x - mTickPoints[0].x, 2) +
                Math.pow(mTickPoints[1].y - mTickPoints[0].y, 2));
        mRightLineDistance = (float) Math.sqrt(Math.pow(mTickPoints[2].x - mTickPoints[1].x, 2) +
                Math.pow(mTickPoints[2].y - mTickPoints[1].y, 2));
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPercent == 0) {
            return;
        }
        mTickPath.reset();
        float angle = 360 * mPercent;
        if (angle < 360) {
            mTickPath.addArc(mArcRect, 270, -angle);
        } else {
            mTickPath.addCircle(mArcRect.centerX(), mArcRect.centerY(), mDiameter / 2, Path.Direction.CW);
        }
        float drewDistance = mPercent * (mLeftLineDistance + mRightLineDistance);
        if (drewDistance < mLeftLineDistance) {
            float stopX = mTickPoints[0].x + (mTickPoints[1].x - mTickPoints[0].x) * drewDistance / mLeftLineDistance;
            float stopY = mTickPoints[0].y + (mTickPoints[1].y - mTickPoints[0].y) * drewDistance / mLeftLineDistance;

            mTickPath.moveTo(mTickPoints[0].x, mTickPoints[0].y);
            mTickPath.lineTo(stopX, stopY);
            canvas.drawPath(mTickPath, mPaint);
        } else {
            mTickPath.moveTo(mTickPoints[0].x, mTickPoints[0].y);
            mTickPath.lineTo(mTickPoints[1].x, mTickPoints[1].y);

            // draw right of the tick
            if (drewDistance < mLeftLineDistance + mRightLineDistance) {
                float stopX = mTickPoints[1].x + (mTickPoints[2].x - mTickPoints[1].x) * (drewDistance - mLeftLineDistance) / mRightLineDistance;
                float stopY = mTickPoints[1].y - (mTickPoints[1].y - mTickPoints[2].y) * (drewDistance - mLeftLineDistance) / mRightLineDistance;

                mTickPath.moveTo(mTickPoints[1].x, mTickPoints[1].y);
                mTickPath.lineTo(stopX, stopY);
                canvas.drawPath(mTickPath, mPaint);
            } else {
                mTickPath.moveTo(mTickPoints[1].x, mTickPoints[1].y);
                mTickPath.lineTo(mTickPoints[2].x, mTickPoints[2].y);
                canvas.drawPath(mTickPath, mPaint);
            }
        }
    }

    public void setPercent(float percent) {
        mPercent = percent;
        invalidate();
    }

    public void setStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    public void setColor(@ColorInt int color){
        mPaint.setColor(color);
    }
}
