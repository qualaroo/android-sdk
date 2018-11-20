/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.ui.render.widget.FreeformCommentCompoundButton;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.DimenUtils;

import java.util.ArrayList;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class CheckboxQuestionRenderer extends QuestionRenderer {

    private static final String KEY_CHECKED_ELEMENTS = "question.checkedElements";
    private static final String KEY_FREEFORM_COMMENTS = "question.freeformComments";

    CheckboxQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public RestorableView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        ViewGroup view = (ViewGroup) View.inflate(context, R.layout.qualaroo__view_question_checkbox, null);
        final ViewGroup checkablesContainer = view.findViewById(R.id.qualaroo__view_question_checkbox_container);
        final Button button = view.findViewById(R.id.qualaroo__view_question_checkbox_confirm);
        button.setText(question.sendText());
        button.setEnabled(!question.isRequired());
        ThemeUtils.applyTheme(button, getTheme());

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int selectedAnswers = CheckboxQuestionRenderer.this.selectedAnswers(checkablesContainer);
                CheckboxQuestionRenderer.this.invalidateViewState(question, checkablesContainer, button, selectedAnswers);
            }
        };
        for (Answer answer : question.answerList()) {
            View checkBox = buildCheckBox(context, answer, listener);
            checkBox.setId(answer.id());
            checkablesContainer.addView(checkBox);
        }
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                UserResponse build = buildUserResponse(question, checkablesContainer);
                onAnsweredListener.onResponse(build);
            }
        });
        return RestorableView.withId(question.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle outState) {
                        CheckboxQuestionRenderer.this.saveState(outState, checkablesContainer);
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle savedState) {
                        CheckboxQuestionRenderer.this.restoreState(savedState, checkablesContainer);
                    }
                })
                .build();
    }

    private void invalidateViewState(Question question, ViewGroup checkablesContainer, Button button, int selectedAnswers) {
        final int minAnswers = question.minAnswersCount();
        final int maxAnswers = question.maxAnswersCount() == 0 ?
                question.answerList().size() :
                question.maxAnswersCount();

        boolean shouldDisableButton =
                        question.isRequired() && (selectedAnswers == 0 || selectedAnswers < minAnswers) ||
                        selectedAnswers > maxAnswers;
        button.setEnabled(!shouldDisableButton);

        enableUncheckedAnswers(checkablesContainer, selectedAnswers < maxAnswers);
    }

    private int selectedAnswers(ViewGroup checkablesContainer) {
        int selectedAnswers = 0;
        for (int j = 0; j < checkablesContainer.getChildCount(); j++) {
            View child = checkablesContainer.getChildAt(j);
            if (child instanceof Checkable && ((Checkable) child).isChecked()) {
                selectedAnswers++;
            }
        }
        return selectedAnswers;
    }

    private void enableUncheckedAnswers(ViewGroup checkablesContainer, boolean enable) {
        for (int j = 0; j < checkablesContainer.getChildCount(); j++) {
            View child = checkablesContainer.getChildAt(j);
            if (child instanceof Checkable && !((Checkable) child).isChecked()) {
                child.setEnabled(enable);
                child.animate().alpha(enable ? 1.0f : 0.4f);
            }
        }
    }

    private UserResponse buildUserResponse(Question question, ViewGroup checkablesContainer) {
        UserResponse.Builder builder = new UserResponse.Builder(question.id());
        for (int i = 0; i < checkablesContainer.getChildCount(); i++) {
            View child = checkablesContainer.getChildAt(i);
            if (child instanceof Checkable && ((Checkable) child).isChecked()) {
                Answer answer = (Answer) child.getTag();
                if (child instanceof FreeformCommentCompoundButton) {
                    builder.addChoiceAnswerWithComment(answer.id(), ((FreeformCommentCompoundButton) child).getText());
                } else {
                    builder.addChoiceAnswer(answer.id());
                }
            }
        }
        return builder.build();
    }

    private void restoreState(Bundle savedState, ViewGroup checkablesContainer) {
        ArrayList<Integer> checkedElements = savedState.getIntegerArrayList(KEY_CHECKED_ELEMENTS);
        if (checkedElements != null) {
            restoreCheckedElementsState(checkablesContainer, checkedElements);
        }
        ArrayList<FreeformCommentCompoundButton.State> freeformCommentsData = savedState.getParcelableArrayList(KEY_FREEFORM_COMMENTS);
        if (freeformCommentsData != null) {
            restoreFreeformCommentsState(checkablesContainer, freeformCommentsData);
        }
    }

    private void restoreCheckedElementsState(ViewGroup checkablesContainer, ArrayList<Integer> checkedElements) {
        for (int i = 0; i < checkablesContainer.getChildCount(); i++) {
            View child = checkablesContainer.getChildAt(i);
            if (checkedElements.contains(child.getId())) {
                ((Checkable) child).setChecked(true);
            }
        }
    }

    private void restoreFreeformCommentsState(ViewGroup checkablesContainer, ArrayList<FreeformCommentCompoundButton.State> freeformCommentsData) {
        for (int i = 0; i < checkablesContainer.getChildCount(); i++) {
            View child = checkablesContainer.getChildAt(i);
            for (FreeformCommentCompoundButton.State state : freeformCommentsData) {
                if (state.id == child.getId()) {
                    ((FreeformCommentCompoundButton) child).restoreState(state);
                }
            }
        }
    }

    private void saveState(Bundle outState, ViewGroup checkablesContainer) {
        ArrayList<Integer> checkedElements = new ArrayList<>();
        ArrayList<FreeformCommentCompoundButton.State> freeformCommentsData = new ArrayList<>();
        for (int i = 0; i < checkablesContainer.getChildCount(); i++) {
            View child = checkablesContainer.getChildAt(i);
            if (child instanceof Checkable && ((Checkable) child).isChecked()) {
                checkedElements.add(child.getId());
            }
            if (child instanceof FreeformCommentCompoundButton) {
                freeformCommentsData.add(((FreeformCommentCompoundButton) child).getState());
            }
        }
        outState.putIntegerArrayList(KEY_CHECKED_ELEMENTS, checkedElements);
        outState.putParcelableArrayList(KEY_FREEFORM_COMMENTS, freeformCommentsData);
    }

    private View buildCheckBox(Context context, Answer answer, CompoundButton.OnCheckedChangeListener listener) {
        final View view;
        if (hasFreeformComment(answer)) {
            view = buildCheckBoxWithFreeformComment(context, answer, listener);
        } else {
            view = buildRegularCheckbox(context, answer, listener);
        }
        view.setTag(answer);
        return view;
    }

    private View buildCheckBoxWithFreeformComment(Context context, Answer answer, CompoundButton.OnCheckedChangeListener listener) {
        CheckBox checkBox = buildRegularCheckbox(context, answer, listener);
        FreeformCommentCompoundButton compoundButton = new FreeformCommentCompoundButton(context, checkBox);
        compoundButton.acceptTheme(getTheme());
        compoundButton.setOnCheckedChangeListener(listener);
        return compoundButton;
    }

    private CheckBox buildRegularCheckbox(Context context, Answer answer, CompoundButton.OnCheckedChangeListener listener) {
        int drawablePadding = DimenUtils.px(context, R.dimen.qualaroo__checkbox_drawable_padding);
        int padding = DimenUtils.px(context, R.dimen.qualaroo__checkbox_padding);
        AppCompatCheckBox checkBox = new AppCompatCheckBox(context);
        ThemeUtils.applyTheme(checkBox, getTheme());
        checkBox.setText(answer.title());
        checkBox.setTextColor(getTheme().textColor());
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.qualaroo__checkbox_text_size));
        checkBox.setPadding(drawablePadding, padding, padding, padding);
        checkBox.setOnCheckedChangeListener(listener);
        return checkBox;
    }

    private boolean hasFreeformComment(Answer answer) {
        return !TextUtils.isEmpty(answer.explainType());
    }

}
