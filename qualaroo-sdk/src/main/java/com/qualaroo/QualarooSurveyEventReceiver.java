package com.qualaroo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.qualaroo.internal.event.SurveyEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class QualarooSurveyEventReceiver extends BroadcastReceiver {

    @IntDef({EVENT_TYPE_SHOWN, EVENT_TYPE_DISMISSED, EVENT_TYPE_FINISHED})
    @Retention(RetentionPolicy.CLASS)
    public @interface Type {}

    public static final int EVENT_TYPE_SHOWN = 1;
    public static final int EVENT_TYPE_DISMISSED = 2;
    public static final int EVENT_TYPE_FINISHED = 3;

    public static Intent buildIntent(SurveyEvent event) {
        Intent intent = new Intent(ACTION_SURVEY_EVENT);
        intent.putExtra(EXTRA_KEY_SURVEY_EVENT, event);
        return intent;
    }

    public static IntentFilter intentFilter() {
        return new IntentFilter(ACTION_SURVEY_EVENT);
    }

    private static final String ACTION_SURVEY_EVENT = "com.qualaroo.event.ACTION_SURVEY_EVENT";
    private static final String EXTRA_KEY_SURVEY_EVENT = "com.qualaroo.event.EXTRA_KEY_SURVEY_EVENT";

    @CallSuper @Override public void onReceive(Context context, Intent intent) {
        if (hasInvalidAction(intent)) {
            return;
        }
        SurveyEvent surveyEvent = intent.getParcelableExtra(EXTRA_KEY_SURVEY_EVENT);
        if (surveyEvent != null) {
            onSurveyEvent(surveyEvent.alias(), surveyEvent.type());
        }
    }

    private boolean hasInvalidAction(Intent intent) {
        return !ACTION_SURVEY_EVENT.equals(intent.getAction());
    }

    /**
     * Called when there are new events related to any survey.
     * @param surveyAlias alias of a survey related to an event
     * @param eventType one of EVENT_TYPE_SHOWN, EVENT_TYPE_DISMISSED, EVENT_TYPE_FINISHED
     */
    protected abstract void onSurveyEvent(@NonNull String surveyAlias, @Type int eventType);
}
