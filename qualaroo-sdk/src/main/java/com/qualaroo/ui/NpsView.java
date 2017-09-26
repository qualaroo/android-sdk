package com.qualaroo.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.Space;
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

import com.qualaroo.R;
import com.qualaroo.ui.render.Theme;

import java.util.ArrayList;
import java.util.List;

public class NpsView extends FrameLayout {

    private final LinearLayout scoresContainer;
    public interface OnScoreChangedListener {
        void onScoreChanged(int score);
    }
    private static final int NUM_OF_SCORES = 11;

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 10;
    private static final int SPACE_WIDTH_IN_DP = 4;
    private final List<AppCompatTextView> scores = new ArrayList<>();

    private final Drawable activeDrawable;
    private final Drawable inactiveDrawable;
    private final @Px int spaceWidth;
    private TextView hintView;
    private final int[] scorePositions = new int[NUM_OF_SCORES];

    private int currentlySelectedScore = -1;
    private OnScoreChangedListener onScoreChangedListener;

    private @ColorInt int inactiveTextColor = Color.WHITE;
    private @ColorInt int activeTextColor = Color.BLACK;

    private LinearLayout.LayoutParams scoreViewParams = new LinearLayout.LayoutParams(0, 0);

    public NpsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scoresContainer = new LinearLayout(context);
        addView(scoresContainer);
        scoresContainer.setOrientation(LinearLayout.HORIZONTAL);
        spaceWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SPACE_WIDTH_IN_DP, getResources().getDisplayMetrics());
        inflateScoreViews();
        inflateHint();
        setClipChildren(false);
        inactiveDrawable = getResources().getDrawable(R.drawable.qualaroo__nps_view_score_background_inactive);
        activeDrawable = getResources().getDrawable(R.drawable.qualaroo__nps_view_score_background_active);
    }

    private void inflateHint() {
        hintView = new TextView(getContext());
        hintView.setVisibility(View.GONE);
        hintView.setGravity(Gravity.CENTER);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        addView(hintView);
    }

    public void applyTheme(Theme theme) {
        inactiveTextColor = theme.textColor();
        activeTextColor = theme.backgroundColor();
        DrawableCompat.setTint(inactiveDrawable, theme.buttonDisabledColor());
        DrawableCompat.setTint(activeDrawable, theme.accentColor());
        for (TextView score : scores) {
            score.setTextColor(inactiveTextColor);
            score.setBackgroundDrawable(inactiveDrawable);
        }
        hintView.setTextColor(inactiveTextColor);
        hintView.setBackgroundDrawable(inactiveDrawable);
        hintView.setVisibility(View.GONE);
    }

    private void inflateScoreViews() {
        for (int scoreValue = MIN_SCORE; scoreValue <= MAX_SCORE; scoreValue++) {
            AppCompatTextView textView = new AppCompatTextView(getContext());
            textView.setText(String.valueOf(scoreValue));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(scoreViewParams);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            scores.add(textView);
            scoresContainer.addView(textView);
            if (scoreValue < MAX_SCORE) {
                addDivider();
            }
        }
    }

    private void addDivider() {
        Space space = new Space(getContext());
        space.setLayoutParams(new LinearLayout.LayoutParams(spaceWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        scoresContainer.addView(space);
    }
    private void selectScore(int scoreValue) {
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

    public void setScore(int score) {
        selectScore(score);
        invalidate();
    }

    public int getCurrentlySelectedScore() {
        return currentlySelectedScore;
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            post(new Runnable() {
                @Override public void run() {
                    int size = calculateScoreViewSize();
                    scoreViewParams.width = size;
                    scoreViewParams.height = size * 8 / 5;
                    for (int i = 0; i < scores.size(); i++) {
                        AppCompatTextView view = scores.get(i);
                        view.setLayoutParams(scoreViewParams);

                        view.setBackgroundDrawable(null);
                        view.setBackgroundDrawable(inactiveDrawable);

                        scorePositions[i] = size * i + spaceWidth * i;
                    }
                    hintView.setLayoutParams(new LayoutParams(size, size * 8 / 5));
                    int leftoverSpace = leftoverSpaceAfterLayingOutScores();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size + leftoverSpace, scoreViewParams.height);
                    scores.get(scores.size() - 1).setLayoutParams(params);
                }
            });
        }
    }

    private int calculateScoreViewSize() {
        return (getWidth() - (NUM_OF_SCORES * spaceWidth)) / NUM_OF_SCORES;
    }

    private int leftoverSpaceAfterLayingOutScores() {
        return getWidth() - NUM_OF_SCORES * spaceWidth - NUM_OF_SCORES * calculateScoreViewSize();
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
            hintView.setVisibility(View.VISIBLE);
            hintView.setText(String.valueOf(current));
            AppCompatTextView view = scores.get(current);
            hintView.setBackgroundDrawable(view.getBackground());
            hintView.setTextColor(view.getCurrentTextColor());
            hintView.setX(view.getX());
            hintView.setY(view.getY() - view.getHeight() + spaceWidth);
        } else {
            hintView.setVisibility(View.GONE);
        }
    }

    private int getCurrentlyTouchedScore(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        if (x <= getWidth() && y >=0 && y <= getHeight()) {
            int touchedScore = 0;
            for (int i = 0; i < scorePositions.length; i++) {
                int pos = scorePositions[i];
                if (x > pos) {
                    touchedScore = i;
                }
            }
            return touchedScore;
        }
        return -1;
    }

    private void selectCurrentlyTouchedScore(MotionEvent event) {
        int currentlyTouchedScore = getCurrentlyTouchedScore(event);
        if (currentlyTouchedScore != -1) {
            selectScore(currentlyTouchedScore);
        }
    }
}
