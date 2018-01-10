package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.ui.render.widget.FreeformCommentCompoundButton;
import com.qualaroo.ui.render.widget.ListeningCheckableGroup;
import com.qualaroo.ui.render.widget.ListeningCheckableRadioButton;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.DimenUtils;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class RadioQuestionRenderer extends QuestionRenderer {

    private static final String KEY_SELECTED_ITEM = "radio.selectedItem";
    private static final String KEY_FREEFORM_COMMENTS = "question.freeformComments";

    RadioQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override RestorableView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_radio, null);
        final Button button = view.findViewById(R.id.qualaroo__question_radio_confirm);
        button.setText(question.sendText());
        ThemeUtils.applyTheme(button, getTheme());
        final ListeningCheckableGroup container = view.findViewById(R.id.qualaroo__question_radio_options);
        ListeningCheckableGroup.OnCheckedChangeListener listener = new ListeningCheckableGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(ListeningCheckableGroup group, final int answerId) {
                if (question.alwaysShowSend()) {
                    button.setEnabled(true);
                } else {
                    container.setOnCheckedChangeListener(null);
                    container.postDelayed(new Runnable() {
                        @Override public void run() {
                            UserResponse userResponse = buildUserResponse(question.id(), container);
                            onAnsweredListener.onResponse(userResponse);
                        }
                    }, 300);
                }
            }
        };
        container.setOnCheckedChangeListener(listener);
        for (int i = 0; i < question.answerList().size(); i++) {
            Answer answer = question.answerList().get(i);
            View radioButton = buildRadioButton(context, answer);
            container.addView(radioButton);
        }
        button.setVisibility(question.alwaysShowSend() ? View.VISIBLE : View.GONE);
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                UserResponse userResponse = buildUserResponse(question.id(), container);
                onAnsweredListener.onResponse(userResponse);
            }
        });
        return RestorableView.withId(question.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle outState) {
                        saveState(outState, container);
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle savedState) {
                        restoreState(savedState, container);
                    }
                }).build();
    }

    private void restoreState(Bundle savedState, ListeningCheckableGroup radioGroup) {
        int checkedId = savedState.getInt(KEY_SELECTED_ITEM, ListeningCheckableGroup.NOTHING_SELECTED);
        radioGroup.check(checkedId);
        SparseArray<FreeformCommentCompoundButton.State> stateList = savedState.getSparseParcelableArray(KEY_FREEFORM_COMMENTS);
        if (stateList == null) {
            return;
        }
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            FreeformCommentCompoundButton.State state = stateList.get(child.getId());
            if (state != null) {
                ((FreeformCommentCompoundButton) child).restoreState(state);
            }
        }
    }

    private void saveState(Bundle outState, ListeningCheckableGroup radioGroup) {
        outState.putInt(KEY_SELECTED_ITEM, radioGroup.getCheckedId());

        SparseArray<FreeformCommentCompoundButton.State> freeformComments = new SparseArray<>();
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            Checkable child = (Checkable) radioGroup.getChildAt(i);
            if (child instanceof FreeformCommentCompoundButton) {
                FreeformCommentCompoundButton.State state = ((FreeformCommentCompoundButton) child).getState();
                freeformComments.put(state.id, state);
            }
        }
        outState.putSparseParcelableArray(KEY_FREEFORM_COMMENTS, freeformComments);
    }

    private UserResponse buildUserResponse(long questionId, ListeningCheckableGroup checkableGroup) {
        for (int i = 0; i < checkableGroup.getChildCount(); i++) {
            View child = checkableGroup.getChildAt(i);
            if (child instanceof Checkable && ((Checkable) child).isChecked()) {
                Answer answer = (Answer) child.getTag();
                if (child instanceof FreeformCommentCompoundButton) {
                    return new UserResponse.Builder(questionId)
                            .addChoiceAnswerWithComment(answer.id(), ((FreeformCommentCompoundButton) child).getText())
                            .build();
                } else {
                    return new UserResponse.Builder(questionId).addChoiceAnswer(answer.id()).build();
                }
            }
        }
        return new UserResponse.Builder(questionId).build();
    }

    private View buildRadioButton(Context context, Answer answer) {
        final View view;
        if (hasFreeformComment(answer)) {
            CompoundButton radioButton = buildRegularRadioButton(context, answer);
            FreeformCommentCompoundButton wrappedRadioButton = new FreeformCommentCompoundButton(context, radioButton);
            wrappedRadioButton.acceptTheme(getTheme());
            view = wrappedRadioButton;
        } else {
            view = buildRegularRadioButton(context, answer);
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);
        view.setTag(answer);
        return view;
    }

    private CompoundButton buildRegularRadioButton(Context context, Answer answer) {
        int drawablePadding = DimenUtils.px(context, R.dimen.qualaroo__radio_button_drawable_padding);
        int padding = DimenUtils.px(context, R.dimen.qualaroo__radio_button_padding);
        ListeningCheckableRadioButton button = new ListeningCheckableRadioButton(context);
        button.setId(answer.id());
        button.setText(answer.title());
        button.setTextColor(getTheme().textColor());
        ThemeUtils.applyTheme(button, getTheme());
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.qualaroo__radio_text_size));
        button.setPadding(drawablePadding, padding, padding, padding);
        return button;
    }

    private boolean hasFreeformComment(Answer answer) {
        return !TextUtils.isEmpty(answer.explainType());
    }
}
