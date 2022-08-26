package com.qualaroo.ui.render;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.RestrictTo;
import androidx.appcompat.widget.AppCompatTextView;
import android.widget.TextView;

import com.qualaroo.internal.ImageProvider;
import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;

import java.util.Locale;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
final class UnknownQuestionTypeRenderer extends QuestionRenderer {

    UnknownQuestionTypeRenderer(Theme theme) {
        super(theme);
    }

    @Override public RestorableView render(Context context, Question question, OnAnsweredListener onAnsweredListener) {
        TextView view = new AppCompatTextView(context);
        view.setText(String.format(Locale.ROOT,"We're sorry! Question with id %d is not supported yet", question.id()));
        view.setTextColor(Color.RED);
        return RestorableView.withId(question.id())
                .view(view)
                .build();
    }
}
