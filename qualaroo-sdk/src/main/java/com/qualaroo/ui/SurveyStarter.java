package com.qualaroo.ui;

import android.content.Context;
import android.support.annotation.MainThread;

import com.qualaroo.QualarooActivity;
import com.qualaroo.internal.model.Survey;

public class SurveyStarter {
    private final Context context;

    public SurveyStarter(Context context) {
        this.context = context;
    }

    @MainThread
    public void start(Survey survey) {
        QualarooActivity.showSurvey(context, survey);
    }
}
