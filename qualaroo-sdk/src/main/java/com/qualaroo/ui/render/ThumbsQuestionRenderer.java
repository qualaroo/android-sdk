package com.qualaroo.ui.render;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.qualaroo.R;
import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.OnAnsweredListener;

public class ThumbsQuestionRenderer extends QuestionRenderer {
    ThumbsQuestionRenderer(Theme theme) {
        super(theme);
    }

    @Override
    public RestorableView render(final Context context, final Question question, final OnAnsweredListener onAnsweredListener) {
         View view = View.inflate(context, R.layout.qualaroo__view_question_thumbs, null);
        LinearLayout thumbs_up_layout = view.findViewById((R.id.thumbs_up_layout));
        LinearLayout thumbs_down_layout = view.findViewById((R.id.thumbs_down_layout));


         ImageView thumbs_up = view.findViewById(R.id.thumbs_up);
         ImageView thumbs_down = view.findViewById(R.id.thumbs_down);


        thumbs_up.setBackgroundColor(getTheme().backgroundColor());
        thumbs_down.setBackgroundColor(getTheme().backgroundColor());


        Glide.with(context)
                .load(question.answerList().get(0).emojiUrl())
                .into(thumbs_up);

        Glide.with(context)
                .load(question.answerList().get(1).emojiUrl())
                .into(thumbs_down);

        GradientDrawable first_grad = (GradientDrawable)thumbs_up_layout.getBackground();
        first_grad.setStroke(15,getTheme().textColor());

        GradientDrawable second_grad = (GradientDrawable)thumbs_down_layout.getBackground();
        second_grad.setStroke(15,getTheme().textColor());

        thumbs_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answer answer = question.answerList().get(0);
                answer(question, answer, onAnsweredListener);
            }
        });

        thumbs_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Answer answer = question.answerList().get(1);
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
