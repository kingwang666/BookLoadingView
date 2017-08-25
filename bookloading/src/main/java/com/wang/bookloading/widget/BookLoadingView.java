package com.wang.bookloading.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wang.bookloading.JumpingBeans;
import com.wang.bookloading.R;
import com.wang.bookloading.Rotate3dAnimation;


/**
 * Created on 2017/8/25.
 * Author: wang
 */

public class BookLoadingView extends FrameLayout {

    private static final int DEFAULT_FIRST_DURATION = 800;
    private static final int DEFAULT_TEXT_DURATION = 1000;
    private static final int DEFAULT_PAGE_DURATION = 1000;

    private static final int DEFAULT_START_COLOR = Color.WHITE;
    private static final int DEFAULT_END_COLOR = 0xffe7e7e8;
    private static final int DEFAULT_COLOR = Color.RED;
    private static final float DEFAULT_LINES = 3.5f;

    private TickView mTickView;
    private TextMaskView mTextMaskView;
    private TextView mLoadingTV;

    private ValueAnimator mPathAnim;
    private Rotate3dAnimation mFirst3DAnim;
    private Rotate3dAnimation mSec3DAnim;
    private ObjectAnimator mColorAnim;
    private JumpingBeans.Builder mBuilder;
    private JumpingBeans mTextJumpAnim;

    private int mDelay;
    private int mFirstDuration;
    private int mTextDuration;
    private int mPageDuration;
    private int mStartColor;
    private int mEndColor;

    private boolean mStopped = true;


    public BookLoadingView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public BookLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BookLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BookLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setClipChildren(false);
        LayoutInflater.from(context).inflate(R.layout.layout_book_loading, this, true);

        mTickView = (TickView) findViewById(R.id.tick_view);
        mTextMaskView = (TextMaskView) findViewById(R.id.text_mask_view);
        mLoadingTV = (TextView) findViewById(R.id.loading_tv);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BookLoadingView);
        mDelay = typedArray.getInteger(R.styleable.BookLoadingView_blv_delay, 0);
        mFirstDuration = typedArray.getInteger(R.styleable.BookLoadingView_blv_firstDuration, DEFAULT_FIRST_DURATION);
        mTextDuration = typedArray.getInteger(R.styleable.BookLoadingView_blv_textDuration, DEFAULT_TEXT_DURATION);
        mPageDuration = typedArray.getInteger(R.styleable.BookLoadingView_blv_pageDuration, DEFAULT_PAGE_DURATION);
        mStartColor = typedArray.getColor(R.styleable.BookLoadingView_blv_startColor, DEFAULT_START_COLOR);
        mEndColor = typedArray.getColor(R.styleable.BookLoadingView_blv_endColor, DEFAULT_END_COLOR);
        setTickColor(typedArray.getColor(R.styleable.BookLoadingView_blv_tickColor, DEFAULT_COLOR));
        setTickStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.BookLoadingView_blv_tickStrokeWidth, 0));
        setLines(typedArray.getFloat(R.styleable.BookLoadingView_blv_lines, DEFAULT_LINES));
        setLinePadding(typedArray.getDimensionPixelSize(R.styleable.BookLoadingView_blv_linePadding, 0));
        setLineStrokeWidth(typedArray.getDimensionPixelSize(R.styleable.BookLoadingView_blv_lineStrokeWidth, 0));
        setLineColor(typedArray.getColor(R.styleable.BookLoadingView_blv_lineColor, DEFAULT_COLOR));
        setTextSize(typedArray.getDimensionPixelSize(R.styleable.BookLoadingView_blv_textSize, (int) mLoadingTV.getTextSize()));
        setTextColor(typedArray.getColor(R.styleable.BookLoadingView_blv_textColor, mLoadingTV.getCurrentTextColor()));
        if (typedArray.hasValue(R.styleable.BookLoadingView_blv_text)) {
            setText(typedArray.getString(R.styleable.BookLoadingView_blv_text));
        }
        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getVisibility() == VISIBLE) {
            startLoading(mDelay);
        }
    }


    private void startLoading(long delay) {
        if (!mStopped
                || (mPathAnim != null && mPathAnim.isStarted())
                || (mColorAnim != null && mColorAnim.isStarted())
                || (mFirst3DAnim != null && mFirst3DAnim.hasStarted())
                || (mSec3DAnim != null && mSec3DAnim.hasStarted())) {
            return;
        }
        mStopped = false;
        mTickView.setPercent(0);
        mTextMaskView.setPercent(0);
        mTextMaskView.clearAnimation();
        if (mTextJumpAnim != null) {
            mTextJumpAnim.stopJumping();
        }
        startPathAnim(delay);
    }

    private void startPathAnim(long delay) {
        if (mPathAnim == null) {
            initPathAnim();
        }
        mPathAnim.setStartDelay(delay);
        mPathAnim.start();
    }

    private void initPathAnim() {
        mPathAnim = ValueAnimator.ofFloat(0f, 1.0f);
        mPathAnim.setDuration(mFirstDuration);
        mPathAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mPathAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mTickView.setPercent(value * 1.5f);
                mTextMaskView.setPercent(value);
            }
        });

        mPathAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mStopped) {
                    startTextAnim();
                    start3DAnim();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void startTextAnim() {
        if (mBuilder == null) {
            mBuilder = JumpingBeans.with(mLoadingTV)
                    .makeTextJump(0, mLoadingTV.getText().length())
                    .setIsWave(true)
                    .setLoopDuration(mTextDuration);
        }
        mTextJumpAnim = mBuilder.build();
    }

    private void start3DAnim() {
        float centerY = (float) mTextMaskView.getMeasuredHeight() / 2;
        if (mFirst3DAnim == null) {
            initStart3DAnim(centerY);
        }
        if (mSec3DAnim == null) {
            initSec3DAnim(centerY);
        }
        if (mColorAnim == null) {
            initColorAnim();
        }
        mTextMaskView.startAnimation(mFirst3DAnim);
        mColorAnim.start();
    }

    private void initStart3DAnim(float centerY) {
        mFirst3DAnim = new Rotate3dAnimation(0, -90, 0, centerY, 0, false);
        mFirst3DAnim.setFillAfter(true);
        mFirst3DAnim.setDuration(mPageDuration / 2);
        mFirst3DAnim.setStartOffset(mTextDuration > 500 ? mTextDuration - 200 : mTextDuration);
        mFirst3DAnim.setInterpolator(new AccelerateInterpolator());
        mFirst3DAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                mTextMaskView.setBackgroundColor(Color.GRAY);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!mStopped) {
                    mTextMaskView.setPercent(0);
                    mTextMaskView.startAnimation(mSec3DAnim);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initSec3DAnim(float centerY) {
        mSec3DAnim = new Rotate3dAnimation(-90, -180, 0, centerY, 0, false);
        mSec3DAnim.setDuration(mPageDuration / 2);
        mSec3DAnim.setInterpolator(new DecelerateInterpolator());
        mSec3DAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!mStopped) {
                    mTickView.setPercent(0);
                    mTextMaskView.clearAnimation();
                    startPathAnim(200);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void initColorAnim() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mColorAnim = ObjectAnimator.ofArgb(mTextMaskView, "backgroundColor", mStartColor, mEndColor, mStartColor);
        } else {
            mColorAnim = ObjectAnimator.ofObject(mTextMaskView, "backgroundColor", new TypeEvaluator() {
                @Override
                public Object evaluate(float fraction, Object startValue, Object endValue) {
                    int startInt = (Integer) startValue;
                    int startA = (startInt >> 24) & 0xff;
                    int startR = (startInt >> 16) & 0xff;
                    int startG = (startInt >> 8) & 0xff;
                    int startB = startInt & 0xff;

                    int endInt = (Integer) endValue;
                    int endA = (endInt >> 24) & 0xff;
                    int endR = (endInt >> 16) & 0xff;
                    int endG = (endInt >> 8) & 0xff;
                    int endB = endInt & 0xff;

                    return (startA + (int) (fraction * (endA - startA))) << 24 |
                            (startR + (int) (fraction * (endR - startR))) << 16 |
                            (startG + (int) (fraction * (endG - startG))) << 8 |
                            (startB + (int) (fraction * (endB - startB)));
                }
            }, mStartColor, mEndColor, mStartColor);
        }
        mColorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mColorAnim.setStartDelay(mTextDuration > 500 ? mTextDuration - 200 : mTextDuration);
        mColorAnim.setDuration(mPageDuration);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoading();
        super.onDetachedFromWindow();
    }

    private void stopLoading() {
        mStopped = true;
        if (mPathAnim != null) {
            if (mPathAnim.isStarted()) {
                mPathAnim.cancel();
            }
            mPathAnim.removeAllListeners();
            mPathAnim.removeAllUpdateListeners();
            mPathAnim = null;
        }
        mTextMaskView.clearAnimation();
        if (mFirst3DAnim != null) {
            mFirst3DAnim.setAnimationListener(null);
            mFirst3DAnim = null;
        }
        if (mSec3DAnim != null) {
            mSec3DAnim.setAnimationListener(null);
            mSec3DAnim = null;
        }
        if (mColorAnim != null) {
            if (mColorAnim.isStarted()) {
                mColorAnim.cancel();
            }
            mColorAnim.removeAllListeners();
            mColorAnim = null;
        }
        mTextJumpAnim.stopJumping();
    }

    @Override
    public void setVisibility(int visibility) {
        this.setVisibility(visibility, mDelay);
    }

    public void setVisibility(int visibility, int delay) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            startLoading(delay);
        } else {
            stopLoading();
        }
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    public int getDelay() {
        return mDelay;
    }

    public void setLinePadding(int linePadding) {
        mTextMaskView.setLinePadding(linePadding);
    }

    public void setLineStrokeWidth(@Px int strokeWidth) {
        mTextMaskView.setStrokeWidth(strokeWidth);
    }

    public void setLines(float lines) {
        mTextMaskView.setLines(lines);
    }

    public void setLineColor(@ColorInt int color) {
        mTextMaskView.setColor(color);
    }

    public void setTickStrokeWidth(@Px int strokeWidth) {
        mTickView.setStrokeWidth(strokeWidth);
    }

    public void setTickColor(@ColorInt int color) {
        mTickView.setColor(color);
    }

    public void setTextSize(@Px int size) {
        mLoadingTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setTextColor(@ColorInt int color) {
        mLoadingTV.setTextColor(color);
    }

    public void setText(CharSequence text) {
        mLoadingTV.setText(text);
    }
}
