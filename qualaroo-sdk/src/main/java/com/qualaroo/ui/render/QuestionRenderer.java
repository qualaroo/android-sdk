package com.qualaroo.ui.render;

import android.content.Context;

import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;

abstract class QuestionRenderer {

    private final Theme theme;

    QuestionRenderer(Theme theme) {
        this.theme = theme;
    }

    protected Theme getTheme() {
        return theme;
    }

    abstract QuestionView render(Context context, Question question, OnAnsweredListener onAnsweredListener);

}
