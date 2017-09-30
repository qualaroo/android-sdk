package com.qualaroo.ui;

import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Question;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public interface SurveyView {
    void setup(SurveyViewModel surveyViewModel);
    void showWithAnimation();
    void showImmediately();
    void showQuestion(Question question);
    void showMessage(Message message);
    void closeSurvey();
}
