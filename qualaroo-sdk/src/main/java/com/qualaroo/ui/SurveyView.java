package com.qualaroo.ui;

import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Question;

public interface SurveyView {
    void setup(SurveyViewModel surveyViewModel);
    void showWithAnimation();
    void showImmediately();
    void showQuestion(Question question);
    void showMessage(Message message);
    void closeSurvey();
}
