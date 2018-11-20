/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.internal.event;

import android.content.Context;
import android.support.annotation.RestrictTo;

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
