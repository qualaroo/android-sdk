package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.DimenUtils;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class RadioQuestionRenderer extends QuestionRenderer {

    private final static String KEY_SELECTED_ITEM = "radio.selectedItem";
    private final static int NOTHING_SELECTED = -1;

    RadioQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override RestorableView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_radio, null);
        final Button button = view.findViewById(R.id.qualaroo__question_radio_confirm);
        button.setText(question.sendText());
        ThemeUtils.applyTheme(button, getTheme());
        final RadioGroup radioGroup = view.findViewById(R.id.qualaroo__question_radio_options);
        int drawablePadding = DimenUtils.px(context, R.dimen.qualaroo__radio_button_drawable_padding);
        int padding = DimenUtils.px(context, R.dimen.qualaroo__radio_button_padding);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < question.answerList().size(); i++) {
            Answer answer = question.answerList().get(i);
            AppCompatRadioButton radioButton = new AppCompatRadioButton(context);
            radioButton.setId(answer.id());
            radioButton.setText(answer.title());
            radioButton.setTextColor(getTheme().textColor());
            ThemeUtils.applyTheme(radioButton, getTheme());
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.qualaroo__radio_text_size));
            radioButton.setPadding(drawablePadding, padding, padding, padding);
            radioButton.setLayoutParams(layoutParams);
            radioGroup.addView(radioButton);
        }
        button.setVisibility(question.alwaysShowSend() ? View.VISIBLE : View.GONE);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup radioGroup, final int answerId) {
                if (question.alwaysShowSend()) {
                    button.setEnabled(true);
                } else {
                    radioGroup.setOnCheckedChangeListener(null);
                    radioGroup.postDelayed(new Runnable() {
                        @Override public void run() {
                            UserResponse userResponse = buildUserResponse(question.id(), answerId);
                            onAnsweredListener.onResponse(userResponse);
                        }
                    }, 300);
                }
            }
        });

        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                int answerId = radioGroup.getCheckedRadioButtonId();
                UserResponse userResponse = buildUserResponse(question.id(), answerId);
                onAnsweredListener.onResponse(userResponse);
            }
        });
        return RestorableView.withId(question.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle into) {
                        into.putInt(KEY_SELECTED_ITEM, radioGroup.getCheckedRadioButtonId());
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle from) {
                        int checkedId = from.getInt(KEY_SELECTED_ITEM, NOTHING_SELECTED);
                        if (checkedId != NOTHING_SELECTED) {
                            radioGroup.check(checkedId);
                        }
                    }
                }).build();
    }

    private UserResponse buildUserResponse(long questionId, int answerId) {
        return new UserResponse.Builder(questionId).addChoiceAnswer(answerId).build();
    }
}
