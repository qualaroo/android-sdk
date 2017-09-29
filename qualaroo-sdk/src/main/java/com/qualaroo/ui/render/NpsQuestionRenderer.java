package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.NpsView;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class NpsQuestionRenderer extends QuestionRenderer {

    private static final String KEY_NPS_SCORE = "question.nps_score";

    NpsQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public QuestionView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_nps, null);
        final NpsView npsView = view.findViewById(R.id.qualaroo__nps_scores);
        npsView.applyTheme(getTheme());

        final Button button = view.findViewById(R.id.qualaroo__nps_view_confirm);
        ThemeUtils.applyTheme(button, getTheme());
        button.setText(question.sendText());
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                Answer answer = question.answerList().get(npsView.getCurrentlySelectedScore());
                onAnsweredListener.onAnswered(question, answer);
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
        return QuestionView.forQuestionId(question.id())
                .setView(view)
                .onSaveState(new QuestionView.OnSaveState() {
                    @Override public void onSaveState(Bundle into) {
                        into.putInt(KEY_NPS_SCORE, npsView.getCurrentlySelectedScore());
                    }
                })
                .onRestoreState(new QuestionView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle from) {
                        int score = from.getInt(KEY_NPS_SCORE, -1);
                        npsView.setScore(score);
                    }
                })
                .build();
    }

}
