package com.qualaroo.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ProgressBarView extends View {

    private int backgroundColor = Color.WHITE;
    private int progressColor = Color.RED;

    private float currentProgress;
    private Paint paint = new Paint();

    private ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);

    public ProgressBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                ProgressBarView.this.currentProgress = (float) animation.getAnimatedValue();
                ProgressBarView.this.invalidate();
            }
        });
        animator.setDuration(300);
    }

    public void setColors(@ColorInt int progressColor, @ColorInt int backgroundColor) {
        this.progressColor = progressColor;
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void setProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress) {
        setProgress(progress, true);
    }

    public void setProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress, boolean animate) {
        animator.cancel();
        if (animate) {
            animator.setFloatValues(currentProgress, progress);
            animator.start();
        } else {
            currentProgress = progress;
            invalidate();
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        paint.setColor(backgroundColor);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        paint.setColor(progressColor);
        float progress = getWidth() * currentProgress;
        canvas.drawRect(0, 0, progress, getHeight(), paint);
    }
}
