package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Answer;

import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface OnAnsweredListener {
    void onAnswered(Answer answer);
    void onAnswered(List<Answer> answers);
    void onAnsweredWithText(String answer);
}
