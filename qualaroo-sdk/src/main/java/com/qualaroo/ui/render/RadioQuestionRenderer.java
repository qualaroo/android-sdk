package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.DimenUtils;

import java.util.List;

final class RadioQuestionRenderer extends QuestionRenderer {

    private final static String KEY_SELECTED_ITEM = "radio.selectedItem";
    private final static int NOTHING_SELECTED = -1;

    RadioQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override QuestionView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_radio, null);
        final Button button = view.findViewById(R.id.qualaroo__question_radio_confirm);
        button.setText(question.sendText());
        ThemeUtils.applyTheme(button, getTheme());
        final RadioGroup radioGroup = view.findViewById(R.id.qualaroo__question_radio_options);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = DimenUtils.px(context, R.dimen.qualaroo__radio_button_top_margin);
        for (int i = 0; i < question.answerList().size(); i++) {
            Answer answer = question.answerList().get(i);
            AppCompatRadioButton radioButton = new AppCompatRadioButton(context);
            radioButton.setId(answer.id());
            radioButton.setText(answer.title());
            radioButton.setTextColor(getTheme().textColor());
            ThemeUtils.applyTheme(radioButton, getTheme());
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.qualaroo__radio_text_size));
            radioButton.setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(R.dimen.qualaroo__radio_button_top_margin));
            radioButton.setLayoutParams(layoutParams);
            radioGroup.addView(radioButton);
        }

        button.setVisibility(question.alwaysShowSend() ? View.VISIBLE : View.GONE);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup radioGroup, int answerId) {
                if (question.alwaysShowSend()) {
                    button.setEnabled(true);
                } else {
                    Answer selectedAnswer = getAnswerById(answerId, question.answerList());
                    onAnsweredListener.onAnswered(question, selectedAnswer);
                }
            }
        });

        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                int answerId = radioGroup.getCheckedRadioButtonId();
                Answer selectedAnswer = getAnswerById(answerId, question.answerList());
                onAnsweredListener.onAnswered(question, selectedAnswer);
            }
        });
        return QuestionView.forQuestionId(question.id())
                .setView(view)
                .onSaveState(new QuestionView.OnSaveState() {
                    @Override public void onSaveState(Bundle into) {
                        into.putInt(KEY_SELECTED_ITEM, radioGroup.getCheckedRadioButtonId());
                    }
                })
                .onRestoreState(new QuestionView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle from) {
                        int checkedId = from.getInt(KEY_SELECTED_ITEM, NOTHING_SELECTED);
                        if (checkedId != NOTHING_SELECTED) {
                            radioGroup.check(checkedId);
                        }
                    }
                }).build();

    }

    private Answer getAnswerById(int id, List<Answer> answerList) {
        for (Answer answer : answerList) {
            if (answer.id() == id) {
                return answer;
            }
        }
        return null;
    }
}
