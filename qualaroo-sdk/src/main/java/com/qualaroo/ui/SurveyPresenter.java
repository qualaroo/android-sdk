package com.qualaroo.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.qualaroo.internal.model.Message;
import com.qualaroo.internal.model.QScreen;
import com.qualaroo.internal.model.Question;
import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.model.UserResponse;
import com.qualaroo.ui.render.Theme;
import com.qualaroo.util.UriOpener;

import java.io.Serializable;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
class SurveyPresenter {

    private final SurveyInteractor interactor;
    private final Survey survey;
    private final Theme theme;
    private final UriOpener uriOpener;

    private SurveyView surveyView;
    private boolean isDisplayingQuestion;

    SurveyPresenter(SurveyInteractor interactor, Survey survey, Theme theme, UriOpener uriOpener) {
        this.interactor = interactor;
        this.survey = survey;
        this.theme = theme;
        this.uriOpener = uriOpener;
    }

    void setView(SurveyView view) {
        surveyView = view;
        surveyView.setup(new SurveyViewModel(
                theme.textColor(), theme.backgroundColor(), theme.uiNormal(), theme.uiSelected(), theme.dimColor(),
                theme.dimOpacity(), survey.spec().optionMap().isMandatory(), survey.spec().optionMap().isShowFullScreen(),
                survey.spec().optionMap().logoUrl()));
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

        @Override public void setProgress(float progress) {
            surveyView.setProgress(progress);
        }

        @Override public void openUri(@NonNull String stringUri) {
            uriOpener.openUri(stringUri);
        }

        @Override public void closeSurvey() {
            surveyView.closeSurvey();
        }
    };

    void onCloseClicked() {
        interactor.requestSurveyToStop();
    }

    void onMessageConfirmed(Message message) {
        interactor.messageConfirmed(message);
    }

    public void onResponse(UserResponse userResponse) {
        interactor.onResponse(userResponse);
    }

    public void onLeadGenResponse(List<UserResponse> userResponse) {
        interactor.onLeadGenResponse(userResponse);
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
