/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.ui.render;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.qualaroo.R;
import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;


@RestrictTo(LIBRARY)
public final class EmojiQuestionRenderer extends QuestionRenderer {


    EmojiQuestionRenderer(Theme theme) {
        super(theme);
    }


    @Override
    public RestorableView render(final Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
        final View view = View.inflate(context, R.layout.qualaroo__view_question_emoji, null);
        LinearLayout emoji_layout_first = view.findViewById((R.id.emoji_layout_first));
        LinearLayout emoji_layout_second = view.findViewById((R.id.emoji_layout_second));
        LinearLayout emoji_layout_third = view.findViewById((R.id.emoji_layout_third));
        LinearLayout emoji_layout_fourth = view.findViewById((R.id.emoji_layout_fourth));
        LinearLayout emoji_layout_fifth = view.findViewById((R.id.emoji_layout_fifth));

        final ImageView emoji_first = view.findViewById(R.id.emoji_first);
        final ImageView emoji_second = view.findViewById(R.id.emoji_second);
        final ImageView emoji_third = view.findViewById(R.id.emoji_third);
        final ImageView emoji_fourth = view.findViewById(R.id.emoji_fourth);
        final ImageView emoji_fifth = view.findViewById(R.id.emoji_fifth);

        emoji_first.setBackgroundColor(getTheme().backgroundColor());
        emoji_second.setBackgroundColor(getTheme().backgroundColor());
        emoji_third.setBackgroundColor(getTheme().backgroundColor());
        emoji_fourth.setBackgroundColor(getTheme().backgroundColor());
        emoji_fifth.setBackgroundColor(getTheme().backgroundColor());

        Glide.with(context)
                .load(question.answerList().get(0).emojiUrl())
                .into(emoji_first);

        Glide.with(context)
                .load(question.answerList().get(1).emojiUrl())
                .into(emoji_second);

        Glide.with(context)
                .load(question.answerList().get(2).emojiUrl())
                .into(emoji_third);

        Glide.with(context)
                .load(question.answerList().get(3).emojiUrl())
                .into(emoji_fourth);

        Glide.with(context)
                .load(question.answerList().get(4).emojiUrl())
                .into(emoji_fifth);

        GradientDrawable first_grad = (GradientDrawable)emoji_layout_first.getBackground();
        first_grad.setStroke(15,getTheme().textColor());

        GradientDrawable second_grad = (GradientDrawable)emoji_layout_second.getBackground();
        second_grad.setStroke(15,getTheme().textColor());

        GradientDrawable third_grad = (GradientDrawable)emoji_layout_third.getBackground();
        third_grad.setStroke(15,getTheme().textColor());

        GradientDrawable fourth_grad = (GradientDrawable)emoji_layout_fourth.getBackground();
        fourth_grad.setStroke(15,getTheme().textColor());

        GradientDrawable fifth_grad = (GradientDrawable)emoji_layout_fifth.getBackground();
        fifth_grad.setStroke(15,getTheme().textColor());

        emoji_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answer answer = question.answerList().get(0);
                answer(question, answer, onAnsweredListener);
            }
        });

        emoji_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Answer answer = question.answerList().get(1);
                answer(question, answer, onAnsweredListener);
            }
        });

        emoji_third.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Answer answer = question.answerList().get(2);
                answer(question, answer, onAnsweredListener);
            }
        });

        emoji_fourth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Answer answer = question.answerList().get(3);
                answer(question, answer, onAnsweredListener);
            }
        });


        emoji_fifth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Answer answer = question.answerList().get(4);
                answer(question, answer, onAnsweredListener);
            }
        });
        return RestorableView.withId(question.id()).view(view).build();
    }

    private void answer(Question question, Answer answer, OnAnsweredListener onAnsweredListener) {
        UserResponse response = new UserResponse.Builder(question.id())
                .addChoiceAnswer(answer.id())
                .build();
        onAnsweredListener.onResponse(response);
    }
}