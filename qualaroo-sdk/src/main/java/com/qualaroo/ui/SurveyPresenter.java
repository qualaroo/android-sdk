package com.qualaroo.ui;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.qualaroo.internal.model.Answer;
import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.ui.render.Theme;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class SurveyPresenter {

    private final SurveyInteractor interactor;
    private final Survey survey;
    private final Theme theme;

    private SurveyView surveyView;
    private boolean isDisplayingQuestion;

    SurveyPresenter(SurveyInteractor interactor, Survey survey, Theme theme) {
        this.interactor = interactor;
        this.survey = survey;
        this.theme = theme;
    }

    void setView(SurveyView view) {
        surveyView = view;
        surveyView.setup(new SurveyViewModel(theme.textColor(), theme.backgroundColor(), theme.buttonDisabledColor(), theme.dimColor(), survey.spec().optionMap().isMandatory(), survey.spec().optionMap().isShowFullScreen()));
        interactor.registerObserver(eventsObserver);
    }

    void init(@Nullable State state) {
        if (state == null) {
            surveyView.showWithAnimation();
        } else {
            isDisplayingQuestion = state.isDisplayingQuestion();
            surveyView.showImmediately();
        }
        interactor.displaySurvey();
    }

    State getSavedState() {
        return new State(isDisplayingQuestion);
    }

    void dropView() {
        interactor.unregisterObserver();
        surveyView = null;
    }

    private SurveyInteractor.EventsObserver eventsObserver = new SurveyInteractor.EventsObserver() {
        @Override public void showQuestion(Question question) {
            surveyView.showQuestion(question);
            isDisplayingQuestion = true;
        }

        @Override public void showMessage(Message message) {
            boolean shouldAnimate = isDisplayingQuestion;
            surveyView.showMessage(message, shouldAnimate);
            isDisplayingQuestion = false;
        }

        @Override public void showLeadGen(QScreen qscreen, List<Question> questions) {
            surveyView.showLeadGen(qscreen, questions);
            if (isDisplayingQuestion) {
                surveyView.forceShowKeyboardWithDelay(600);
            }
            isDisplayingQuestion = true;
        }

        @Override public void closeSurvey() {
            surveyView.closeSurvey();
        }
    };

    void onCloseClicked() {
        interactor.stopSurvey();
    }

    void onAnswered(Answer answer) {
        interactor.questionAnswered(Collections.singletonList(answer));
    }

    void onAnswered(List<Answer> answers) {
        interactor.questionAnswered(answers);
    }

    void onAnsweredWithText(String payload) {
        interactor.questionAnsweredWithText(payload);
    }

    public void onLeadGenAnswered(Map<Long, String> questionIdsWithAnswers) {
        interactor.leadGenAnswered(questionIdsWithAnswers);
    }

    static class State implements Serializable {

        private final boolean isDisplayingQuestion;

        @VisibleForTesting State(boolean isDisplayingQuestion) {
            this.isDisplayingQuestion = isDisplayingQuestion;
        }

        public boolean isDisplayingQuestion() {
            return isDisplayingQuestion;
        }
    }
}
