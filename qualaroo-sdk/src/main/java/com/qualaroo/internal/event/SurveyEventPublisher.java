package com.qualaroo.internal.event;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.v4.content.LocalBroadcastManager;

import com.qualaroo.QualarooSurveyEventReceiver;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SurveyEventPublisher {

    private final Context context;

    public SurveyEventPublisher(Context context) {
        this.context = context.getApplicationContext();
    }

    public void publish(SurveyEvent surveyEvent) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.sendBroadcast(QualarooSurveyEventReceiver.buildIntent(surveyEvent));
    }

}
