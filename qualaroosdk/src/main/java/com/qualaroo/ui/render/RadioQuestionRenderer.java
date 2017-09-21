package com.qualaroo.ui.render;

import android.content.Context;
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

import java.util.Collections;
import java.util.LinkedList;

final class RadioQuestionRenderer extends QuestionRenderer{

    RadioQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override View render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_radio, null);
        final Button button = view.findViewById(R.id.qualaroo__question_radio_confirm);
        button.setText(question.sendText());
        ThemeUtils.applyTheme(button, getTheme());
        final RadioGroup radioGroup = view.findViewById(R.id.qualaroo__question_radio_options);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = DimenUtils.px(context, R.dimen.qualaroo__radio_button_top_margin);

        //TODO move this to business logic part so it can be tested nicely
        final LinkedList<Answer> answerList = new LinkedList<>(question.answerList());
        Answer anchoredLastAnswer = null;
        if (question.anchorLast()) {
            anchoredLastAnswer = answerList.removeLast();
        }
        if (question.enableRandom()) {
            Collections.shuffle(answerList);
        }
        if (anchoredLastAnswer != null) {
            answerList.addLast(anchoredLastAnswer);
        }
        //TODO ^ move this to business logic part

        for (int i = 0; i < answerList.size(); i++) {
            Answer answer = answerList.get(i);
            AppCompatRadioButton radioButton = new AppCompatRadioButton(context);
            radioButton.setId(i);
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
            @Override public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (question.alwaysShowSend()) {
                    button.setEnabled(true);
                } else {
                    Answer selectedAnswer = answerList.get(checkedId);
                    onAnsweredListener.onAnswered(question, selectedAnswer);
                }
            }
        });

        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                int buttonIndex = radioGroup.getCheckedRadioButtonId();
                Answer selectedAnswer = answerList.get(buttonIndex);
                onAnsweredListener.onAnswered(question, selectedAnswer);
            }
        });
        return view;
    }

}
