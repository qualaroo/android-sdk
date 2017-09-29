package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;

import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnAnsweredListener {
    void onAnswered(Question question, Answer answer);
    void onAnswered(Question question, List<Answer> answers);
    void onAnsweredWithText(Question question, String answer);
}
