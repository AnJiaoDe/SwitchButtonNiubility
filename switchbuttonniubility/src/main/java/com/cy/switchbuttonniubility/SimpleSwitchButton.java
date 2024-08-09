package com.cy.switchbuttonniubility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public class SimpleSwitchButton extends View {
    private float radius_indicator;
    private Paint paint_stroke, paint_bg, paint_tint, paint_indicator;
    private float cx;
    private ValueAnimator valueAnimator;
    private long downTime;
    private boolean isChecked = false;
    private OnCheckedChangeListener onCheckedChangeListener;
    private int color_alpha_start;
    private float radius_stroke, radius_tint;
    private volatile boolean isLayoutCalled = false;
    private volatile boolean isDetached = false;

    public SimpleSwitchButton(Context context) {
        this(context, null);
    }

    public SimpleSwitchButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint_stroke = new Paint();
        paint_stroke.setAntiAlias(true);
        paint_stroke.setStyle(Paint.Style.STROKE);

        paint_bg = new Paint();
        paint_bg.setAntiAlias(true);

        paint_tint = new Paint();
        paint_tint.setAntiAlias(true);

        paint_indicator = new Paint();
        paint_indicator.setAntiAlias(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleSwitchButton);
        isChecked = typedArray.getBoolean(R.styleable.SimpleSwitchButton_cy_checked_, false);

        setWidth_stroke(typedArray.getDimensionPixelSize(R.styleable.SimpleSwitchButton_cy_width_stroke, ScreenUtils.dpAdapt(context, 1.3f)));
        setColor_stroke(typedArray.getColor(R.styleable.SimpleSwitchButton_cy_color_stroke, 0xffEEEEEE));
        setColor_bg(typedArray.getColor(R.styleable.SimpleSwitchButton_cy_color_bg, 0xffd0d0d0));
        setColor_tint(typedArray.getColor(R.styleable.SimpleSwitchButton_cy_color_tint, 0xff52D166));
        setColor_indicator(typedArray.getColor(R.styleable.SimpleSwitchButton_cy_color_indicator, 0xffffffff));
        int shadow_radius = typedArray.getDimensionPixelSize(R.styleable.SimpleSwitchButton_cy_shadow_radius, ScreenUtils.dpAdapt(context, 4));
        int shadow_dx = typedArray.getDimensionPixelSize(R.styleable.SimpleSwitchButton_cy_shadow_dx, ScreenUtils.dpAdapt(context, 2));
        int shadow_dy = typedArray.getDimensionPixelSize(R.styleable.SimpleSwitchButton_cy_shadow_dy, ScreenUtils.dpAdapt(context, 2));
        int color_shadow = typedArray.getColor(R.styleable.SimpleSwitchButton_cy_color_shadow, 0x2a000000);

        typedArray.recycle();

        setShadowLayer(shadow_radius, shadow_dx, shadow_dy, color_shadow);
    }

    public void setWidth_stroke(int width_stroke) {
        paint_stroke.setStrokeWidth(width_stroke);
    }

    public void setColor_stroke(int color_stroke) {
        paint_stroke.setColor(color_stroke);
    }

    public void setColor_indicator(int color) {
        paint_indicator.setColor(color);
    }

    public void setColor_bg(int color_bg) {
        paint_bg.setColor(color_bg);
    }

    public void setColor_tint(int color_tint) {
        paint_tint.setColor(Color.argb(isChecked ? 255 : 0,
                Color.red(color_tint), Color.green(color_tint), Color.blue(color_tint)));
    }

    public void setShadowLayer(float radius, float shadow_dx, float shadow_dy, int color) {
        paint_indicator.setShadowLayer(radius, shadow_dx, shadow_dy, color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        radius_stroke = (getHeight() - getPaddingTop() - getPaddingBottom() - paint_stroke.getStrokeWidth()) * 0.5f;
        radius_tint = radius_stroke + paint_stroke.getStrokeWidth() * 0.5f;
        radius_indicator = radius_stroke - paint_stroke.getStrokeWidth() * 0.5f;
        cx = isChecked ? getWidth() - getPaddingRight() - paint_stroke.getStrokeWidth() - radius_indicator
                : (getPaddingLeft() + paint_stroke.getStrokeWidth() + radius_indicator);

        isLayoutCalled = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRoundRect(getPaddingLeft() + paint_stroke.getStrokeWidth() * 0.5f,
                getPaddingTop() + paint_stroke.getStrokeWidth() * 0.5f,
                getWidth() - getPaddingRight() - paint_stroke.getStrokeWidth() * 0.5f,
                getHeight() - getPaddingBottom() - paint_stroke.getStrokeWidth() * 0.5f,
                radius_stroke, radius_stroke, paint_stroke);

        canvas.drawRoundRect(getPaddingLeft() + paint_stroke.getStrokeWidth(),
                getPaddingTop() + paint_stroke.getStrokeWidth(),
                getWidth() - getPaddingRight() - paint_stroke.getStrokeWidth(),
                getHeight() - getPaddingBottom() - paint_stroke.getStrokeWidth(),
                radius_indicator, radius_indicator, paint_bg);

        canvas.drawRoundRect(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom(),
                radius_tint, radius_tint, paint_tint);

        canvas.drawCircle(cx, getPaddingTop() + paint_stroke.getStrokeWidth() + radius_indicator,
                radius_indicator, paint_indicator);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final ViewParent parent = getParent();
        float cx_min = getPaddingLeft() + paint_stroke.getStrokeWidth() + radius_indicator;
        float cx_max = getWidth() - getPaddingRight() - paint_stroke.getStrokeWidth() - radius_indicator;
        if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                cx = event.getX();
                cx = Math.min(cx_max, Math.max(cx, cx_min));
                int color = Color.argb((int) (255 * (cx - cx_min) / (cx_max - cx_min)),
                        Color.red(paint_tint.getColor()), Color.green(paint_tint.getColor()), Color.blue(paint_tint.getColor()));
                paint_tint.setColor(color);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                boolean lastChecked = isChecked;
                //点击时间小于300ms，认为是点击操作
                if (System.currentTimeMillis() - downTime <= 300) {
                    isChecked = !isChecked;
                } else {
                    isChecked = event.getX() >= getWidth() * 0.5f;
                }
                valueAnimate();
                //注意顺序，onCheckedChanged应该在valueAnimate后面，防止onCheckedChanged耗时操作导致动画延误显示，感觉很卡
                if (lastChecked != isChecked && onCheckedChangeListener != null)
                    onCheckedChangeListener.onCheckedChanged(this, isChecked);
                break;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetached = true;
    }

    public void setChecked(boolean check) {
        if (isChecked == check) return;
        isChecked = check;
        //注意顺序，onCheckedChanged应该在valueAnimate后面，防止onCheckedChanged耗时操作导致动画延误显示，感觉很卡
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.onCheckedChanged(this, isChecked);
        ThreadUtils.getInstance().runThread(new ThreadUtils.RunnableCallback<Object>() {
            @Override
            public Object runThread() {
                while (!isLayoutCalled && !isDetached) {
                }
                return null;
            }

            @Override
            public void runUIThread(Object result) {
//                LogUtils.log("runUIThread");
                if (!isDetached)
                    valueAnimate();
            }
        });

    }

    public boolean isChecked() {
        return isChecked;
    }

    private void valueAnimate() {
        //注意color_alpha_start的位置
        color_alpha_start = Color.alpha(paint_tint.getColor());
        //必须让取消先前的动画，再去执行新的动画。注意：不是end()执行完毕，不然会先抖动一次
        if (valueAnimator != null) {
            valueAnimator.cancel();
        } else {
            valueAnimator = new ValueAnimator();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    cx = (float) animation.getAnimatedValue();
                    int alpha = (int) (isChecked ? (color_alpha_start + (255 - color_alpha_start) * animation.getAnimatedFraction())
                            : (color_alpha_start - color_alpha_start * animation.getAnimatedFraction()));
                    int color = Color.argb(alpha,
                            Color.red(paint_tint.getColor()), Color.green(paint_tint.getColor()), Color.blue(paint_tint.getColor()));
                    paint_tint.setColor(color);
                    invalidate();
                }
            });
            valueAnimator.setDuration(150);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.setEvaluator(new FloatEvaluator());
        }
        float cx_min = getPaddingLeft() + paint_stroke.getStrokeWidth() + radius_indicator;
        float cx_max = getWidth() - getPaddingRight() - paint_stroke.getStrokeWidth() - radius_indicator;
        valueAnimator.setFloatValues(cx, isChecked ? cx_max : cx_min);
        valueAnimator.start();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public static interface OnCheckedChangeListener {
        void onCheckedChanged(SimpleSwitchButton simpleSwitchButton, boolean isChecked);
    }
}
