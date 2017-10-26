package com.qualaroo.ui.render;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.qualaroo.R;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;

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

    public View render(Context context, QScreen qScreen, List<Question> questions) {
        final View view = LayoutInflater.from(context).inflate(R.layout.qualaroo__view_question_lead_gen, null);

        final Button button = view.findViewById(R.id.qualaroo__view_question_lead_gen_confirm);
        button.setText(qScreen.sendText());
        ThemeUtils.applyTheme(button, theme);

        final ViewGroup inputFieldsParent = view.findViewById(R.id.qualaroo__view_question_lead_gen_input_fields);
        for (Question question : questions) {
            TextInputLayout inputField = buildTextInput(context, question.title(), question.cname());
            inputFieldsParent.addView(inputField);
            if (question.isRequired()) {
                button.setEnabled(false);
            }
        }
        return view;
    }

    private TextInputLayout buildTextInput(Context context, String title, String fieldType) {
        TextInputLayout inputLayout = new TextInputLayout(context);
        TextInputEditText editText = new TextInputEditText(context);
        editText.setHint(title);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
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
