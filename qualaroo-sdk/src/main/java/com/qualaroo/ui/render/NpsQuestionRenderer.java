package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.RestrictTo;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.NpsView;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class NpsQuestionRenderer extends QuestionRenderer {

    private static final String KEY_NPS_SCORE = "question.nps_score";

    NpsQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public RestorableView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_nps, null);
        final NpsView npsView = view.findViewById(R.id.qualaroo__nps_scores);
        npsView.applyTheme(getTheme());

        final Button button = view.findViewById(R.id.qualaroo__nps_view_confirm);
        ThemeUtils.applyTheme(button, getTheme());
        button.setText(question.sendText());
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                Answer answer = question.answerList().get(npsView.getCurrentlySelectedScore());
                onAnsweredListener.onResponse(
                        new UserResponse.Builder(question.id())
                                .addChoiceAnswer(answer.id())
                                .build()
                );
            }
        });
        TextView minLabel = view.findViewById(R.id.qualaroo__nps_view_min_label);
        minLabel.setText(question.npsMinLabel());
        minLabel.setTextColor(getTheme().textColor());
        TextView maxLabel = view.findViewById(R.id.qualaroo__nps_view_max_label);
        maxLabel.setText(question.npsMaxLabel());
        maxLabel.setTextColor(getTheme().textColor());
        npsView.setOnScoreChangedListener(new NpsView.OnScoreChangedListener() {
            @Override public void onScoreChanged(int score) {
                button.setEnabled(true);
            }
        });
        return RestorableView.withId(question.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle outState) {
                        outState.putInt(KEY_NPS_SCORE, npsView.getCurrentlySelectedScore());
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle savedState) {
                        int score = savedState.getInt(KEY_NPS_SCORE, -1);
                        npsView.setScore(score);
                    }
                })
                .build();
    }

}
