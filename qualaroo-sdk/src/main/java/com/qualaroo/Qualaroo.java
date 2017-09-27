package com.qualaroo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.qualaroo.internal.model.Survey;
import com.qualaroo.internal.network.Callback;
import com.qualaroo.internal.network.SurveyClient;

import java.util.ArrayList;
import java.util.List;

public class Qualaroo implements QualarooBase {

    private static QualarooBase INSTANCE;
    private final SurveyClient surveyClient;
    private final Context context;
    private List<Survey> surveys = new ArrayList<>();
    private SurveysListener surveysListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public static QualarooBase getInstance() {
        return INSTANCE;
    }


    public static Builder with(Context context) {
        return new Builder(context);
    }

    public void init() {
        surveyClient.fetchSurveys(new Callback<List<Survey>>() {
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

    @Override public void showSurvey(@NonNull String alias) {
        if (alias.length() == 0) {
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
        QualarooActivity.showSurvey(context, surveyToDisplay);
    }

    @Override public void setUserId(@NonNull String userId) {

    }

    @Override public void setUserProperty(@NonNull String key, String value) {

    }

    @Override public void setPreferredLanguage(@NonNull String iso2Language) {

    }
    public static class Builder {
        private final Context context;
        private Credentials credentials;
        private boolean debugMode = false;

        Builder(Context context) {
            this.context = context;
        }

        public Builder setApiKey(String apiKey) {
            this.credentials = new Credentials(apiKey);
            return this;
        }

        public Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public void init() {
            if (INSTANCE == null) {
                INSTANCE = new Qualaroo(context, credentials, debugMode);
            }
        }
    }

}
