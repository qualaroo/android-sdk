/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui.render;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.widget.Button;

import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class BinaryQuestionRenderer extends QuestionRenderer {

    BinaryQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override RestorableView render(Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        View view = View.inflate(context, R.layout.qualaroo__view_question_binary, null);

        Button firstButton = view.findViewById(R.id.qualaroo__view_question_binary_first);
        final Answer firstAnswer = question.answerList().get(0);
        ThemeUtils.applyTheme(firstButton, getTheme());
        firstButton.setText(firstAnswer.title());
        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                answer(question, firstAnswer, onAnsweredListener);
            }
        });

        Button secondButton = view.findViewById(R.id.qualaroo__view_question_binary_second);
        final Answer secondAnswer = question.answerList().get(1);
        ThemeUtils.applyTheme(secondButton, getTheme());
        secondButton.setText(secondAnswer.title());
        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                answer(question, secondAnswer, onAnsweredListener);
            }
        });

        return RestorableView.withId(question.id())
                .view(view)
                .build();
    }

    private void answer(Question question, Answer answer, OnAnsweredListener onAnsweredListener) {
        UserResponse response = new UserResponse.Builder(question.id())
                .addChoiceAnswer(answer.id())
                .build();
        onAnsweredListener.onResponse(response);
    }
}
