package com.qualaroo.ui;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Question;

import java.util.List;

public interface OnAnsweredListener {
    void onAnswered(Question question, Answer answer);
    void onAnswered(Question question, List<Answer> answers);
    void onAnsweredWithText(Question question, String answer);
}
