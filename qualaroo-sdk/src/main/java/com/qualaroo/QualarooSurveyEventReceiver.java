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

/**
 * <p>Base {@link BroadcastReceiver} class for getting survey related events.</p>
 * <p>Events are being distributed via {@link android.content.Context#sendBroadcast(Intent)}.</p>
 *
 * <h3>Usage:</h3>
 * <p>Register your implementation via {@link Context#registerReceiver Context.registerReceiver()}, use {@link this#intentFilter()}
 * to acquire an {@link IntentFilter}.</p>
 *
 * <p>Register your implementation via AndroidManifest.xml using <b>com.qualaroo.event.ACTION_SURVEY_EVENT</b> action.</p>
 *
 */
public abstract class QualarooSurveyEventReceiver extends BroadcastReceiver {

    /**
     * Type of an event
     */
    @IntDef({EVENT_TYPE_SHOWN, EVENT_TYPE_DISMISSED, EVENT_TYPE_FINISHED})
    @Retention(RetentionPolicy.CLASS)
    public @interface Type {}

    public static final int EVENT_TYPE_SHOWN = 1;
    public static final int EVENT_TYPE_DISMISSED = 2;
    public static final int EVENT_TYPE_FINISHED = 3;

    public static Intent buildIntent(Context context, SurveyEvent event) {
        Intent intent = new Intent(ACTION_SURVEY_EVENT);
        intent.setPackage(context.getPackageName());
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
     * @param surveyAlias alias of a survey
     * @param eventType one of EVENT_TYPE_SHOWN, EVENT_TYPE_DISMISSED, EVENT_TYPE_FINISHED
     */
    protected abstract void onSurveyEvent(@NonNull String surveyAlias, @Type int eventType);
}
