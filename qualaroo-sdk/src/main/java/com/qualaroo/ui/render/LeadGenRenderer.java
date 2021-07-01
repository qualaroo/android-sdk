package com.qualaroo.ui.render;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.RestrictTo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.collection.LongSparseArray;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.qualaroo.R;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnLeadGenAnswerListener;
import com.qualaroo.util.DebouncingOnClickListener;
import com.qualaroo.util.DimenUtils;
import com.qualaroo.util.KeyboardUtil;
import com.qualaroo.util.TextWatcherAdapter;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LeadGenRenderer {

    private static final String FIELD_TYPE_FIRST_NAME = "first_name";
    private static final String FIELD_TYPE_LAST_NAME = "last_name";
    private static final String FIELD_TYPE_PHONE = "phone";
    private static final String FIELD_TYPE_EMAIL = "email";

    private final Theme theme;

    LeadGenRenderer(Theme theme) {
        this.theme = theme;
    }

    public RestorableView render(Context context, final QScreen qScreen, final List<Question> questions, final OnLeadGenAnswerListener onLeadGenAnswerListener) {
        final View view = LayoutInflater.from(context).inflate(R.layout.qualaroo__view_question_lead_gen, null);

        final Button button = view.findViewById(R.id.qualaroo__view_question_lead_gen_confirm);
        button.setText(qScreen.sendText());
        ThemeUtils.applyTheme(button, theme);

        final ViewGroup parent = view.findViewById(R.id.qualaroo__view_question_lead_gen_input_fields);
        final LongSparseArray<TextInputLayout> fields = new LongSparseArray<>();
        final List<EditText> requiredFields = new ArrayList<>();
        for (Question question : questions) {
            TextInputLayout inputField = buildTextInput(context, question);
            parent.addView(inputField);
            if (question.isRequired()) {
                button.setEnabled(false);
                requiredFields.add(inputField.getEditText());
                inputField.getEditText().addTextChangedListener(new TextWatcherAdapter() {
                    @Override public void afterTextChanged(Editable s) {
                        boolean enableButton = true;
                        for (EditText requiredField : requiredFields) {
                            if (requiredField.length() == 0) {
                                enableButton = false;
                            }
                        }
                        button.setEnabled(enableButton);
                    }
                });
            }
            fields.append(question.id(), inputField);
        }

        button.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                final List<UserResponse> leadGenResponse = new ArrayList<>(questions.size());
                for (Question question : questions) {
                    TextInputLayout inputLayout = fields.get(question.id());
                    UserResponse response = new UserResponse.Builder(question.id())
                            .addTextAnswer(inputLayout.getEditText().getText().toString())
                            .build();
                    leadGenResponse.add(response);
                }
                KeyboardUtil.hideKeyboard(button);
                button.postDelayed(new Runnable() {
                    @Override public void run() {
                        onLeadGenAnswerListener.onResponse(leadGenResponse);
                    }
                }, 600);
            }
        });
        return RestorableView.withId(qScreen.id())
                .view(view)
                .onSaveState(new RestorableView.OnSaveState() {
                    @Override public void onSaveState(Bundle outState) {
                        for (Question question : questions) {
                            outState.putString(
                                    String.valueOf(question.id()),
                                    fields.get(question.id()).getEditText().getText().toString()
                            );
                        }
                    }
                })
                .onRestoreState(new RestorableView.OnRestoreState() {
                    @Override public void onRestoreState(Bundle savedState) {
                        for (Question question : questions) {
                            String text = savedState.getString(String.valueOf(question.id()), "");
                            fields.get(question.id()).getEditText().setText(text);
                        }
                    }
                }).build();
    }

    private TextInputLayout buildTextInput(Context context, Question question) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int topMargin = (int) DimenUtils.toPx(context, 8);
        layoutParams.setMargins(0, topMargin, 0, 0);
        TextInputLayout inputLayout = new TextInputLayout(context);
        inputLayout.setLayoutParams(layoutParams);
        TextInputEditText editText = new TextInputEditText(context);
        String hint = question.title();
        if (question.isRequired()) {
            hint = hint.concat(" *");
        }
        editText.setHint(hint);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        String fieldType = question.cname();
        if (FIELD_TYPE_FIRST_NAME.equals(fieldType) || FIELD_TYPE_LAST_NAME.equals(fieldType)) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        } else if (FIELD_TYPE_PHONE.equals(fieldType)) {
            editText.setInputType(InputType.TYPE_CLASS_PHONE);
        } else if (FIELD_TYPE_EMAIL.equals(fieldType)) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
        editText.setMaxLines(1);
        inputLayout.addView(editText);
        ThemeUtils.applyTheme(inputLayout, theme);
        ThemeUtils.applyTheme(editText, theme);
        return inputLayout;
    }

}
