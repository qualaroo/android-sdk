package com.qualaroo.ui;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.ui.render.Theme;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
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
        surveyView.setup(new SurveyViewModel(theme.textColor(), theme.backgroundColor(), theme.buttonDisabledColor(), theme.dimColor(), survey.spec().optionMap().isMandatory(), survey.spec().optionMap().isShowFullScreen()));
        interactor.registerObserver(this);
    }

    void init(@Nullable State state) {
        if (state == null) {
            surveyView.showWithAnimation();
        } else {
            currentlyDisplayedQuestion = state.question();
            surveyView.showImmediately();
        }
        interactor.displaySurvey();
    }

    State getSavedState() {
        return new State(currentlyDisplayedQuestion);
    }

    void dropView() {
        interactor.unregisterObserver();
        surveyView = null;
    }

    @Override public void showQuestion(Question question) {
        surveyView.showQuestion(question);
        currentlyDisplayedQuestion = question;
    }

    @Override public void showMessage(Message message) {
        boolean shouldAnimate = currentlyDisplayedQuestion != null;
        surveyView.showMessage(message, shouldAnimate);
        currentlyDisplayedQuestion = null;
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

    static class State implements Serializable {

        private final Question question;

        private State(Question question) {
            this.question = question;
        }

        public Question question() {
            return question;
        }
    }
}
