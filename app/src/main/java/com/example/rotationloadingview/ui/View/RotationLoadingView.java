package com.example.rotationloadingview.ui.View;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.rotationloadingview.R;

/**
 *加载控件
 *
 * Ethan Lee
 */
public class RotationLoadingView extends View {
    public static final String TAG = "RotationLoadingView";
    /**
     * 控件宽高
     */
    private float mWidth, mHeight;
    /**
     * 6个小圆围绕旋转中心点的位置
     */
    private PointF core;
    /**
     * 6 个小圆围绕旋转的大半径
     */
    private float rotationRadius;
    /**
     * 6 个小圆的半径
     */
    private float miniCircleRadius;
    /**
     * 6 中颜色
     */
    private int[] colors;
    /**
     * 小圆画笔
     */
    private Paint mPaint;

    /**
     * 旋转角度，以中心点正上方为 0 度
     */
    private double deltaAngle = 0;

    /**
     * 空心大圆画笔
     */
    private Paint transparentPaint;
    /**
     * 控件对角线的一半
     */
    private float sqrtDistance;
    /**
     * 空心大圆半径
     */
    private float tpRadius = 0;

    // 属性动画
    private ValueAnimator mValueAnimator;
    private AnimationEndListener mAnimationEndListener;

    public RotationLoadingView(Context context) {
        this(context, null);
    }

    public RotationLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotationLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRes(context, attrs, defStyleAttr);
    }

    private void initRes(Context context, AttributeSet attrs, int defStyleAttr) {
        int blue = Color.parseColor("#3079F6");
        int red = Color.parseColor("#E41A1A");
        int green = Color.parseColor("#33C339");
        int purple_500 = Color.parseColor("#FF6200EE");
        int teal_700 = Color.parseColor("#FF018786");
        int yellow = Color.parseColor("#BFAC03");
        colors = new int[]{blue, red, green, purple_500, teal_700, yellow};
        mPaint = new Paint();
        mPaint.setColor(colors[0]);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        core = new PointF();
        transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(R.color.white));
        transparentPaint.setStyle(Paint.Style.STROKE);
        transparentPaint.setAntiAlias(true);
        transparentPaint.setDither(true);
    }

    /**
     * 对外接口，动画开始
     */
    public void startAnimator(){
        dataReset();
        getRotationAnimator();
    }

    /**
     * 对外接口，取消动画
     */
    public void setAnimatorCancel(){
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }

    /**
     * 开始属性动画
     */
    private void getRotationAnimator() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
        mValueAnimator = new ValueAnimator();
        // 将动画分成4小段，用于控制4段效果
        mValueAnimator.setFloatValues(0, 1, 2, 3, 4);
        // 总时长
        mValueAnimator.setDuration(4000);
        mValueAnimator.addUpdateListener(this::dealWithValue);
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "onAnimationStart");
                dataReset();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd");
                setAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(TAG, "onAnimationCancel");
                dataReset();
                setAnimationEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.d(TAG, "onAnimationRepeat");
            }
        });
        mValueAnimator.start();
    }

    /**
     * 开始计算绘制参数
     *
     * @param animator
     */
    private void dealWithValue(ValueAnimator animator) {
        float value = (float) animator.getAnimatedValue();
        Log.d(TAG, "V = " + value);

        if (value > 3){   // 计算第四段动画参数
            // -3 使 value 从 0 变到 1
            value = value - 3;
            // 计算大圆的参数
            float strokeWidth = sqrtDistance * (1 - value);
            transparentPaint.setStrokeWidth(strokeWidth);
            tpRadius = strokeWidth / 2 + (sqrtDistance - strokeWidth);
            // 计算6 个小圆的参数
            deltaAngle = (1 - value) * 4 * Math.PI;
            value = (float) (value * 1.25);
            rotationRadius = sqrtDistance * value;
        }else if (value > 2){ // 计算第三段动画参数
            // -2 使 value 从 0 变到 1
            value = value - 2;
            deltaAngle = (1 + value) * 2 * Math.PI;
            rotationRadius = (3 * mWidth / 8) * (1 - value);
        }else if (value > 1){ // 计算第二段动画参数
            // -1 使 value 从 0 变到 1
            value = value - 1;
            rotationRadius = (mWidth / 4) * (1 + value / 2);
        }else {  // 计算第一段动画参数
            deltaAngle = value * 2 * Math.PI;
        }
        // 有时候一个轮回下来 value都没有 1
        // 重绘
        invalidate();
    }

    /**
     * 重置参数
     */
    private void dataReset() {
        deltaAngle = 0;
        rotationRadius = mWidth / 4;
        tpRadius = sqrtDistance / 2;
        transparentPaint.setStrokeWidth(sqrtDistance);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
        // 初始化参数
        core.set(mWidth / 2, mHeight / 2);
        miniCircleRadius = mWidth / 32;
        sqrtDistance = (float) Math.sqrt(mWidth * mWidth / 4 + mHeight * mHeight / 4);
        dataReset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制背景大圆
        canvas.drawCircle(core.x, core.y, tpRadius, transparentPaint);
        // 循环绘制 6 个小圆
        for (int i = 0; i < colors.length; i++) {
            mPaint.setColor(colors[i]);
            float circleX = (float) (core.x + rotationRadius * Math.sin(i * 2 * Math.PI / 6 + deltaAngle));
            float circleY = (float) (core.y - rotationRadius * Math.cos(i * 2 * Math.PI / 6 + deltaAngle));
            canvas.drawCircle(circleX, circleY, miniCircleRadius, mPaint);
        }
    }

    public interface AnimationEndListener {
        void animationEnd();
    }

    public void setAnimationEndListener(AnimationEndListener animationEndListener) {
        mAnimationEndListener = animationEndListener;
    }

    public void setAnimationEnd() {
        if (mAnimationEndListener != null) {
            mAnimationEndListener.animationEnd();
        }
    }
}
