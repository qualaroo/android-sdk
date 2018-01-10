package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.text.Editable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

import com.qualaroo.R;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.KeyboardUtil;
import com.qualaroo.util.TextWatcherAdapter;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class TextQuestionRenderer extends QuestionRenderer {

    private static final String KEY_TEXT = "question.text";

    TextQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public RestorableView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        View view = View.inflate(context, R.layout.qualaroo__view_question_text, null);
        final EditText editText = view.findViewById(R.id.qualaroo__view_question_text_input);
        editText.setSelection(0);
        editText.requestFocus();
        editText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                if (editText != null) {
                    editText.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    editText.postDelayed(new Runnable() {
                        @Override public void run() {
                            KeyboardUtil.showKeyboard(editText);
                        }
                    }, 300);
                }
            }
        });
        ThemeUtils.applyTheme(editText, getTheme());
        final Button button = view.findViewById(R.id.qualaroo__view_question_text_confirm);
        ThemeUtils.applyTheme(button, getTheme());
        button.setText(question.sendText());
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                KeyboardUtil.hideKeyboard(editText);
                button.postDelayed(new Runnable() {
                    @Override public void run() {
                        if (editText.getText() != null) {
                            onAnsweredListener.onResponse(
                                    new UserResponse.Builder(question.id())
                                            .addTextAnswer(editText.getText().toString())
                                            .build()
                            );
                        }
                    }
                }, 300);
            }
        });
        button.setEnabled(!question.isRequired());
        editText.addTextChangedListener(new TextWatcherAdapter() {
            @Override public void afterTextChanged(Editable s) {
                if (question.isRequired()) {
                    button.setEnabled(s.length() > 0);
                }
            }
        });
        return RestorableView.withId(question.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle outState) {
                        outState.putString(KEY_TEXT, editText.getText().toString());
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle savedState) {
                        String text = savedState.getString(KEY_TEXT);
                        editText.setText(text);
                    }
                })
                .build();
    }

}
