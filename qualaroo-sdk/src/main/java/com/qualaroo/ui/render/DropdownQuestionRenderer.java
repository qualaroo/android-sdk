package com.qualaroo.ui.render;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;
import com.qualaroo.util.DebouncingOnClickListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class DropdownQuestionRenderer extends QuestionRenderer {

    DropdownQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override public QuestionView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.qualaroo__view_question_dropdown, null);
        Button confirmButton = view.findViewById(R.id.qualaroo__view_question_dropdown_confirm);
        final Spinner spinner = view.findViewById(R.id.qualaroo__view_question_dropdown_spinner);
        confirmButton.setText(question.sendText());
        ArrayAdapter<AnswerItem> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        List<AnswerItem> spinnerItems = new ArrayList<>();
        for (Answer answer : question.answerList()) {
            spinnerItems.add(new AnswerItem(answer));
        }
        adapter.addAll(spinnerItems);
        spinner.setAdapter(adapter);
        ThemeUtils.applyTheme(spinner, getTheme());
        confirmButton.setOnClickListener(new DebouncingOnClickListener() {
            @Override public void doClick(View v) {
                AnswerItem answerItem = (AnswerItem) spinner.getSelectedItem();
                onAnsweredListener.onAnswered(question, answerItem.answer);
            }
        });
        return QuestionView.forQuestionId(question.id())
                .setView(view)
                .build();
    }

    private static class AnswerItem {
        final Answer answer;

        AnswerItem(Answer answer) {
            this.answer = answer;
        }

        @Override public String toString() {
            return answer.title();
        }
    }

}
