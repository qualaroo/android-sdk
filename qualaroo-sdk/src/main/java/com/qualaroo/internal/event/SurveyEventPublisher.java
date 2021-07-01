package com.qualaroo.internal.event;

import android.content.Context;
import androidx.annotation.RestrictTo;

import com.qualaroo.QualarooSurveyEventReceiver;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SurveyEventPublisher {

    private final Context context;

    public SurveyEventPublisher(Context context) {
        this.context = context.getApplicationContext();
    }

    public void publish(SurveyEvent surveyEvent) {
        context.sendBroadcast(QualarooSurveyEventReceiver.buildIntent(context, surveyEvent));
    }

}
