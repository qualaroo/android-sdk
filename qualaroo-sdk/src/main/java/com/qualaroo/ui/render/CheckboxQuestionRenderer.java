package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.DimenUtils;

import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class CheckboxQuestionRenderer extends QuestionRenderer {

    private static final String KEY_CHECKED_ELEMENTS = "question.checkedElements";

    CheckboxQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public RestorableView render(Context context, Question question, final OnAnsweredListener onAnsweredListener) {
        ViewGroup view = (ViewGroup) View.inflate(context, R.layout.qualaroo__view_question_checkbox, null);
        final ViewGroup checkboxesContainer = view.findViewById(R.id.qualaroo__view_question_checkbox_container);
        final Button button = view.findViewById(R.id.qualaroo__view_question_checkbox_confirm);
        button.setText(question.sendText());
        button.setEnabled(false);
        ThemeUtils.applyTheme(button, getTheme());
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ViewGroup parent = (ViewGroup) compoundButton.getParent();
                boolean anyChecked = false;
                for (int j = 0; j < parent.getChildCount(); j++) {
                    View child = parent.getChildAt(j);
                    if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                        anyChecked = true;
                        break;
                    }
                }
                button.setEnabled(anyChecked);
            }
        };
        int drawablePadding = DimenUtils.px(context, R.dimen.qualaroo__checkbox_drawable_padding);
        int padding = DimenUtils.px(context, R.dimen.qualaroo__checkbox_padding);
        for (Answer answer : question.answerList()) {
            AppCompatCheckBox checkBox = new AppCompatCheckBox(context);
            ThemeUtils.applyTheme(checkBox, getTheme());
            checkBox.setId(answer.id());
            checkBox.setText(answer.title());
            checkBox.setTextColor(getTheme().textColor());
            checkBox.setOnCheckedChangeListener(listener);
            checkBox.setTag(answer);
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.qualaroo__checkbox_text_size));
            checkBox.setPadding(drawablePadding, padding, padding, padding);
            checkboxesContainer.addView(checkBox);
        }
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                List<Answer> selectedAnswers = new ArrayList<>();
                for (int i = 0; i < checkboxesContainer.getChildCount(); i++) {
                    View child = checkboxesContainer.getChildAt(i);
                    if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                        selectedAnswers.add((Answer) child.getTag());
                    }
                }
                onAnsweredListener.onAnswered(selectedAnswers);
            }
        });
        return RestorableView.withId(question.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle into) {
                        ArrayList<Integer> checkedElements = new ArrayList<>();
                        for (int i = 0; i < checkboxesContainer.getChildCount(); i++) {
                            CheckBox checkBox = (CheckBox) checkboxesContainer.getChildAt(i);
                            if (checkBox.isChecked()) {
                                checkedElements.add(checkBox.getId());
                            }
                        }
                        into.putIntegerArrayList(KEY_CHECKED_ELEMENTS, checkedElements);
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle from) {
                        ArrayList<Integer> checkedElements = from.getIntegerArrayList(KEY_CHECKED_ELEMENTS);
                        if (checkedElements != null) {
                            for (int i = 0; i < checkboxesContainer.getChildCount(); i++) {
                                CheckBox checkBox = (CheckBox) checkboxesContainer.getChildAt(i);
                                if (checkedElements.contains(checkBox.getId())) {
                                    checkBox.setChecked(true);
                                }
                            }
                        }
                    }
                })
                .build();
    }

}
