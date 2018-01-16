package com.qualaroo.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualaroo.ui.render.Theme;
import com.qualaroo.util.DimenUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public class NpsView extends FrameLayout {

    public interface OnScoreChangedListener {
        void onScoreChanged(int score);
    }

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 10;
    private static final int SPACE_WIDTH_IN_DP = 4;
    private static final int SCORE_CORNER_RADIUS = 2;
    private static final int NUM_OF_SCORES = 11;

    private final List<AppCompatTextView> scores = new ArrayList<>();
    private final Rect hitRect = new Rect();
    private final LinearLayout scoresContainer;
    private final Drawable activeDrawable;
    private final Drawable inactiveDrawable;
    private final @Px int spaceWidth;

    private TextView hintView;
    private int currentlySelectedScore = -1;
    private OnScoreChangedListener onScoreChangedListener;

    private @ColorInt int inactiveTextColor = Color.WHITE;
    private @ColorInt int activeTextColor = Color.BLACK;

    public NpsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scoresContainer = new LinearLayout(context);
        addView(scoresContainer);
        scoresContainer.setOrientation(LinearLayout.HORIZONTAL);
        spaceWidth = (int) DimenUtils.toPx(context, SPACE_WIDTH_IN_DP);
        inflateScoreViews();
        inflateHint();
        float[] cornerRadius = new float[8];
        Arrays.fill(cornerRadius, DimenUtils.toPx(context, SCORE_CORNER_RADIUS));
        inactiveDrawable = new ShapeDrawable(new RoundRectShape(cornerRadius, null, null));
        activeDrawable = new ShapeDrawable(new RoundRectShape(cornerRadius, null, null));
    }

    public void applyTheme(Theme theme) {
        inactiveTextColor = theme.buttonTextDisabled();
        activeTextColor = theme.buttonTextEnabled();
        for (TextView score : scores) {
            score.setTextColor(inactiveTextColor);
            score.setBackgroundDrawable(inactiveDrawable);
        }
        activeDrawable.setColorFilter(theme.buttonEnabledColor(), PorterDuff.Mode.SRC_ATOP);
        inactiveDrawable.setColorFilter(theme.buttonDisabledColor(), PorterDuff.Mode.SRC_ATOP);

        hintView.setTextColor(inactiveTextColor);
        hintView.setBackgroundDrawable(inactiveDrawable);
        hintView.setVisibility(View.GONE);
    }

    private void inflateScoreViews() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, spaceWidth, 0);
        params.weight = 1;
        for (int scoreValue = MIN_SCORE; scoreValue <= MAX_SCORE; scoreValue++) {
            AppCompatTextView textView = new AppCompatTextView(getContext());
            textView.setText(String.valueOf(scoreValue));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            if (scoreValue == MAX_SCORE) {
                LinearLayout.LayoutParams lastItemParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                lastItemParams.setMargins(0, 0, 0, 0);
                lastItemParams.weight = 1;
                textView.setLayoutParams(lastItemParams);
            } else {
                textView.setLayoutParams(params);
            }
            scores.add(textView);
            scoresContainer.addView(textView);
        }
    }

    private void inflateHint() {
        hintView = new TextView(getContext());
        hintView.setVisibility(View.GONE);
        hintView.setGravity(Gravity.CENTER);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        addView(hintView);
    }


    public void setScore(int scoreValue) {
        if (scoreValue == currentlySelectedScore) {
            //preemptive optimization :))
            return;
        }
        currentlySelectedScore = scoreValue;
        for (TextView score : scores) {
            score.setBackgroundDrawable(inactiveDrawable);
            score.setTextColor(inactiveTextColor);
        }
        AppCompatTextView selectedScore = scores.get(currentlySelectedScore);
        selectedScore.setBackgroundDrawable(activeDrawable);
        selectedScore.setTextColor(activeTextColor);
        if (onScoreChangedListener != null) {
            onScoreChangedListener.onScoreChanged(scoreValue);
        }
    }

    public void setOnScoreChangedListener(OnScoreChangedListener onScoreChangedListener) {
        this.onScoreChangedListener = onScoreChangedListener;
    }

    public int getCurrentlySelectedScore() {
        return currentlySelectedScore;
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (width - (NUM_OF_SCORES * spaceWidth)) / NUM_OF_SCORES;
        height = height * 8 / 5;
        measureChildren(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            displayHint(event);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            displayHint(event);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            selectCurrentlyTouchedScore(event);
            hintView.setVisibility(View.GONE);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void displayHint(MotionEvent event) {
        int current = getCurrentlyTouchedScore(event);
        if (current != -1) {
            AppCompatTextView view = scores.get(current);
            hintView.setLayoutParams(new FrameLayout.LayoutParams(view.getWidth(), view.getHeight()));
            hintView.setVisibility(View.VISIBLE);
            hintView.setText(String.valueOf(current));
            hintView.setBackgroundDrawable(view.getBackground());
            hintView.setTextColor(view.getCurrentTextColor());
            hintView.setX(view.getX());
            hintView.setY(view.getY() - view.getHeight() + spaceWidth);
        } else {
            hintView.setVisibility(View.GONE);
        }
    }

    private int getCurrentlyTouchedScore(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        for (int i = 0; i < scoresContainer.getChildCount(); i++) {
            View view = scoresContainer.getChildAt(i);
            view.getHitRect(hitRect);
            if (hitRect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void selectCurrentlyTouchedScore(MotionEvent event) {
        int currentlyTouchedScore = getCurrentlyTouchedScore(event);
        if (currentlyTouchedScore != -1) {
            setScore(currentlyTouchedScore);
        }
    }
}
