package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qualaroo.R;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;

final class TextQuestionRenderer extends QuestionRenderer {

    private static final String KEY_TEXT = "question.text";

    TextQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public QuestionView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        View view = View.inflate(context, R.layout.qualaroo__view_question_text, null);
        final TextInputLayout textInputLayout = view.findViewById(R.id.qualaroo__view_question_text_input);
        ThemeUtils.applyTheme(textInputLayout, getTheme());
        final EditText editText = textInputLayout.getEditText();
        editText.setTextColor(getTheme().accentColor());
        final Button button = view.findViewById(R.id.qualaroo__view_question_text_confirm);
        ThemeUtils.applyTheme(button, getTheme());
        button.setText(question.sendText());
        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                onAnsweredListener.onAnsweredWithText(question, editText.getText().toString());
            }
        });
        ThemeUtils.applyTheme(editText, getTheme());
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                button.setEnabled(count > 0);
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override public void afterTextChanged(Editable s) {
            }
        });
        return QuestionView.forQuestionId(question.id())
                .setView(view)
                .onSaveState(new QuestionView.OnSaveState() {
                    @Override public void onSaveState(Bundle into) {
                        into.putString(KEY_TEXT, editText.getText().toString());
                    }
                })
                .onRestoreState(new QuestionView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle from) {
                        String text = from.getString(KEY_TEXT);
                        editText.setText(text);
                    }
                })
                .build();
    }

}
