package com.qualaroo.ui;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.ui.render.Theme;

import java.util.Collections;
import java.util.List;

public class SurveyPresenter implements SurveyInteractor.EventsObserver {

    private final SurveyInteractor interactor;
    private final Survey survey;
    private final Theme theme;

    private SurveyView surveyView;
    private Question currentlyDisplayedQuestion;

    SurveyPresenter(SurveyInteractor interactor, Survey survey, Theme theme) {
        this.interactor = interactor;
        this.survey = survey;
        this.theme = theme;
    }

    public void setView(SurveyView view) {
        surveyView = view;
        surveyView.setup(new SurveyViewModel(theme.textColor(), theme.backgroundColor(), theme.buttonDisabledColor(), survey.spec().optionMap().isMandatory(), survey.spec().optionMap().isShowFullScreen()));
        interactor.registerObserver(this);
        interactor.startSurvey();
    }

    public void dropView() {
        interactor.unregisterObserver();
        surveyView = null;
    }

    @Override public void showQuestion(Question question) {
        surveyView.showQuestion(question);
        currentlyDisplayedQuestion = question;
    }

    @Override public void showMessage(Message message) {
        surveyView.showMessage(message);
    }

    @Override public void closeSurvey() {
        surveyView.closeSurvey();
    }

    void onCloseClicked() {
        interactor.stopSurvey();
    }

    void onAnswered(Answer answer) {
        interactor.questionAnswered(currentlyDisplayedQuestion, Collections.singletonList(answer));
    }

    void onAnswered(List<Answer> answers) {
        interactor.questionAnswered(currentlyDisplayedQuestion, answers);
    }

    void onAnsweredWithText(String payload) {
        interactor.questionAnsweredWithText(currentlyDisplayedQuestion, payload);
    }
}
