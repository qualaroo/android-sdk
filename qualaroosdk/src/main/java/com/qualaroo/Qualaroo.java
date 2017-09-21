package com.qualaroo;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.SurveyClient;
import com.qualaroo.internal.network.Callback;

import java.util.ArrayList;
import java.util.List;

public class Qualaroo {

    private final SurveyClient surveyClient;
    private List<Survey> surveys = new ArrayList<>();
    private SurveysListener surveysListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public void setSurveysListener(SurveysListener surveysListener) {
        this.surveysListener = surveysListener;
    }

    public interface SurveysListener {
        void onSurveysReady(List<Survey> surveys);
    }

    public Qualaroo(SurveyClient surveyClient) {
        this.surveyClient = surveyClient;
    }

    public void init() {
        this.surveyClient.fetchSurveys(new Callback<List<Survey>>() {
            @Override public void onSuccess(List<Survey> result) {
                surveys.clear();
                surveys.addAll(result);
                handler.post(new Runnable() {
                    @Override public void run() {
                        surveysListener.onSurveysReady(surveys);
                    }
                });
            }

            @Override public void onFailure(Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public void showSurvey(Activity activity, @NonNull String alias) {
        if (alias == null || alias.length() == 0) {
            throw new IllegalArgumentException("Alias can't be null or empty!");
        }
        Survey surveyToDisplay = null;
        for (Survey survey : surveys) {
            if (alias.equals(survey.canonicalName())) {
                surveyToDisplay = survey;
            }
        }
        if (surveyToDisplay == null) {
            throw new IllegalArgumentException("Survey not found");
        }
        QualarooActivity.showSurvey(activity, surveyToDisplay);
    }

    public void showSurvey(Activity activity, Survey survey) {
        QualarooActivity.showSurvey(activity, survey);
    }

}
