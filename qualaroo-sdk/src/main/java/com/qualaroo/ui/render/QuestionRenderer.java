package com.qualaroo.ui.render;

import android.content.Context;
import androidx.annotation.RestrictTo;

import com.qualaroo.internal.model.Question;
import com.qualaroo.ui.OnAnsweredListener;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
abstract class QuestionRenderer {

    private final Theme theme;

    QuestionRenderer(Theme theme) {
        this.theme = theme;
    }

    protected Theme getTheme() {
        return theme;
    }

    abstract RestorableView render(Context context, Question question, OnAnsweredListener onAnsweredListener);

}
